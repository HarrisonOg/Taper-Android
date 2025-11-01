package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlarmSchedulerTest {

    private val baseInstant = Instant.parse("2025-01-01T12:00:00Z")
    private val clock: Clock = Clock.fixed(baseInstant, ZoneOffset.UTC)

    @Test
    fun prefersWorkManagerForNonZeroTapers() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactScheduler()
        val scheduler = AlarmScheduler(work, exact, clock)

        val habit = habit(startPerDay = 3, endPerDay = 1)
        val event = HabitEvent(habitId = 1L, scheduledAt = baseInstant.plusSeconds(3_600))

        scheduler.scheduleNext(habit, event)

        assertEquals(listOf(event to Duration.ofSeconds(3_600)), work.scheduled)
        assertTrue(exact.scheduled.isEmpty(), "Exact alarms should not be used for steady habits")
    }

    @Test
    fun usesExactAlarmWhenTaperingToZeroForDozeReliability() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactScheduler()
        val scheduler = AlarmScheduler(work, exact, clock)

        val habit = habit(startPerDay = 6, endPerDay = 0)
        val event = HabitEvent(habitId = 9L, scheduledAt = baseInstant.plusSeconds(900))

        scheduler.scheduleNext(habit, event)

        assertTrue(work.scheduled.isEmpty(), "WorkManager should be skipped for taper to zero")
        assertEquals(listOf(event), exact.scheduled)
    }

    @Test
    fun ignoresEventsThatAreAlreadyInThePast() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactScheduler()
        val scheduler = AlarmScheduler(work, exact, clock)

        val habit = habit(startPerDay = 1, endPerDay = 0)
        val event = HabitEvent(habitId = 2L, scheduledAt = baseInstant.minusSeconds(30))

        scheduler.scheduleNext(habit, event)

        assertTrue(work.scheduled.isEmpty())
        assertTrue(exact.scheduled.isEmpty())
    }

    @Test
    fun cancelPropagatesToBothSchedulers() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactScheduler()
        val scheduler = AlarmScheduler(work, exact, clock)

        scheduler.cancel(42L)

        assertEquals(listOf(42L), work.cancelled)
        assertEquals(listOf(42L), exact.cancelled)
    }

    private fun habit(startPerDay: Int, endPerDay: Int) = Habit(
        id = 0L,
        name = "habit",
        description = null,
        message = "",
        startPerDay = startPerDay,
        endPerDay = endPerDay,
        weeks = 1,
        startDate = java.time.LocalDate.of(2025, 1, 1),
        isGoodHabit = false,
        isActive = true,
    )

    private class RecordingWorkScheduler : AlarmScheduler.WorkScheduler {
        val scheduled = mutableListOf<Pair<HabitEvent, Duration>>()
        val cancelled = mutableListOf<Long>()

        override fun schedule(event: HabitEvent, delay: Duration) {
            scheduled += event to delay
        }

        override fun cancel(habitId: Long) {
            cancelled += habitId
        }
    }

    private class RecordingExactScheduler : AlarmScheduler.ExactAlarmScheduler {
        val scheduled = mutableListOf<HabitEvent>()
        val cancelled = mutableListOf<Long>()

        override fun schedule(event: HabitEvent) {
            scheduled += event
        }

        override fun cancel(habitId: Long) {
            cancelled += habitId
        }
    }
}
