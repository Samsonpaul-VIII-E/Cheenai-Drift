package com.example.leadforge.webhook

import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.model.WebhookConfig
import com.example.leadforge.model.WebhookDeliveryLog
import com.example.leadforge.model.WebhookTestResult
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object WebhookDispatcher {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Formats a lead entity into a standardized enterprise CRM JSON payload.
     */
    fun buildLeadPayload(lead: LeadEntity): String {
        val root = JSONObject()
        root.put("event", "lead.extracted")
        root.put("timestamp", System.currentTimeMillis())
        root.put("fingerprint", lead.fingerprint)
        root.put("domain", lead.domain)
        root.put("source_url", lead.url)
        root.put("company_name", lead.name.ifBlank { lead.domain })
        root.put("category", lead.category)
        root.put("score", lead.keywordScore)
        root.put("tier", when {
            lead.keywordScore >= 90.0 -> "TIER_A"
            lead.keywordScore >= 70.0 -> "TIER_B"
            lead.keywordScore >= 45.0 -> "TIER_C"
            else -> "TIER_D"
        })

        // Contact info
        root.put("primary_email", lead.email)
        val emailsArray = JSONArray()
        lead.emails.split(";").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            emailsArray.put(it)
        }
        if (emailsArray.length() == 0 && lead.email.isNotBlank()) {
            emailsArray.put(lead.email)
        }
        root.put("emails", emailsArray)

        root.put("primary_phone", lead.phone)
        root.put("phone_e164", lead.phoneE164)
        root.put("contact_name", lead.contactName)

        // Location
        val locationObj = JSONObject()
        locationObj.put("full_address", lead.address)
        locationObj.put("locality", lead.locality)
        locationObj.put("region", lead.region)
        locationObj.put("country", lead.country)
        locationObj.put("postal_code", lead.postalCode)
        root.put("location", locationObj)

        // Social profiles
        val socialsObj = JSONObject()
        if (lead.linkedin.isNotBlank()) socialsObj.put("linkedin", lead.linkedin)
        if (lead.xTwitter.isNotBlank()) socialsObj.put("twitter", lead.xTwitter)
        if (lead.facebook.isNotBlank()) socialsObj.put("facebook", lead.facebook)
        if (lead.instagram.isNotBlank()) socialsObj.put("instagram", lead.instagram)
        if (lead.youtube.isNotBlank()) socialsObj.put("youtube", lead.youtube)
        root.put("social_profiles", socialsObj)

        // Intelligence summary
        root.put("title", lead.title)
        root.put("description", lead.description)
        val kwArray = JSONArray()
        lead.keywordsFound.split(";").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            kwArray.put(it)
        }
        root.put("matched_keywords", kwArray)
        root.put("discovered_at", lead.discoveredAt)
        root.put("verification_status", lead.status)
        root.put("source", "LeadForge X Dark Command Center")

        return root.toString(2)
    }

    /**
     * Dispatches a lead JSON payload to the configured webhook endpoint with exponential backoff.
     */
    suspend fun dispatch(config: WebhookConfig, lead: LeadEntity): WebhookDeliveryLog = withContext(Dispatchers.IO) {
        val payload = buildLeadPayload(lead)
        val startTime = System.currentTimeMillis()

        if (config.url.isBlank()) {
            return@withContext WebhookDeliveryLog(
                endpointUrl = "(unconfigured)",
                targetDomain = lead.domain,
                leadName = lead.name,
                leadScore = lead.keywordScore,
                httpCode = 0,
                isSuccess = false,
                latencyMs = 0,
                errorMessage = "Webhook URL is blank",
                payloadPreview = payload.take(300)
            )
        }

        var attempts = 0
        var lastCode = 0
        var lastError: String? = null
        var isSuccess = false
        val maxAttempts = 3

        while (attempts < maxAttempts) {
            attempts++
            try {
                val requestBuilder = Request.Builder()
                    .url(config.url)
                    .header("User-Agent", "LeadForge-Webhook-Engine/2.0")
                    .header("Content-Type", "application/json")
                    .header("X-LeadForge-Event", "lead.extracted")

                if (config.bearerToken.isNotBlank()) {
                    val token = config.bearerToken.trim()
                    val authHeader = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    requestBuilder.header("Authorization", authHeader)
                }

                val body = payload.toRequestBody(jsonMediaType)
                requestBuilder.post(body)

                httpClient.newCall(requestBuilder.build()).execute().use { response ->
                    lastCode = response.code
                    if (response.isSuccessful) {
                        isSuccess = true
                        return@withContext WebhookDeliveryLog(
                            endpointUrl = config.url,
                            targetDomain = lead.domain,
                            leadName = lead.name,
                            leadScore = lead.keywordScore,
                            httpCode = lastCode,
                            isSuccess = true,
                            latencyMs = System.currentTimeMillis() - startTime,
                            retryCount = attempts - 1,
                            errorMessage = null,
                            payloadPreview = payload.take(300)
                        )
                    } else {
                        lastError = "HTTP $lastCode: ${response.message.ifBlank { "Server response error" }}"
                        // Only retry on 5xx server errors
                        if (lastCode in 500..599 && attempts < maxAttempts) {
                            delay((attempts * 1000L)) // 1s, 2s backoff
                        } else {
                            // 4xx client errors should not be retried blindly
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                lastError = e.message ?: "Network I/O failure"
                if (attempts < maxAttempts) {
                    delay((attempts * 1000L))
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Unexpected dispatch error"
                break
            }
        }

        WebhookDeliveryLog(
            endpointUrl = config.url,
            targetDomain = lead.domain,
            leadName = lead.name,
            leadScore = lead.keywordScore,
            httpCode = lastCode,
            isSuccess = isSuccess,
            latencyMs = System.currentTimeMillis() - startTime,
            retryCount = attempts - 1,
            errorMessage = lastError,
            payloadPreview = payload.take(300)
        )
    }

    /**
     * Dispatches an immediate test ping payload to verify webhook connectivity.
     */
    suspend fun sendTestPing(config: WebhookConfig): WebhookTestResult = withContext(Dispatchers.IO) {
        if (config.url.isBlank()) {
            return@withContext WebhookTestResult(
                isSuccess = false,
                httpCode = 0,
                latencyMs = 0,
                message = "Webhook URL cannot be empty."
            )
        }

        val testPayload = JSONObject().apply {
            put("event", "leadforge.test_ping")
            put("timestamp", System.currentTimeMillis())
            put("status", "diagnostic_test")
            put("message", "LeadForge X CRM Webhook Sync Engine test ping")
            put("sample_lead", JSONObject().apply {
                put("domain", "wikimediafoundation.org")
                put("company_name", "Wikimedia Foundation")
                put("tier", "TIER_A")
                put("score", 96.0)
                put("primary_email", "press@wikimedia.org")
                put("location", "San Francisco, CA")
            })
        }.toString(2)

        val start = System.currentTimeMillis()
        try {
            val requestBuilder = Request.Builder()
                .url(config.url)
                .header("User-Agent", "LeadForge-Webhook-Engine/2.0 (Diagnostic Ping)")
                .header("Content-Type", "application/json")
                .header("X-LeadForge-Event", "leadforge.test_ping")

            if (config.bearerToken.isNotBlank()) {
                val token = config.bearerToken.trim()
                val authHeader = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                requestBuilder.header("Authorization", authHeader)
            }

            val body = testPayload.toRequestBody(jsonMediaType)
            requestBuilder.post(body)

            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val elapsed = System.currentTimeMillis() - start
                val code = response.code
                if (response.isSuccessful) {
                    WebhookTestResult(
                        isSuccess = true,
                        httpCode = code,
                        latencyMs = elapsed,
                        message = "Connection Verified: HTTP $code OK (${elapsed}ms)"
                    )
                } else {
                    WebhookTestResult(
                        isSuccess = false,
                        httpCode = code,
                        latencyMs = elapsed,
                        message = "Endpoint returned HTTP $code: ${response.message}"
                    )
                }
            }
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            WebhookTestResult(
                isSuccess = false,
                httpCode = 0,
                latencyMs = elapsed,
                message = "Connection Failed: ${e.message ?: "Unknown network error"}"
            )
        }
    }
}
