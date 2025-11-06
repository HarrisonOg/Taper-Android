package com.harrisonog.taperAndroid.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harrisonog.taperAndroid.data.db.AppDatabase
import com.harrisonog.taperAndroid.notifications.NotificationHelper
import java.time.Instant

/**
 * WorkManager worker that displays notifications for habit events.
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val habitId = inputData.getLong(KEY_HABIT_ID, -1)
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val eventId = inputData.getLong(KEY_EVENT_ID, -1)

        if (habitId == -1L || eventId == -1L) {
            return Result.failure()
        }

        // Show the notification
        NotificationHelper.showNotification(
            context = applicationContext,
            habitName = habitName,
            message = message,
            eventId = eventId,
        )

        // Update the event in the database to mark it as sent
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val event = database.habitEventDao().getById(eventId)
            if (event != null) {
                val updatedEvent = event.copy(sentAt = Instant.now())
                database.habitEventDao().update(updatedEvent)
            }
        } catch (e: Exception) {
            // Log error but still return success since notification was shown
            e.printStackTrace()
        }

        return Result.success()
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_MESSAGE = "message"
        const val KEY_EVENT_ID = "event_id"
    }
}
