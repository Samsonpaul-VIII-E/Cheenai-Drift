package com.example.leadforge.model

data class ScrapeConfig(
    val targetUrl: String = "",
    val maxPages: Int = 50,
    val maxDepth: Int = 2,
    val concurrency: Int = 6,
    val requestsPerSecond: Double = 3.0,
    val timeoutSeconds: Int = 15,
    val respectRobots: Boolean = true,
    val enrichWebsites: Boolean = true,
    val extractEmails: Boolean = true,
    val extractPhones: Boolean = true,
    val extractSocials: Boolean = true,
    val includeRoleEmails: Boolean = true,
    val defaultPhoneRegion: String = "US",
    val keywords: List<String> = emptyList()
)

data class FetchResult(
    val url: String,
    val finalUrl: String,
    val statusCode: Int,
    val text: String,
    val contentType: String,
    val fromCache: Boolean = false,
    val blocked: Boolean = false,
    val elapsedMs: Long = 0,
    val error: String? = null
)

data class ExtractedData(
    val title: String = "",
    val description: String = "",
    val text: String = "",
    val businessName: String = "",
    val emails: List<String> = emptyList(),
    val phones: List<String> = emptyList(),
    val phoneE164: List<String> = emptyList(),
    val socials: Map<String, String> = emptyMap(),
    val address: String = "",
    val links: List<String> = emptyList()
)

data class ScoreBreakdown(
    val totalScore: Double,
    val emailSignal: Double,
    val phoneSignal: Double,
    val websiteSignal: Double,
    val socialSignal: Double,
    val keywordSignal: Double,
    val addressSignal: Double,
    val matchedKeywords: List<String>,
    val confidence: String
)

enum class JobStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    CANCELLED,
    COMPLETED,
    FAILED
}

enum class AuditLevel {
    INFO,
    WARNING,
    ERROR,
    SECURITY
}

// ==========================================
// FEATURE 1: BULK INGESTION & BATCH QUEUE
// ==========================================

enum class BatchItemStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class BatchQueueItem(
    val id: Long = 0,
    val domain: String,
    val originalUrl: String,
    val status: BatchItemStatus = BatchItemStatus.PENDING,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val httpStatus: Int? = null,
    val latencyMs: Long? = null,
    val leadsFound: Int = 0,
    val errorMessage: String? = null
)

data class BatchPreflightResult(
    val rawCount: Int = 0,
    val validUrls: List<String> = emptyList(),
    val duplicatesRemoved: Int = 0,
    val ssrfBlockedCount: Int = 0,
    val sampleValidDomains: List<String> = emptyList()
)

data class BatchQueueTelemetry(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val failedItems: Int = 0,
    val currentItemIndex: Int = 0,
    val currentTargetDomain: String = "",
    val averageLatencyMs: Long = 0,
    val elapsedSeconds: Long = 0,
    val domainsPerMinute: Double = 0.0
)

// ==========================================
// FEATURE 2: CRM WEBHOOK ENGINE
// ==========================================

enum class WebhookTierThreshold(val label: String, val minScore: Double) {
    TIER_A_ONLY("Tier A Only (≥90%)", 90.0),
    TIERS_A_AND_B("Tiers A & B (≥70%)", 70.0),
    ALL_LEADS("All Leads (≥0%)", 0.0)
}

data class WebhookConfig(
    val url: String = "",
    val bearerToken: String = "",
    val threshold: WebhookTierThreshold = WebhookTierThreshold.TIERS_A_AND_B,
    val autoSyncEnabled: Boolean = true
)

data class WebhookDeliveryLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val endpointUrl: String,
    val targetDomain: String,
    val leadName: String,
    val leadScore: Double,
    val httpCode: Int,
    val isSuccess: Boolean,
    val latencyMs: Long,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val payloadPreview: String = ""
)

data class WebhookTestResult(
    val isSuccess: Boolean,
    val httpCode: Int,
    val latencyMs: Long,
    val message: String
)
