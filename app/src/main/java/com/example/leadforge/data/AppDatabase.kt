package com.example.leadforge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.leadforge.data.dao.AuditDao
import com.example.leadforge.data.dao.JobDao
import com.example.leadforge.data.dao.LeadDao
import com.example.leadforge.data.dao.PageDao
import com.example.leadforge.data.entity.AuditEntity
import com.example.leadforge.data.entity.JobEntity
import com.example.leadforge.data.entity.LeadEntity
import com.example.leadforge.data.entity.PageEntity

@Database(
    entities = [
        LeadEntity::class,
        JobEntity::class,
        PageEntity::class,
        AuditEntity::class,
        com.example.leadforge.data.entity.BatchQueueEntity::class,
        com.example.leadforge.data.entity.WebhookLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun jobDao(): JobDao
    abstract fun pageDao(): PageDao
    abstract fun auditDao(): AuditDao
    abstract fun batchQueueDao(): com.example.leadforge.data.dao.BatchQueueDao
    abstract fun webhookLogDao(): com.example.leadforge.data.dao.WebhookLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "leadforge_x.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
