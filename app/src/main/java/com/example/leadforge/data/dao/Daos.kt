package com.example.leadforge.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.leadforge.data.entity.AuditEntity
import com.example.leadforge.data.entity.JobEntity
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.data.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY keywordScore DESC, discoveredAt DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE name LIKE '%' || :query || '%' OR domain LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR keywordsFound LIKE '%' || :query || '%' ORDER BY keywordScore DESC")
    fun searchLeads(query: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getLeadByFingerprint(fingerprint: String): LeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLead(lead: LeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeads(leads: List<LeadEntity>)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    @Query("DELETE FROM leads")
    suspend fun clearAllLeads()

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): JobEntity?

    @Query("SELECT * FROM jobs WHERE status = 'RUNNING' OR status = 'PAUSED' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveJobFlow(): Flow<JobEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Update
    suspend fun updateJob(job: JobEntity)

    @Delete
    suspend fun deleteJob(job: JobEntity)

    @Query("DELETE FROM jobs")
    suspend fun clearAllJobs()
}

@Dao
interface PageDao {
    @Query("SELECT EXISTS(SELECT 1 FROM pages WHERE url = :url LIMIT 1)")
    suspend fun hasVisited(url: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Query("SELECT * FROM pages WHERE jobId = :jobId ORDER BY fetchedAt DESC")
    suspend fun getPagesForJob(jobId: Long): List<PageEntity>

    @Query("SELECT COUNT(*) FROM pages")
    fun getVisitedCount(): Flow<Int>

    @Query("DELETE FROM pages")
    suspend fun clearAllPages()
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAudit(limit: Int = 100): Flow<List<AuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditEntity)

    @Query("DELETE FROM audit_events")
    suspend fun clearAudit()
}

@Dao
interface BatchQueueDao {
    @Query("SELECT * FROM batch_queue_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<com.example.leadforge.data.entity.BatchQueueEntity>>

    @Query("SELECT * FROM batch_queue_items WHERE status = 'PENDING' ORDER BY id ASC")
    suspend fun getPendingItems(): List<com.example.leadforge.data.entity.BatchQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<com.example.leadforge.data.entity.BatchQueueEntity>)

    @Update
    suspend fun updateItem(item: com.example.leadforge.data.entity.BatchQueueEntity)

    @Query("UPDATE batch_queue_items SET status = 'CANCELLED' WHERE status = 'PENDING' OR status = 'IN_PROGRESS'")
    suspend fun cancelActiveItems()

    @Query("DELETE FROM batch_queue_items")
    suspend fun clearQueue()

    @Query("DELETE FROM batch_queue_items WHERE status = 'COMPLETED' OR status = 'FAILED' OR status = 'CANCELLED'")
    suspend fun clearFinishedItems()
}

@Dao
interface WebhookLogDao {
    @Query("SELECT * FROM webhook_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<com.example.leadforge.data.entity.WebhookLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: com.example.leadforge.data.entity.WebhookLogEntity)

    @Query("DELETE FROM webhook_logs")
    suspend fun clearLogs()
}
