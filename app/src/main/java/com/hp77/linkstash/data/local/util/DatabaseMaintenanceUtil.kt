package com.hp77.linkstash.data.local.util

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.*
import com.hp77.linkstash.data.local.LinkStashDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseMaintenanceUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: LinkStashDatabase
) {
    companion object {
        private const val VACUUM_WORK_NAME = "database_vacuum_work"
        private const val VACUUM_INTERVAL_HOURS = 24L // Run daily
    }

    /**
     * Schedule periodic database maintenance tasks
     */
    fun scheduleMaintenance() {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true) // Run only when device is idle
            .setRequiresBatteryNotLow(true) // Don't run on low battery
            .build()

        val vacuumWorkRequest = PeriodicWorkRequestBuilder<DatabaseMaintenanceWorker>(
            VACUUM_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                VACUUM_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                vacuumWorkRequest
            )
    }

    /**
     * Run maintenance tasks immediately
     */
    suspend fun performMaintenance() {
        (database as RoomDatabase).runInTransaction {
            val db = (database as RoomDatabase).openHelper.writableDatabase
            // Run VACUUM to reclaim space and defragment
            db.query("VACUUM").close()
            // Analyze tables for query optimization
            db.query("ANALYZE").close()
        }
    }
}

/**
 * WorkManager worker class to perform database maintenance
 */
@HiltWorker
class DatabaseMaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: LinkStashDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            (database as RoomDatabase).runInTransaction {
                val db = database.openHelper.writableDatabase
                db.query("VACUUM").close()
                db.query("ANALYZE").close()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
