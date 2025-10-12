package com.harrisonog.taper_android.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.workDataOf
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Coordinates background reminders for a habit by using WorkManager when possible and
 * falling back to exact alarms for critical "taper to zero" events.
 */
class AlarmScheduler(
    private val workScheduler: WorkScheduler,
    private val exactAlarmDispatcher: ExactAlarmDispatcher
) {

    /**
     * Schedule all [events] for the supplied [habit]. Existing work for the habit is cleared
     * before enqueuing the new plan. When the habit tapers down to zero and the platform allows
     * exact alarms, the final day of events is scheduled with `AlarmManager.setExactAndAllowWhileIdle`
     * so that Doze does not delay delivery.
     */
    fun schedule(
        habit: Habit,
        events: List<HabitEvent>,
        zone: ZoneId = ZoneId.systemDefault()
    ) {
        workScheduler.cancel(habit.id)
        exactAlarmDispatcher.cancel(habit.id)

        val sorted = events.sortedBy { it.scheduledAt }
        if (sorted.isEmpty()) return

        val taperToZero = habit.startPerDay > habit.endPerDay && habit.endPerDay <= 0
        val lastEventDate = sorted.last().scheduledAt.atZone(zone).toLocalDate()
        val useExactForFinalDay = taperToZero && exactAlarmDispatcher.canScheduleExact()

        sorted.forEach { event ->
            val eventDate = event.scheduledAt.atZone(zone).toLocalDate()
            if (useExactForFinalDay && eventDate == lastEventDate) {
                exactAlarmDispatcher.scheduleExact(habit, event)
            } else {
                workScheduler.schedule(habit, event)
            }
        }
    }

    /**
     * Cancels any pending work or exact alarms associated with the habit.
     */
    fun cancelHabit(habitId: Long) {
        workScheduler.cancel(habitId)
        exactAlarmDispatcher.cancel(habitId)
    }

    /**
     * Abstraction for scheduling via WorkManager so it can be swapped out in tests.
     */
    interface WorkScheduler {
        fun schedule(habit: Habit, event: HabitEvent)
        fun cancel(habitId: Long)
    }

    /**
     * Abstraction over `AlarmManager` to support deterministic testing and runtime capability checks.
     */
    interface ExactAlarmDispatcher {
        fun canScheduleExact(): Boolean
        fun scheduleExact(habit: Habit, event: HabitEvent)
        fun cancel(habitId: Long)
    }

    /**
     * Default [WorkScheduler] implementation backed by [WorkManager].
     */
    class WorkManagerWorkScheduler(
        private val workManager: WorkManager,
        private val requestFactory: WorkRequestFactory = WorkRequestFactory()
    ) : WorkScheduler {

        override fun schedule(habit: Habit, event: HabitEvent) {
            val request = requestFactory.create(habit, event)
            workManager.enqueueUniqueWork(
                uniqueName(habit.id, event),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        override fun cancel(habitId: Long) {
            workManager.cancelAllWorkByTag(habitTag(habitId))
        }

        private fun uniqueName(habitId: Long, event: HabitEvent): String =
            "habit-$habitId-event-${event.scheduledAt.toEpochMilli()}"

        companion object {
            fun habitTag(habitId: Long): String = "habit-$habitId"
        }
    }

    /**
     * Factory responsible for creating WorkManager requests with correct metadata.
     */
    class WorkRequestFactory(
        private val clock: Clock = Clock.systemDefaultZone()
    ) {
        fun create(habit: Habit, event: HabitEvent): OneTimeWorkRequest {
            val now = clock.instant()
            var delay = Duration.between(now, event.scheduledAt)
            if (delay.isNegative) {
                delay = Duration.ZERO
            }

            return OneTimeWorkRequestBuilder<HabitEventWorker>()
                .setInputData(payloadFor(habit, event))
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .addTag(WorkManagerWorkScheduler.habitTag(habit.id))
                .build()
        }

        private fun payloadFor(habit: Habit, event: HabitEvent): Data = workDataOf(
            HabitEventWorker.KEY_HABIT_ID to habit.id,
            HabitEventWorker.KEY_EVENT_INSTANT to event.scheduledAt.toEpochMilli()
        )
    }

    /**
     * Default dispatcher that calls through to [AlarmManager].
     */
    class AlarmManagerExactAlarmDispatcher(
        private val context: Context,
        private val alarmManager: AlarmManager,
        private val intentFactory: PendingIntentFactory = PendingIntentFactory(context)
    ) : ExactAlarmDispatcher {

        private val trackedRequestCodes = ConcurrentHashMap<Long, MutableSet<Int>>()

        override fun canScheduleExact(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        }

        override fun scheduleExact(habit: Habit, event: HabitEvent) {
            val requestCode = intentFactory.requestCodeFor(habit.id, event.scheduledAt)
            val pendingIntent = intentFactory.create(habit, event, requestCode)
            trackedRequestCodes.getOrPut(habit.id) { mutableSetOf() }.add(requestCode)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                event.scheduledAt.toEpochMilli(),
                pendingIntent
            )
        }

        override fun cancel(habitId: Long) {
            val codes = trackedRequestCodes.remove(habitId) ?: return
            for (code in codes) {
                val pendingIntent = intentFactory.createCancellationIntent(habitId, code)
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    /**
     * Creates and manages [PendingIntent] instances for exact alarm delivery.
     */
    class PendingIntentFactory(private val context: Context) {

        fun create(habit: Habit, event: HabitEvent, requestCode: Int): PendingIntent {
            val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
                action = ACTION_FIRE
                putExtra(EXTRA_HABIT_ID, habit.id)
                putExtra(EXTRA_EVENT_INSTANT, event.scheduledAt.toEpochMilli())
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun createCancellationIntent(habitId: Long, requestCode: Int): PendingIntent {
            val intent = Intent(context, HabitAlarmReceiver::class.java).apply {
                action = ACTION_FIRE
                putExtra(EXTRA_HABIT_ID, habitId)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun requestCodeFor(habitId: Long, instant: Instant): Int {
            val combined = 31 * habitId.hashCode() + instant.epochSecond.hashCode()
            return combined
        }
    }

    /**
     * Broadcast receiver stub that will be triggered by the exact alarm pending intents.
     * Apps can extend this to hand off to WorkManager, show notifications, etc.
     */
    class HabitAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // No-op stub; real delivery is app specific.
        }

        companion object {
            const val ACTION_FIRE = "com.harrisonog.taper_android.action.HABIT_EVENT"
            const val EXTRA_HABIT_ID = "habitId"
            const val EXTRA_EVENT_INSTANT = "eventInstant"
        }
    }

    /**
     * Worker stub that downstream features can use to react to scheduled events.
     */
    class HabitEventWorker(
        appContext: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(appContext, workerParams) {

        override suspend fun doWork(): Result {
            return Result.success()
        }

        companion object {
            const val KEY_HABIT_ID = "habitId"
            const val KEY_EVENT_INSTANT = "eventInstant"
        }
    }
}
