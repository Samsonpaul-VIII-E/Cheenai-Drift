package com.example.leadforge.data

import com.example.leadforge.data.dao.AuditDao
import com.example.leadforge.data.dao.BatchQueueDao
import com.example.leadforge.data.dao.JobDao
import com.example.leadforge.data.dao.LeadDao
import com.example.leadforge.data.dao.PageDao
import com.example.leadforge.data.dao.WebhookLogDao
import com.example.leadforge.data.entity.AuditEntity
import com.example.leadforge.data.entity.JobEntity
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.data.entity.PageEntity
import com.example.leadforge.data.entity.WebhookLogEntity
import com.example.leadforge.model.WebhookDeliveryLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LeadForgeRepository(
    private val leadDao: LeadDao,
    private val jobDao: JobDao,
    private val pageDao: PageDao,
    private val auditDao: AuditDao,
    private val batchQueueDao: BatchQueueDao,
    private val webhookLogDao: WebhookLogDao
) {
    val allLeads: Flow<List<LeadEntity>> = leadDao.getAllLeads()
    val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobs()
    val activeJob: Flow<JobEntity?> = jobDao.getActiveJobFlow()
    val leadCount: Flow<Int> = leadDao.getLeadCount()
    val visitedCount: Flow<Int> = pageDao.getVisitedCount()
    val recentAudits: Flow<List<AuditEntity>> = auditDao.getRecentAudit()

    val recentWebhookLogs: Flow<List<WebhookDeliveryLog>> = webhookLogDao.getRecentLogs().map { entities ->
        entities.map { entity ->
            WebhookDeliveryLog(
                id = entity.id,
                timestamp = entity.timestamp,
                endpointUrl = entity.endpointUrl,
                targetDomain = entity.targetDomain,
                leadName = entity.leadName,
                leadScore = entity.leadScore,
                httpCode = entity.httpCode,
                isSuccess = entity.isSuccess,
                latencyMs = entity.latencyMs,
                retryCount = entity.retryCount,
                errorMessage = entity.errorMessage,
                payloadPreview = entity.payloadPreview
            )
        }
    }

    fun searchLeads(query: String): Flow<List<LeadEntity>> = leadDao.searchLeads(query)

    suspend fun upsertLead(lead: LeadEntity) = leadDao.upsertLead(lead)
    suspend fun upsertLeads(leads: List<LeadEntity>) = leadDao.upsertLeads(leads)
    suspend fun deleteLead(lead: LeadEntity) = leadDao.deleteLead(lead)
    suspend fun clearAllLeads() = leadDao.clearAllLeads()

    suspend fun createJob(job: JobEntity): Long = jobDao.insertJob(job)
    suspend fun updateJob(job: JobEntity) = jobDao.updateJob(job)
    suspend fun getJobById(id: Long): JobEntity? = jobDao.getJobById(id)
    suspend fun deleteJob(job: JobEntity) = jobDao.deleteJob(job)
    suspend fun clearAllJobs() = jobDao.clearAllJobs()

    suspend fun hasVisited(url: String): Boolean = pageDao.hasVisited(url)
    suspend fun recordVisited(page: PageEntity) = pageDao.insertPage(page)
    suspend fun clearAllPages() = pageDao.clearAllPages()

    suspend fun logAudit(event: String, detail: String, level: String = "INFO", jobId: Long? = null) {
        auditDao.insertAudit(
            AuditEntity(
                timestamp = System.currentTimeMillis(),
                jobId = jobId,
                event = event,
                detail = detail,
                level = level
            )
        )
    }

    suspend fun clearAudit() = auditDao.clearAudit()

    suspend fun logWebhookDelivery(log: WebhookDeliveryLog) {
        webhookLogDao.insertLog(
            WebhookLogEntity(
                timestamp = log.timestamp,
                endpointUrl = log.endpointUrl,
                targetDomain = log.targetDomain,
                leadName = log.leadName,
                leadScore = log.leadScore,
                httpCode = log.httpCode,
                isSuccess = log.isSuccess,
                latencyMs = log.latencyMs,
                retryCount = log.retryCount,
                errorMessage = log.errorMessage,
                payloadPreview = log.payloadPreview
            )
        )
    }

    suspend fun clearWebhookLogs() = webhookLogDao.clearLogs()

    suspend fun seedInitialDataIfEmpty() {
        // Handled in ViewModel
    }
}
