package com.example.leadforge.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leads",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["domain"]),
        Index(value = ["keywordScore"])
    ]
)
data class LeadEntity(
    @PrimaryKey val fingerprint: String,
    val url: String,
    val domain: String,
    val name: String,
    val category: String = "",
    val address: String = "",
    val locality: String = "",
    val region: String = "",
    val country: String = "",
    val postalCode: String = "",
    val phone: String = "",
    val phoneE164: String = "",
    val email: String = "",
    val emails: String = "",
    val contactName: String = "",
    val rating: String = "",
    val reviewCount: String = "",
    val title: String = "",
    val description: String = "",
    val keywordsFound: String = "",
    val keywordScore: Double = 0.0,
    val linkedin: String = "",
    val facebook: String = "",
    val instagram: String = "",
    val xTwitter: String = "",
    val youtube: String = "",
    val tiktok: String = "",
    val discoveredAt: Long = System.currentTimeMillis(),
    val status: String = "validated",
    val source: String = "crawler",
    val notes: String = ""
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUrl: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val pagesDiscovered: Int = 0,
    val pagesProcessed: Int = 0,
    val leadsFound: Int = 0,
    val errorCount: Int = 0,
    val configJson: String = "",
    val lastError: String? = null
)

@Entity(
    tableName = "pages",
    indices = [
        Index(value = ["jobId"]),
        Index(value = ["url"], unique = true)
    ]
)
data class PageEntity(
    @PrimaryKey val url: String,
    val jobId: Long,
    val fetchedAt: Long = System.currentTimeMillis(),
    val statusCode: Int,
    val title: String = "",
    val bodyHash: String = "",
    val contentType: String = "",
    val elapsedMs: Long = 0
)

@Entity(
    tableName = "audit_events",
    indices = [Index(value = ["timestamp"])]
)
data class AuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val jobId: Long? = null,
    val event: String,
    val detail: String = "",
    val level: String = "INFO"
)

@Entity(
    tableName = "batch_queue_items",
    indices = [
        Index(value = ["domain"]),
        Index(value = ["status"])
    ]
)
data class BatchQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val originalUrl: String,
    val status: String = "PENDING",
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val httpStatus: Int? = null,
    val latencyMs: Long? = null,
    val leadsFound: Int = 0,
    val errorMessage: String? = null
)

@Entity(
    tableName = "webhook_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["isSuccess"])
    ]
)
data class WebhookLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
