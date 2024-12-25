package com.hp77.linkstash.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hp77.linkstash.util.Logger
import androidx.core.app.NotificationCompat
import com.hp77.linkstash.MainActivity
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hp77.linkstash.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val LINK_ID_KEY = "link_id"
        const val LINK_TITLE_KEY = "link_title"
        const val LINK_URL_KEY = "link_url"
        const val CHANNEL_ID = "link_reminders"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        Logger.d("ReminderWorker", "Starting doWork()")
        val linkId = inputData.getString(LINK_ID_KEY)
        val title = inputData.getString(LINK_TITLE_KEY)
        val url = inputData.getString(LINK_URL_KEY)

        Logger.d("ReminderWorker", "Received data - linkId: $linkId, title: $title, url: $url")

        if (linkId == null || url == null) {
            Logger.e("ReminderWorker", "Missing required data")
            return Result.failure()
        }

        createNotificationChannel()
        showNotification(title ?: "Link Reminder", url)

        Logger.d("ReminderWorker", "Work completed successfully")
        return Result.success()
    }

    private fun createNotificationChannel() {
        Logger.d("ReminderWorker", "Creating notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Link Reminders"
            val descriptionText = "Notifications for link reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH // Changed to HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Logger.d("ReminderWorker", "Notification channel created with importance: $importance")
        } else {
            Logger.d("ReminderWorker", "Skipping notification channel creation for older Android versions")
        }
    }

    private fun showNotification(title: String, url: String) {
        Logger.d("ReminderWorker", "Preparing to show notification - Title: $title, URL: $url")
        try {
            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data = android.net.Uri.parse(url)
            }
            Logger.d("ReminderWorker", "Created intent with URL: ${intent.data}")

            val pendingIntent = PendingIntent.getActivity(
                appContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Logger.d("ReminderWorker", "Created PendingIntent")

            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Reminder: $title")
                .setContentText("Time to revisit: $url")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            Logger.d("ReminderWorker", "Built notification with high priority")

            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            Logger.d("ReminderWorker", "Notification sent successfully")
        } catch (e: Exception) {
            Logger.e("ReminderWorker", "Error showing notification", e)
        }
    }
}
