package com.harrisonog.taperAndroid.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.harrisonog.taperAndroid.data.db.HabitEvent
import java.util.concurrent.TimeUnit

/**
 * Interface for scheduling exact alarms for habit events.
 */
interface AlarmScheduler {
    /**
     * Schedule a notification for a habit event.
     */
    suspend fun scheduleEvent(
        habitId: Long,
        habitName: String,
        message: String,
        event: HabitEvent,
    )

    /**
     * Cancel all scheduled notifications for a habit.
     */
    suspend fun cancelEventsForHabit(habitId: Long)

    /**
     * Check if exact alarms can be scheduled (permissions granted).
     */
    fun canScheduleExactAlarms(): Boolean

    companion object {
        /**
         * Creates the appropriate scheduler based on system capabilities and permissions.
         * Prefers WorkManager but falls back to AlarmManager if needed.
         */
        fun create(context: Context): AlarmScheduler {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Check if we can schedule exact alarms
            val canScheduleExact =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

            // Prefer WorkManager for better reliability and battery optimization
            return if (canScheduleExact) {
                WorkManagerScheduler(context)
            } else {
                // Fallback to AlarmManager (though it won't be exact without permission)
                AlarmManagerScheduler(context)
            }
        }
    }
}

/**
 * WorkManager-based implementation (preferred).
 * Uses WorkManager's exact timing capabilities for scheduling.
 */
class WorkManagerScheduler(private val context: Context) : AlarmScheduler {
    override suspend fun scheduleEvent(
        habitId: Long,
        habitName: String,
        message: String,
        event: HabitEvent,
    ) {
        val delayMillis = event.scheduledAt.toEpochMilli() - System.currentTimeMillis()

        // Don't schedule events in the past
        if (delayMillis < 0) return

        val data =
            workDataOf(
                NotificationWorker.KEY_HABIT_ID to habitId,
                NotificationWorker.KEY_HABIT_NAME to habitName,
                NotificationWorker.KEY_MESSAGE to message,
                NotificationWorker.KEY_EVENT_ID to event.id,
            )

        val workRequest =
            OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("habit_$habitId")
                .addTag("event_${event.id}")
                .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override suspend fun cancelEventsForHabit(habitId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_$habitId")
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}

/**
 * AlarmManager-based implementation (fallback).
 * Uses system AlarmManager for exact timing.
 */
class AlarmManagerScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun scheduleEvent(
        habitId: Long,
        habitName: String,
        message: String,
        event: HabitEvent,
    ) {
        val triggerTime = event.scheduledAt.toEpochMilli()

        // Don't schedule events in the past
        if (triggerTime < System.currentTimeMillis()) return

        val intent =
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_HABIT_ID, habitId)
                putExtra(AlarmReceiver.EXTRA_HABIT_NAME, habitName)
                putExtra(AlarmReceiver.EXTRA_MESSAGE, message)
                putExtra(AlarmReceiver.EXTRA_EVENT_ID, event.id)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        // Use exact alarm if permission is granted
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent,
            )
        } else {
            // Fallback to inexact alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent,
            )
        }
    }

    override suspend fun cancelEventsForHabit(habitId: Long) {
        // Note: With AlarmManager, we need to cancel individual alarms
        // This is a simplified implementation - in production, you'd want to track
        // all scheduled alarm IDs for proper cancellation
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
