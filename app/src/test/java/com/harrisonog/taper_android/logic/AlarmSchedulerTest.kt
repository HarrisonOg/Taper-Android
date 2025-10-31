package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSchedulerTest {

    private val zone = ZoneId.of("America/Los_Angeles")
    private val fixedClock = Clock.fixed(Instant.parse("2025-01-05T15:00:00Z"), zone)

    @Test
    fun `days with multiple reminders use WorkManager`() {
        val habit = taperHabit(startPerDay = 4)
        val events = listOf(
            eventAt(habit, 0, 9),
            eventAt(habit, 0, 14),
            eventAt(habit, 1, 10)
        )
        val workGateway = RecordingWorkGateway()
        val exactGateway = RecordingExactGateway()
        val scheduler = AlarmScheduler(workGateway, exactGateway, fixedClock)

        scheduler.reschedule(habit, events, zone)

        assertEquals("WorkManager should receive two events", 2, workGateway.enqueued.size)
        assertTrue("Exact alarm gateway should be empty", exactGateway.scheduled.isEmpty())
        assertEquals(listOf(habit.id), workGateway.cancelled)
        assertEquals(listOf(habit.id), exactGateway.cancelled)
    }

    @Test
    fun `single reminder days fall back to exact alarms to beat doze`() {
        val habit = taperHabit(startPerDay = 2)
        val events = listOf(
            eventAt(habit, 0, 9),
            eventAt(habit, 2, 11)
        )
        val workGateway = RecordingWorkGateway()
        val exactGateway = RecordingExactGateway()
        val scheduler = AlarmScheduler(workGateway, exactGateway, fixedClock)

        scheduler.reschedule(habit, events, zone)

        assertEquals(1, workGateway.enqueued.size)
        assertEquals(1, exactGateway.scheduled.size)
        val exactEvent = exactGateway.scheduled.single()
        assertEquals(habit.id, exactEvent.habitId)
        val scheduledInstant = events[1].scheduledAt
        assertEquals(scheduledInstant, exactEvent.event.scheduledAt)
    }

    private fun taperHabit(startPerDay: Int) = Habit(
        id = 42,
        name = "Nicotine",
        description = null,
        message = "Wind down",
        startPerDay = startPerDay,
        endPerDay = 0,
        weeks = 3,
        startDate = LocalDate.of(2025, 1, 1),
        isGoodHabit = false,
        isActive = true
    )

    private fun eventAt(habit: Habit, dayOffset: Long, hour: Int): HabitEvent {
        val zoned = ZonedDateTime.of(habit.startDate.plusDays(dayOffset), LocalTime.of(hour, 0), zone)
        return HabitEvent(
            habitId = habit.id,
            scheduledAt = zoned.toInstant()
        )
    }

    private class RecordingWorkGateway : AlarmScheduler.WorkGateway {
        val enqueued = mutableListOf<Triple<Long, HabitEvent, Duration>>()
        val cancelled = mutableListOf<Long>()

        override fun enqueue(habitId: Long, event: HabitEvent, delay: Duration) {
            enqueued += Triple(habitId, event, delay)
        }

        override fun cancel(habitId: Long) {
            cancelled += habitId
        }
    }

    private class RecordingExactGateway : AlarmScheduler.ExactAlarmGateway {
        data class Entry(val habitId: Long, val event: HabitEvent)

        val scheduled = mutableListOf<Entry>()
        val cancelled = mutableListOf<Long>()

        override fun scheduleExact(habitId: Long, event: HabitEvent, zoneId: ZoneId) {
            scheduled += Entry(habitId, event)
        }

        override fun cancel(habitId: Long) {
            cancelled += habitId
        }
    }
}
