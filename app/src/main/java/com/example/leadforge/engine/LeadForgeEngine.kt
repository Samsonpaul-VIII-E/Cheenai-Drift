package com.example.leadforge.engine

import com.example.leadforge.data.LeadForgeRepository
import com.example.leadforge.data.entity.JobEntity
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.data.entity.PageEntity
import com.example.leadforge.model.ExtractedData
import com.example.leadforge.model.FetchResult
import com.example.leadforge.model.JobStatus
import com.example.leadforge.model.ScrapeConfig
import com.example.leadforge.security.SsrfValidator
import com.example.leadforge.security.ValidationResult
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class EngineTelemetry(
    val status: JobStatus = JobStatus.QUEUED,
    val currentUrl: String = "",
    val pagesDiscovered: Int = 0,
    val pagesProcessed: Int = 0,
    val leadsFound: Int = 0,
    val errorsCount: Int = 0,
    val requestsPerSecond: Double = 0.0,
    val averageLatencyMs: Long = 0,
    val activeJobId: Long? = null,
    val currentTargetHost: String = ""
)

data class OperationEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isError: Boolean = false,
    val isSecurity: Boolean = false,
    val isLead: Boolean = false
)

data class ProbeResult(
    val url: String,
    val statusCode: Int,
    val elapsedMs: Long,
    val data: ExtractedData? = null,
    val error: String? = null
)

class LeadForgeEngine(
    private val repository: LeadForgeRepository,
    private val scope: CoroutineScope
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    private val robotsChecker = RobotsChecker(httpClient)

    private val _telemetry = MutableStateFlow(EngineTelemetry())
    val telemetry: StateFlow<EngineTelemetry> = _telemetry.asStateFlow()

    private val _events = MutableSharedFlow<OperationEvent>(replay = 50)
    val events: SharedFlow<OperationEvent> = _events.asSharedFlow()

    private var currentExecutionJob: Job? = null
    private val isPaused = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    private val totalRequests = AtomicInteger(0)
    private val totalLatency = AtomicLong(0)

    var webhookConfigSupplier: (() -> com.example.leadforge.model.WebhookConfig)? = null

    fun startOperation(config: ScrapeConfig) {
        if (_telemetry.value.status == JobStatus.RUNNING) {
            emitLog("ENGINE", "An operation is already running", isError = true)
            return
        }

        isPaused.set(false)
        isCancelled.set(false)
        totalRequests.set(0)
        totalLatency.set(0)

        currentExecutionJob = scope.launch(Dispatchers.IO) {
            var jobId: Long = 0
            try {
                emitLog("INIT", "Validating target ${config.targetUrl} against SSRF policies...", isSecurity = true)

                // 1. SSRF Validation
                val validation = SsrfValidator.validate(config.targetUrl)
                if (validation !is ValidationResult.Valid) {
                    val reason = (validation as ValidationResult.Rejected).reason
                    emitLog("SSRF_BLOCK", "REJECTED: $reason", isError = true, isSecurity = true)
                    repository.logAudit("SSRF_VIOLATION", "Target URL ${config.targetUrl} was rejected: $reason", "SECURITY")
                    _telemetry.value = _telemetry.value.copy(status = JobStatus.FAILED)
                    return@launch
                }

                val validatedTarget = validation.canonicalUrl
                val host = validation.host
                emitLog("SSRF_PASS", "Safe public destination verified: $host (IP: ${validation.resolvedIps.firstOrNull() ?: "OK"})", isSecurity = true)

                // 2. Create Job in Room
                val jobEntity = JobEntity(
                    targetUrl = validatedTarget,
                    status = JobStatus.RUNNING.name,
                    startedAt = System.currentTimeMillis()
                )
                jobId = repository.createJob(jobEntity)
                repository.logAudit("JOB_STARTED", "LeadForge X launched operation on $validatedTarget (Job #$jobId)", "INFO", jobId)

                _telemetry.value = EngineTelemetry(
                    status = JobStatus.RUNNING,
                    currentUrl = validatedTarget,
                    activeJobId = jobId,
                    currentTargetHost = host
                )

                // 3. Queue and Crawl loop
                val urlQueue = ArrayDeque<Pair<String, Int>>()
                urlQueue.add(Pair(validatedTarget, 0))
                val discoveredSet = mutableSetOf(validatedTarget)

                val concurrencySemaphore = Semaphore(config.concurrency.coerceIn(1, 16))
                val rateIntervalMs = (1000.0 / config.requestsPerSecond.coerceIn(0.5, 10.0)).toLong()

                var pagesProcessedCount = 0
                var leadsFoundCount = 0
                var errorsCount = 0

                while (urlQueue.isNotEmpty() && pagesProcessedCount < config.maxPages && !isCancelled.get()) {
                    while (isPaused.get() && !isCancelled.get()) {
                        _telemetry.value = _telemetry.value.copy(status = JobStatus.PAUSED)
                        delay(500)
                    }

                    if (isCancelled.get()) break

                    val (currentUrl, currentDepth) = urlQueue.removeFirst()
                    _telemetry.value = _telemetry.value.copy(
                        status = JobStatus.RUNNING,
                        currentUrl = currentUrl,
                        pagesDiscovered = discoveredSet.size,
                        pagesProcessed = pagesProcessedCount,
                        leadsFound = leadsFoundCount,
                        errorsCount = errorsCount
                    )

                    // Check if already visited in this session
                    if (repository.hasVisited(currentUrl)) {
                        continue
                    }

                    // Respect robots.txt if configured
                    if (config.respectRobots) {
                        val allowed = robotsChecker.isAllowed(currentUrl)
                        if (!allowed) {
                            emitLog("ROBOTS", "Disallowed by robots.txt: $currentUrl", isError = true)
                            repository.recordVisited(
                                PageEntity(
                                    url = currentUrl,
                                    jobId = jobId,
                                    statusCode = 403,
                                    title = "Blocked by robots.txt",
                                    contentType = "text/plain"
                                )
                            )
                            continue
                        }
                    }

                    // Politeness rate limiter delay with randomized jitter
                    delay(rateIntervalMs + (Math.random() * 120).toLong())

                    val fetchResult = concurrencySemaphore.withPermit {
                        fetchPage(currentUrl)
                    }

                    val reqCount = totalRequests.incrementAndGet()
                    val totalLat = totalLatency.addAndGet(fetchResult.elapsedMs)
                    val avgLat = totalLat / reqCount

                    _telemetry.value = _telemetry.value.copy(
                        averageLatencyMs = avgLat,
                        requestsPerSecond = if (fetchResult.elapsedMs > 0) 1000.0 / fetchResult.elapsedMs else config.requestsPerSecond
                    )

                    if (fetchResult.error != null) {
                        errorsCount++
                        emitLog("FETCH_ERR", "$currentUrl -> ${fetchResult.error}", isError = true)
                        repository.recordVisited(
                            PageEntity(
                                url = currentUrl,
                                jobId = jobId,
                                statusCode = fetchResult.statusCode,
                                title = "Error: ${fetchResult.error}",
                                contentType = ""
                            )
                        )
                        continue
                    }

                    if (fetchResult.blocked) {
                        emitLog("BLOCKED", "Anti-bot marker detected at $currentUrl", isError = true)
                        continue
                    }

                    pagesProcessedCount++
                    emitLog("PARSED", "$currentUrl (${fetchResult.statusCode}) in ${fetchResult.elapsedMs}ms")

                    // Extract data from real HTML
                    val extracted = DataExtractor.extract(fetchResult.text, currentUrl, config.includeRoleEmails)

                    // Score lead
                    val scoreBreakdown = FastScorer.score(extracted, config.keywords)

                    // Deterministic fingerprint
                    val fp = FastFingerprint.compute(
                        extracted.businessName,
                        extracted.emails.firstOrNull() ?: "",
                        extracted.phones.firstOrNull() ?: "",
                        currentUrl
                    )

                    // Record page visit checkpoint
                    repository.recordVisited(
                        PageEntity(
                            url = currentUrl,
                            jobId = jobId,
                            statusCode = fetchResult.statusCode,
                            title = extracted.title,
                            bodyHash = FastFingerprint.sha256(fetchResult.text.take(5000)),
                            contentType = fetchResult.contentType,
                            elapsedMs = fetchResult.elapsedMs
                        )
                    )

                    // Save verified lead if contact points or high relevance are present
                    if (extracted.emails.isNotEmpty() || extracted.phones.isNotEmpty() || scoreBreakdown.totalScore >= 25.0) {
                        leadsFoundCount++
                        val lead = LeadEntity(
                            fingerprint = fp,
                            url = currentUrl,
                            domain = host,
                            name = extracted.businessName,
                            category = if (scoreBreakdown.matchedKeywords.isNotEmpty()) scoreBreakdown.matchedKeywords.first() else "Public Directory",
                            address = extracted.address,
                            phone = extracted.phones.firstOrNull() ?: "",
                            phoneE164 = extracted.phoneE164.firstOrNull() ?: "",
                            email = extracted.emails.firstOrNull() ?: "",
                            emails = extracted.emails.joinToString("; "),
                            title = extracted.title,
                            description = extracted.description,
                            keywordsFound = scoreBreakdown.matchedKeywords.joinToString("; "),
                            keywordScore = scoreBreakdown.totalScore,
                            linkedin = extracted.socials["linkedin"] ?: "",
                            facebook = extracted.socials["facebook"] ?: "",
                            instagram = extracted.socials["instagram"] ?: "",
                            xTwitter = extracted.socials["x"] ?: "",
                            youtube = extracted.socials["youtube"] ?: "",
                            tiktok = extracted.socials["tiktok"] ?: "",
                            discoveredAt = System.currentTimeMillis(),
                            status = "validated",
                            source = "crawler"
                        )
                        repository.upsertLead(lead)
                        emitLog("LEAD+", "[${scoreBreakdown.confidence} ${scoreBreakdown.totalScore.toInt()}%] ${extracted.businessName} (Emails: ${extracted.emails.size}, Phones: ${extracted.phones.size})", isLead = true)

                        // Feature 2: Automated CRM Webhook Sync Trigger
                        val whConfig = webhookConfigSupplier?.invoke()
                        if (whConfig != null && whConfig.autoSyncEnabled && whConfig.url.isNotBlank()) {
                            if (lead.keywordScore >= whConfig.threshold.minScore) {
                                scope.launch(Dispatchers.IO) {
                                    val log = com.example.leadforge.webhook.WebhookDispatcher.dispatch(whConfig, lead)
                                    repository.logWebhookDelivery(log)
                                    repository.logAudit(
                                        event = "WEBHOOK_AUTOSYNC",
                                        detail = "Auto-synced ${lead.domain} to webhook: HTTP ${log.httpCode} (${log.latencyMs}ms)",
                                        level = if (log.isSuccess) "INFO" else "WARNING",
                                        jobId = jobId
                                    )
                                }
                            }
                        }
                    }

                    // Enqueue discovered child links from same domain if depth allows
                    if (currentDepth < config.maxDepth) {
                        for (link in extracted.links) {
                            if (SsrfValidator.isSameDomain(link, validatedTarget) && discoveredSet.add(link)) {
                                urlQueue.add(Pair(link, currentDepth + 1))
                            }
                        }
                    }

                    // Update job progress in database
                    repository.updateJob(
                        jobEntity.copy(
                            id = jobId,
                            pagesDiscovered = discoveredSet.size,
                            pagesProcessed = pagesProcessedCount,
                            leadsFound = leadsFoundCount,
                            errorCount = errorsCount
                        )
                    )
                }

                // Final status
                val finalStatus = if (isCancelled.get()) JobStatus.CANCELLED else JobStatus.COMPLETED
                _telemetry.value = _telemetry.value.copy(
                    status = finalStatus,
                    pagesProcessed = pagesProcessedCount,
                    leadsFound = leadsFoundCount,
                    errorsCount = errorsCount
                )

                repository.updateJob(
                    jobEntity.copy(
                        id = jobId,
                        status = finalStatus.name,
                        finishedAt = System.currentTimeMillis(),
                        pagesDiscovered = discoveredSet.size,
                        pagesProcessed = pagesProcessedCount,
                        leadsFound = leadsFoundCount,
                        errorCount = errorsCount
                    )
                )

                emitLog("COMPLETE", "Operation $finalStatus: $pagesProcessedCount pages processed, $leadsFoundCount leads secured.")
                repository.logAudit("JOB_FINISHED", "Job #$jobId finished as $finalStatus with $leadsFoundCount leads", "INFO", jobId)

            } catch (e: CancellationException) {
                emitLog("ABORT", "Operation was cancelled by user", isError = true)
                _telemetry.value = _telemetry.value.copy(status = JobStatus.CANCELLED)
            } catch (e: Exception) {
                emitLog("ERROR", "Unhandled operation error: ${e.message}", isError = true)
                repository.logAudit("JOB_ERROR", "Job #$jobId failed: ${e.message}", "ERROR", jobId)
                _telemetry.value = _telemetry.value.copy(status = JobStatus.FAILED)
            }
        }
    }

    /**
     * Executes an instant live recon probe of a single target URL.
     * Returns real, honest parsed data immediately for testing.
     */
    suspend fun executeSingleProbe(targetUrl: String): ProbeResult = withContext(Dispatchers.IO) {
        val validation = SsrfValidator.validate(targetUrl)
        if (validation !is ValidationResult.Valid) {
            val reason = (validation as ValidationResult.Rejected).reason
            return@withContext ProbeResult(
                url = targetUrl,
                statusCode = 0,
                elapsedMs = 0,
                error = "SSRF Shield Blocked: $reason"
            )
        }

        val fetchResult = fetchPage(validation.canonicalUrl)
        if (fetchResult.error != null) {
            return@withContext ProbeResult(
                url = validation.canonicalUrl,
                statusCode = fetchResult.statusCode,
                elapsedMs = fetchResult.elapsedMs,
                error = fetchResult.error
            )
        }

        val extracted = DataExtractor.extract(fetchResult.text, validation.canonicalUrl, includeRoleEmails = true)
        ProbeResult(
            url = validation.canonicalUrl,
            statusCode = fetchResult.statusCode,
            elapsedMs = fetchResult.elapsedMs,
            data = extracted
        )
    }

    fun pauseOperation() {
        if (_telemetry.value.status == JobStatus.RUNNING) {
            isPaused.set(true)
            _telemetry.value = _telemetry.value.copy(status = JobStatus.PAUSED)
            emitLog("CONTROL", "Operation paused")
        }
    }

    fun resumeOperation() {
        if (_telemetry.value.status == JobStatus.PAUSED) {
            isPaused.set(false)
            _telemetry.value = _telemetry.value.copy(status = JobStatus.RUNNING)
            emitLog("CONTROL", "Operation resumed")
        }
    }

    fun cancelOperation() {
        isCancelled.set(true)
        isPaused.set(false)
        currentExecutionJob?.cancel()
        _telemetry.value = _telemetry.value.copy(status = JobStatus.CANCELLED)
        emitLog("CONTROL", "Operation cancel requested")
    }

    private suspend fun fetchPage(url: String): FetchResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(url)
                // Use standard modern browser headers so public servers accept requests without 403
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36 (LeadForge-X Research Agent)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val elapsed = System.currentTimeMillis() - start
                val code = response.code
                val cType = response.header("Content-Type", "") ?: ""

                // Bounded stream read to prevent memory exhaustion (max 5MB)
                val bodyStream = response.body?.byteStream()
                val bodyBytes = if (bodyStream != null) {
                    val buffer = ByteArray(8192)
                    val baos = ByteArrayOutputStream()
                    var totalRead = 0
                    var bytesRead: Int
                    while (bodyStream.read(buffer).also { bytesRead = it } != -1) {
                        baos.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalRead >= 5_000_000) break
                    }
                    baos.toByteArray()
                } else {
                    ByteArray(0)
                }

                // Detect character encoding
                val charset = try {
                    val charsetName = cType.substringAfter("charset=", "").substringBefore(";").trim('"', ' ')
                    if (charsetName.isNotEmpty()) Charset.forName(charsetName) else Charsets.UTF_8
                } catch (e: Exception) {
                    Charsets.UTF_8
                }

                val bodyStr = String(bodyBytes, charset)
                val blocked = DataExtractor.isBlockedPage(bodyStr, code)

                FetchResult(
                    url = url,
                    finalUrl = response.request.url.toString(),
                    statusCode = code,
                    text = bodyStr,
                    contentType = cType,
                    blocked = blocked,
                    elapsedMs = elapsed
                )
            }
        } catch (e: Exception) {
            FetchResult(
                url = url,
                finalUrl = url,
                statusCode = 0,
                text = "",
                contentType = "",
                elapsedMs = System.currentTimeMillis() - start,
                error = "${e.javaClass.simpleName}: ${e.message ?: "Network error"}"
            )
        }
    }

    private fun emitLog(tag: String, message: String, isError: Boolean = false, isSecurity: Boolean = false, isLead: Boolean = false) {
        scope.launch {
            _events.emit(
                OperationEvent(
                    tag = tag,
                    message = message,
                    isError = isError,
                    isSecurity = isSecurity,
                    isLead = isLead
                )
            )
        }
    }
}
