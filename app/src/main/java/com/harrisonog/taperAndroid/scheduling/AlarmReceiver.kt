package com.harrisonog.taperAndroid.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.harrisonog.taperAndroid.data.db.AppDatabase
import com.harrisonog.taperAndroid.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * BroadcastReceiver that handles alarm broadcasts and displays notifications.
 * Used by AlarmManagerScheduler as a fallback scheduling mechanism.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: return
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)

        if (habitId == -1L || eventId == -1L) return

        // Show the notification
        NotificationHelper.showNotification(
            context = context,
            habitName = habitName,
            message = message,
            habitId = habitId,
            eventId = eventId,
        )

        // Update the database in a coroutine
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val event = database.habitEventDao().getById(eventId)
                if (event != null) {
                    val updatedEvent = event.copy(sentAt = Instant.now())
                    database.habitEventDao().update(updatedEvent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
