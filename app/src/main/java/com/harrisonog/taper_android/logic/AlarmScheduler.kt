package com.harrisonog.taper_android.logic

import android.app.AlarmManager
import android.app.PendingIntent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.math.max

/**
 * Coordinates alarm delivery for [HabitEvent] reminders.
 *
 * The scheduler prefers WorkManager for flexible delivery so the system can coalesce
 * background work. When a habit is tapering down to zero, Android's doze mode can delay
 * WorkManager jobs for several hours, so we fall back to exact alarms to honour the
 * "last dose" style reminders.
 *
 * Consumers should provide platform specific implementations of [WorkScheduler] and
 * [ExactAlarmScheduler]. See [WorkManagerScheduler] and [AlarmManagerExactScheduler] for
 * ready-to-use adapters.
 */
class AlarmScheduler(
    private val workScheduler: WorkScheduler,
    private val exactAlarmScheduler: ExactAlarmScheduler,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /**
     * Schedule the next [HabitEvent]. When the habit tapers down to zero, exact alarms are
     * preferred so the event fires even in doze mode.
     */
    fun scheduleNext(habit: Habit, event: HabitEvent) {
        val now: Instant = clock.instant()
        if (!event.scheduledAt.isAfter(now)) {
            return
        }
        if (shouldUseExact(habit)) {
            exactAlarmScheduler.schedule(event)
        } else {
            val delay = Duration.between(now, event.scheduledAt)
            workScheduler.schedule(event, delay)
        }
    }

    /**
     * Cancel any pending reminders for the habit across both scheduling backends.
     */
    fun cancel(habitId: Long) {
        workScheduler.cancel(habitId)
        exactAlarmScheduler.cancel(habitId)
    }

    private fun shouldUseExact(habit: Habit): Boolean {
        return habit.startPerDay > habit.endPerDay && habit.endPerDay <= 0
    }

    /**
     * Abstraction over the preferred WorkManager based scheduling path.
     */
    interface WorkScheduler {
        fun schedule(event: HabitEvent, delay: Duration)
        fun cancel(habitId: Long)
    }

    /**
     * Abstraction over the exact alarm path.
     */
    interface ExactAlarmScheduler {
        fun schedule(event: HabitEvent)
        fun cancel(habitId: Long)
    }

    /**
     * Helper that wires [WorkManager] into [AlarmScheduler].
     */
    class WorkManagerScheduler(
        private val workManager: WorkManager,
        private val dispatcher: AlarmDispatcher,
    ) : WorkScheduler {
        override fun schedule(event: HabitEvent, delay: Duration) {
            val safeDelay = max(delay.toMillis(), 0L)
            val request = dispatcher.createWorkRequest(event, safeDelay)
            workManager.enqueueUniqueWork(
                dispatcher.workNameFor(event.habitId),
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        override fun cancel(habitId: Long) {
            workManager.cancelUniqueWork(dispatcher.workNameFor(habitId))
        }
    }

    /**
     * Helper that wires [AlarmManager] into [AlarmScheduler].
     */
    class AlarmManagerExactScheduler(
        private val alarmSetter: AlarmSetter,
        private val dispatcher: AlarmDispatcher,
    ) : ExactAlarmScheduler {
        override fun schedule(event: HabitEvent) {
            val pendingIntent = dispatcher.createExactAlarmIntent(event)
            val triggerAtMillis = event.scheduledAt.toEpochMilli()
            if (alarmSetter.supportsAllowWhileIdle) {
                alarmSetter.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            } else {
                alarmSetter.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }

        override fun cancel(habitId: Long) {
            dispatcher.createExactAlarmCancellationIntent(habitId)?.let { pendingIntent ->
                alarmSetter.cancel(pendingIntent)
            }
        }
    }

    /**
     * Thin abstraction over [AlarmManager] so behaviour is testable on the JVM.
     */
    interface AlarmSetter {
        val supportsAllowWhileIdle: Boolean
        fun setExact(@AlarmManager.AlarmType type: Int, triggerAtMillis: Long, pendingIntent: PendingIntent)
        fun setExactAndAllowWhileIdle(@AlarmManager.AlarmType type: Int, triggerAtMillis: Long, pendingIntent: PendingIntent)
        fun cancel(pendingIntent: PendingIntent)
    }

    class AlarmManagerAdapter(private val alarmManager: AlarmManager) : AlarmSetter {
        override val supportsAllowWhileIdle: Boolean
            get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

        override fun setExact(
            type: Int,
            triggerAtMillis: Long,
            pendingIntent: PendingIntent,
        ) {
            alarmManager.setExact(type, triggerAtMillis, pendingIntent)
        }

        override fun setExactAndAllowWhileIdle(
            type: Int,
            triggerAtMillis: Long,
            pendingIntent: PendingIntent,
        ) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, pendingIntent)
        }

        override fun cancel(pendingIntent: PendingIntent) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Contract that builds the concrete WorkManager request and exact alarm PendingIntent.
     */
    interface AlarmDispatcher {
        fun workNameFor(habitId: Long): String
        fun createWorkRequest(event: HabitEvent, delayMillis: Long): OneTimeWorkRequest
        fun createExactAlarmIntent(event: HabitEvent): PendingIntent
        fun createExactAlarmCancellationIntent(habitId: Long): PendingIntent? = null
    }

    companion object {
        /**
         * Build an [AlarmScheduler] using default Android adapters.
         */
        fun create(
            workManager: WorkManager,
            alarmManager: AlarmManager,
            dispatcher: AlarmDispatcher,
            clock: Clock = Clock.systemDefaultZone(),
        ): AlarmScheduler {
            return AlarmScheduler(
                WorkManagerScheduler(workManager, dispatcher),
                AlarmManagerExactScheduler(AlarmManagerAdapter(alarmManager), dispatcher),
                clock,
            )
        }
    }
}
