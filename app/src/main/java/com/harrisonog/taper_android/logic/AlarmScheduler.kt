package com.harrisonog.taper_android.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Clock
import java.time.Duration
import java.time.ZoneId

/**
 * Coordinates reminder delivery across WorkManager and AlarmManager. For most
 * events we enqueue a deferred WorkManager task, but for taper-down habits that
 * end at zero we fall back to an exact alarm to survive Doze and app process
 * death.
 */
class AlarmScheduler(
    private val workGateway: WorkGateway,
    private val exactAlarmGateway: ExactAlarmGateway,
    private val clock: Clock = Clock.systemDefaultZone()
) {

    /**
     * Cancels any previous alarms for [habit] and schedules every [event] in
     * the supplied collection. Events are filtered to future instants and
     * grouped per day to decide whether WorkManager or an exact alarm should be
     * used.
     */
    fun reschedule(
        habit: Habit,
        events: List<HabitEvent>,
        zoneId: ZoneId = clock.zone
    ) {
        val now = clock.instant()
        val upcoming = events.filter { it.scheduledAt.isAfter(now) }

        workGateway.cancel(habit.id)
        exactAlarmGateway.cancel(habit.id)
        if (upcoming.isEmpty()) return

        val countsByDate = upcoming
            .groupingBy { it.scheduledAt.atZone(zoneId).toLocalDate() }
            .eachCount()

        for (event in upcoming) {
            val delay = Duration.between(now, event.scheduledAt).coerceAtLeast(Duration.ZERO)
            val localDate = event.scheduledAt.atZone(zoneId).toLocalDate()
            val perDay = countsByDate[localDate] ?: 0
            if (shouldUseExact(habit, perDay)) {
                exactAlarmGateway.scheduleExact(habit.id, event, zoneId)
            } else {
                workGateway.enqueue(habit.id, event, delay)
            }
        }
    }

    /** Cancels both WorkManager jobs and exact alarms associated with [habitId]. */
    fun cancel(habitId: Long) {
        workGateway.cancel(habitId)
        exactAlarmGateway.cancel(habitId)
    }

    private fun shouldUseExact(habit: Habit, eventsPerDay: Int): Boolean =
        !habit.isGoodHabit && habit.endPerDay == 0 && eventsPerDay <= 1

    /**
     * Abstraction over WorkManager so JVM tests do not need an Android
     * dependency. Real implementations enqueue a [OneTimeWorkRequest] per
     * event.
     */
    interface WorkGateway {
        fun enqueue(habitId: Long, event: HabitEvent, delay: Duration)
        fun cancel(habitId: Long)
    }

    /** Abstraction around exact alarm delivery for Doze-sensitive reminders. */
    interface ExactAlarmGateway {
        fun scheduleExact(habitId: Long, event: HabitEvent, zoneId: ZoneId)
        fun cancel(habitId: Long)
    }

    companion object {
        /** Convenience factory binding to real platform primitives. */
        fun forContext(
            context: Context,
            workManager: WorkManager,
            requestFactory: WorkRequestFactory,
            alarmManager: AlarmManager,
            pendingIntentFactory: PendingIntentFactory,
            clock: Clock = Clock.systemDefaultZone()
        ): AlarmScheduler {
            return AlarmScheduler(
                workGateway = WorkManagerGateway(workManager, requestFactory),
                exactAlarmGateway = AlarmManagerGateway(alarmManager, pendingIntentFactory),
                clock = clock
            )
        }

        /** Tag that should be attached to every WorkManager request for [habitId]. */
        fun habitTag(habitId: Long): String = "habit-$habitId"
    }
}

/** Factory used by [WorkManagerGateway] to build platform work requests. */
fun interface WorkRequestFactory {
    fun create(habitId: Long, event: HabitEvent, delay: Duration): OneTimeWorkRequest
}

/** Factory for PendingIntent objects used with exact alarms. */
interface PendingIntentFactory {
    fun create(habitId: Long, event: HabitEvent): PendingIntent
    fun cancelAll(habitId: Long, alarmManager: AlarmManager)
}

private class WorkManagerGateway(
    private val workManager: WorkManager,
    private val requestFactory: WorkRequestFactory
) : AlarmScheduler.WorkGateway {

    override fun enqueue(habitId: Long, event: HabitEvent, delay: Duration) {
        val request = requestFactory.create(habitId, event, delay)
        val uniqueName = uniqueWorkName(habitId, event)
        val tag = AlarmScheduler.habitTag(habitId)
        require(request.tags.contains(tag)) {
            "WorkRequestFactory must attach tag '$tag' so AlarmScheduler can cancel prior work."
        }
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(habitId: Long) {
        workManager.cancelAllWorkByTag(AlarmScheduler.habitTag(habitId))
    }

    private fun uniqueWorkName(habitId: Long, event: HabitEvent): String {
        val suffix = if (event.id != 0L) {
            "event-${event.id}"
        } else {
            "ts-${event.scheduledAt.toEpochMilli()}"
        }
        return "habit-$habitId-$suffix"
    }
}

private class AlarmManagerGateway(
    private val alarmManager: AlarmManager,
    private val pendingIntentFactory: PendingIntentFactory
) : AlarmScheduler.ExactAlarmGateway {

    override fun scheduleExact(habitId: Long, event: HabitEvent, zoneId: ZoneId) {
        val pendingIntent = pendingIntentFactory.create(habitId, event)
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            event.scheduledAt.toEpochMilli(),
            pendingIntent
        )
    }

    override fun cancel(habitId: Long) {
        pendingIntentFactory.cancelAll(habitId, alarmManager)
    }
}

/**
 * Default PendingIntent factory that keeps track of generated request codes so
 * alarms can be cancelled later. The supplied [intentProvider] should create a
 * broadcast intent that is declared in the manifest.
 */
class TrackingPendingIntentFactory(
    private val context: Context,
    private val intentProvider: (habitId: Long, event: HabitEvent) -> Intent
) : PendingIntentFactory {

    private val activeIntents = mutableMapOf<Long, MutableSet<Registration>>()

    override fun create(habitId: Long, event: HabitEvent): PendingIntent {
        val requestCode = computeRequestCode(habitId, event)
        val intent = intentProvider(habitId, event)
        activeIntents
            .getOrPut(habitId) { mutableSetOf() }
            .add(Registration(requestCode, Intent(intent)))
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun cancelAll(habitId: Long, alarmManager: AlarmManager) {
        val registrations = activeIntents.remove(habitId) ?: emptySet()
        for (registration in registrations) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                registration.requestCode,
                Intent(registration.intent),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun computeRequestCode(habitId: Long, event: HabitEvent): Int {
        val hashInput = habitId * 31 + event.scheduledAt.toEpochMilli()
        return hashInput.hashCode()
    }

    private data class Registration(val requestCode: Int, val intent: Intent)
}
