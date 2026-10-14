package com.example.leadforge.batch

import com.example.leadforge.data.LeadForgeRepository
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.engine.FastFingerprint
import com.example.leadforge.engine.FastScorer
import com.example.leadforge.engine.LeadForgeEngine
import com.example.leadforge.model.BatchItemStatus
import com.example.leadforge.model.BatchPreflightResult
import com.example.leadforge.model.BatchQueueItem
import com.example.leadforge.model.BatchQueueTelemetry
import com.example.leadforge.model.WebhookConfig
import com.example.leadforge.security.SsrfValidator
import com.example.leadforge.security.ValidationResult
import com.example.leadforge.webhook.WebhookDispatcher
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatchQueueProcessor(
    private val repository: LeadForgeRepository,
    private val engine: LeadForgeEngine,
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(BatchQueueTelemetry())
    val telemetry: StateFlow<BatchQueueTelemetry> = _telemetry.asStateFlow()

    private val _queueItems = MutableStateFlow<List<BatchQueueItem>>(emptyList())
    val queueItems: StateFlow<List<BatchQueueItem>> = _queueItems.asStateFlow()

    private var executionJob: Job? = null
    private val isPaused = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    /**
     * Pre-flight input sanitizer: parses raw multiline, comma-delimited, or CSV data,
     * strips formatting, normalizes domains, checks SSRF rules, and de-duplicates.
     */
    suspend fun preflightSanitize(
        rawInput: String,
        existingDomains: Set<String> = emptySet()
    ): BatchPreflightResult = withContext(Dispatchers.IO) {
        if (rawInput.isBlank()) {
            return@withContext BatchPreflightResult()
        }

        val rawTokens = rawInput.lines().flatMap { line ->
            // Support CSV lines: split by commas or tabs or semicolons
            line.split(",", ";", "\t")
        }.map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
         .filter { it.isNotBlank() }

        val seenDomains = mutableSetOf<String>()
        val validUrls = mutableListOf<String>()
        var duplicatesCount = 0
        var ssrfBlocked = 0

        for (token in rawTokens) {
            // Normalize token to full URL
            val candidate = when {
                token.startsWith("http://", ignoreCase = true) -> token
                token.startsWith("https://", ignoreCase = true) -> token
                else -> "https://$token"
            }

            // Extract host
            val host = try {
                val uri = URI(candidate)
                uri.host?.lowercase() ?: ""
            } catch (e: Exception) {
                ""
            }

            if (host.isBlank() || !host.contains(".")) {
                continue
            }

            // Deduplication check
            if (seenDomains.contains(host) || existingDomains.contains(host)) {
                duplicatesCount++
                continue
            }

            // SSRF Zero-Trust check
            val validation = SsrfValidator.validate(candidate)
            if (validation !is ValidationResult.Valid) {
                ssrfBlocked++
                continue
            }

            seenDomains.add(host)
            validUrls.add(validation.canonicalUrl)
        }

        BatchPreflightResult(
            rawCount = rawTokens.size,
            validUrls = validUrls,
            duplicatesRemoved = duplicatesCount,
            ssrfBlockedCount = ssrfBlocked,
            sampleValidDomains = seenDomains.take(6).toList()
        )
    }

    /**
     * Populates the queue with pre-flight validated targets.
     */
    fun enqueueTargets(targets: List<String>) {
        val items = targets.mapIndexed { idx, url ->
            val host = try { URI(url).host ?: url } catch (e: Exception) { url }
            BatchQueueItem(
                id = (idx + 1).toLong(),
                domain = host,
                originalUrl = url,
                status = BatchItemStatus.PENDING
            )
        }
        _queueItems.value = items
        _telemetry.value = BatchQueueTelemetry(
            isRunning = false,
            isPaused = false,
            totalItems = items.size,
            completedItems = 0,
            failedItems = 0,
            currentItemIndex = 0,
            currentTargetDomain = "",
            averageLatencyMs = 0
        )
    }

    /**
     * Executes the queue sequentially: one domain at a time with politeness delay.
     */
    fun startSequentialQueue(
        webhookConfig: WebhookConfig,
        keywords: List<String> = emptyList(),
        politenessDelayMs: Long = 800L
    ) {
        if (_telemetry.value.isRunning) return

        val items = _queueItems.value.toMutableList()
        if (items.isEmpty()) return

        isPaused.set(false)
        isCancelled.set(false)

        executionJob = scope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            var completedCount = 0
            var failedCount = 0
            var totalLatency = 0L

            _telemetry.value = _telemetry.value.copy(
                isRunning = true,
                isPaused = false,
                totalItems = items.size,
                completedItems = 0,
                failedItems = 0
            )

            repository.logAudit(
                event = "BATCH_QUEUE_LAUNCHED",
                detail = "Sequential processing initiated for ${items.size} domains (politeness delay: ${politenessDelayMs}ms)",
                level = "INFO"
            )

            for (i in items.indices) {
                if (isCancelled.get()) {
                    // Mark remaining as cancelled
                    for (rem in i until items.size) {
                        items[rem] = items[rem].copy(status = BatchItemStatus.CANCELLED)
                    }
                    _queueItems.value = items.toList()
                    break
                }

                // Check pause state
                while (isPaused.get() && !isCancelled.get()) {
                    delay(300)
                }

                if (isCancelled.get()) break

                val current = items[i]
                val itemStart = System.currentTimeMillis()

                items[i] = current.copy(
                    status = BatchItemStatus.IN_PROGRESS,
                    startedAt = itemStart
                )
                _queueItems.value = items.toList()

                _telemetry.value = _telemetry.value.copy(
                    currentItemIndex = i + 1,
                    currentTargetDomain = current.domain,
                    elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000L
                )

                try {
                    // 1. Probe & extract target data using engine's single probe
                    val probeResult = engine.executeSingleProbe(current.originalUrl)
                    val elapsed = System.currentTimeMillis() - itemStart
                    totalLatency += elapsed

                    var leadsFoundForItem = 0

                    if (probeResult.error != null) {
                        failedCount++
                        items[i] = items[i].copy(
                            status = BatchItemStatus.FAILED,
                            completedAt = System.currentTimeMillis(),
                            httpStatus = probeResult.statusCode,
                            latencyMs = elapsed,
                            errorMessage = probeResult.error
                        )
                    } else {
                        val extracted = probeResult.data
                        if (extracted != null) {
                            val scoreBreakdown = FastScorer.score(extracted, keywords)
                            val fp = FastFingerprint.compute(
                                extracted.businessName,
                                extracted.emails.firstOrNull() ?: "",
                                extracted.phones.firstOrNull() ?: "",
                                probeResult.url
                            )

                            val lead = LeadEntity(
                                fingerprint = fp,
                                url = probeResult.url,
                                domain = current.domain,
                                name = extracted.businessName.ifBlank { current.domain },
                                category = if (scoreBreakdown.matchedKeywords.isNotEmpty()) scoreBreakdown.matchedKeywords.first() else "Public Bulk Ingestion",
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
                                discoveredAt = System.currentTimeMillis(),
                                status = "validated",
                                source = "bulk_queue"
                            )

                            // Save to Room DB
                            repository.upsertLead(lead)
                            leadsFoundForItem = 1

                            // Feature 2: Automated CRM Webhook Dispatch Trigger
                            if (webhookConfig.autoSyncEnabled && webhookConfig.url.isNotBlank()) {
                                if (lead.keywordScore >= webhookConfig.threshold.minScore) {
                                    val log = WebhookDispatcher.dispatch(webhookConfig, lead)
                                    repository.logWebhookDelivery(log)
                                    repository.logAudit(
                                        event = "WEBHOOK_DISPATCH",
                                        detail = "Auto-synced ${lead.domain} to webhook: HTTP ${log.httpCode} (${log.latencyMs}ms, retry: ${log.retryCount})",
                                        level = if (log.isSuccess) "INFO" else "WARNING"
                                    )
                                }
                            }
                        }

                        completedCount++
                        items[i] = items[i].copy(
                            status = BatchItemStatus.COMPLETED,
                            completedAt = System.currentTimeMillis(),
                            httpStatus = probeResult.statusCode,
                            latencyMs = elapsed,
                            leadsFound = leadsFoundForItem,
                            errorMessage = null
                        )
                    }

                    _queueItems.value = items.toList()

                    val processedSoFar = (completedCount + failedCount).coerceAtLeast(1)
                    val avgLatency = totalLatency / processedSoFar
                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000L).coerceAtLeast(1)
                    val dpm = (processedSoFar.toDouble() / elapsedSec) * 60.0

                    _telemetry.value = _telemetry.value.copy(
                        completedItems = completedCount,
                        failedItems = failedCount,
                        averageLatencyMs = avgLatency,
                        elapsedSeconds = elapsedSec,
                        domainsPerMinute = dpm
                    )

                    // Politeness rate-limiting delay between sequential targets
                    if (i < items.size - 1 && !isCancelled.get()) {
                        delay(politenessDelayMs)
                    }

                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    failedCount++
                    val elapsed = System.currentTimeMillis() - itemStart
                    items[i] = items[i].copy(
                        status = BatchItemStatus.FAILED,
                        completedAt = System.currentTimeMillis(),
                        latencyMs = elapsed,
                        errorMessage = e.message ?: "Unknown error"
                    )
                    _queueItems.value = items.toList()
                }
            }

            val finalElapsed = ((System.currentTimeMillis() - startTime) / 1000L).coerceAtLeast(1)
            _telemetry.value = _telemetry.value.copy(
                isRunning = false,
                isPaused = false,
                currentTargetDomain = "",
                elapsedSeconds = finalElapsed
            )

            repository.logAudit(
                event = "BATCH_QUEUE_COMPLETED",
                detail = "Batch complete: $completedCount succeeded, $failedCount failed in ${finalElapsed}s",
                level = "INFO"
            )
        }
    }

    fun pauseQueue() {
        if (_telemetry.value.isRunning) {
            isPaused.set(true)
            _telemetry.value = _telemetry.value.copy(isPaused = true)
        }
    }

    fun resumeQueue() {
        if (_telemetry.value.isPaused) {
            isPaused.set(false)
            _telemetry.value = _telemetry.value.copy(isPaused = false)
        }
    }

    fun cancelQueue() {
        isCancelled.set(true)
        isPaused.set(false)
        executionJob?.cancel()
        _telemetry.value = _telemetry.value.copy(isRunning = false, isPaused = false)

        val updated = _queueItems.value.map {
            if (it.status == BatchItemStatus.PENDING || it.status == BatchItemStatus.IN_PROGRESS) {
                it.copy(status = BatchItemStatus.CANCELLED)
            } else it
        }
        _queueItems.value = updated
    }

    fun clearQueue() {
        if (!_telemetry.value.isRunning) {
            _queueItems.value = emptyList()
            _telemetry.value = BatchQueueTelemetry()
        }
    }
}
