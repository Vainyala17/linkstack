package com.hp77.linkstash.util

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderManager @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager
) {

    fun scheduleReminder(link: Link) {
        link.reminderTime?.let { reminderTime ->
            val currentTime = System.currentTimeMillis()
            val delay = reminderTime - currentTime
            Log.d("ReminderManager", """
                Scheduling reminder:
                - Link ID: ${link.id}
                - Title: ${link.title}
                - Current time: $currentTime
                - Reminder time: $reminderTime
                - Calculated delay: $delay ms
                - Formatted reminder time: ${DateUtils.formatDateTime(reminderTime)}
            """.trimIndent())

            if (delay <= 0) {
                Log.w("ReminderManager", "Skipping reminder as delay is not positive")
                return@let
            }

            if (delay > 2_592_000_000) { // 30 days in milliseconds
                Log.w("ReminderManager", "Delay is more than 30 days, this might be unreliable on some devices")
            }

            val inputData = Data.Builder()
                .putString(ReminderWorker.LINK_ID_KEY, link.id)
                .putString(ReminderWorker.LINK_TITLE_KEY, link.title)
                .putString(ReminderWorker.LINK_URL_KEY, link.url)
                .build()

            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            val result = workManager.enqueueUniqueWork(
                "reminder_${link.id}",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
            Log.d("ReminderManager", "Reminder work request enqueued with ID: ${reminderWork.id}")
            
            // Log the work request details
            Log.d("ReminderManager", """
                Work request details:
                - Work ID: ${reminderWork.id}
                - Initial delay: ${reminderWork.workSpec.initialDelay} ms
                - Input data: ${inputData.keyValueMap}
            """.trimIndent())
        }
    }

    fun cancelReminder(linkId: String) {
        Log.d("ReminderManager", "Cancelling reminder for link: $linkId")
        workManager.cancelUniqueWork("reminder_${linkId}")
        Log.d("ReminderManager", "Cancellation request sent for reminder_$linkId")
    }

    fun observeReminderStatus(linkId: String): Flow<WorkInfo.State> {
        Log.d("ReminderManager", "Starting to observe reminder status for link: $linkId")
        return workManager.getWorkInfosForUniqueWorkFlow("reminder_${linkId}")
            .map { workInfoList ->
                val state = workInfoList.firstOrNull()?.state ?: WorkInfo.State.CANCELLED
                Log.d("ReminderManager", "Current work state for link $linkId: $state")
                state
            }
    }
}
