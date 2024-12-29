package com.hp77.linkstash

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hp77.linkstash.data.local.util.DatabaseMaintenanceUtil
import com.hp77.linkstash.util.CrashReporter
import com.hp77.linkstash.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LinkStashApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var databaseMaintenanceUtil: DatabaseMaintenanceUtil

    override fun onCreate() {
        super.onCreate()
        Log.d("LinkStashApplication", "Initializing application")
        CrashReporter.initialize(this)
        createNotificationChannel()
        Log.d("LinkStashApplication", "Notification channel created")
        
        // Initialize database maintenance
        databaseMaintenanceUtil.scheduleMaintenance()
        Log.d("LinkStashApplication", "Database maintenance scheduled")
    }

    // WorkManager configuration is now handled in ReminderModule
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannel() {
        Log.d("LinkStashApplication", "Creating notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "Link Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for link reminders"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
