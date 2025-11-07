package com.harrisonog.taperAndroid.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.harrisonog.taperAndroid.data.db.AppDatabase
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.scheduling.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.Instant

/**
 * BroadcastReceiver that handles notification action button clicks.
 * Supports three actions: Completed, Snooze (15 min), and Deny.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return

        if (eventId == -1L || habitId == -1L || notificationId == -1) return

        // Dismiss the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)

        // Handle the action in a coroutine
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val event = database.habitEventDao().getById(eventId)

                if (event != null) {
                    when (action) {
                        ACTION_COMPLETED -> {
                            // Mark as completed
                            val updatedEvent = event.copy(
                                responseType = "completed",
                                respondedAt = Instant.now()
                            )
                            database.habitEventDao().update(updatedEvent)
                        }
                        ACTION_DENY -> {
                            // Mark as denied
                            val updatedEvent = event.copy(
                                responseType = "denied",
                                respondedAt = Instant.now()
                            )
                            database.habitEventDao().update(updatedEvent)
                        }
                        ACTION_SNOOZE -> {
                            // Mark current event as snoozed
                            val updatedEvent = event.copy(
                                responseType = "snoozed",
                                respondedAt = Instant.now()
                            )
                            database.habitEventDao().update(updatedEvent)

                            // Get habit details for scheduling
                            val habit = database.habitDao().observe(habitId).first()

                            if (habit != null) {
                                // Create a new snoozed event for 15 minutes later
                                val snoozeTime = Instant.now().plusSeconds(15 * 60)
                                val snoozedEvent = HabitEvent(
                                    habitId = habitId,
                                    scheduledAt = snoozeTime,
                                    isSnoozed = true
                                )
                                database.habitEventDao().insertAll(listOf(snoozedEvent))

                                // Get the newly inserted event
                                val allEvents = database.habitEventDao().getAll()
                                val newSnoozeEvent = allEvents.filter {
                                    it.habitId == habitId &&
                                    it.isSnoozed &&
                                    it.scheduledAt == snoozeTime
                                }.maxByOrNull { it.id }

                                // Schedule the snoozed alarm
                                if (newSnoozeEvent != null) {
                                    val scheduler = AlarmScheduler.create(context)
                                    scheduler.scheduleEvent(
                                        habitId = habitId,
                                        habitName = habit.name,
                                        message = habit.message,
                                        event = newSnoozeEvent
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_ACTION = "extra_action"

        const val ACTION_COMPLETED = "action_completed"
        const val ACTION_DENY = "action_deny"
        const val ACTION_SNOOZE = "action_snooze"
    }
}
