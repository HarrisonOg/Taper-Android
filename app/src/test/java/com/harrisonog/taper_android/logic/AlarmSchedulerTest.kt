package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlarmSchedulerTest {

    private val zone = ZoneId.of("UTC")

    @Test
    fun `taper to zero uses exact alarms on final day`() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactDispatcher(canSchedule = true)
        val scheduler = AlarmScheduler(work, exact)
        val habit = taperHabit()
        val events = listOf(
            eventAt(habit, 0, 8),
            eventAt(habit, 0, 14),
            eventAt(habit, 1, 12),
            eventAt(habit, 2, 9)
        )

        scheduler.schedule(habit, events, zone)

        val finalDate = events.maxBy { it.scheduledAt }.scheduledAt.atZone(zone).toLocalDate()
        assertTrue(exact.scheduledEvents.isNotEmpty())
        assertTrue(exact.scheduledEvents.all { it.scheduledAt.atZone(zone).toLocalDate() == finalDate })
        assertTrue(work.scheduledEvents.none { it.scheduledAt.atZone(zone).toLocalDate() == finalDate })

        // Cancels are called before scheduling and exposed to the fakes.
        assertEquals(listOf(habit.id), work.cancelledHabits)
        assertEquals(listOf(habit.id), exact.cancelledHabits)
    }

    @Test
    fun `falls back to work when exact alarms are not permitted`() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactDispatcher(canSchedule = false)
        val scheduler = AlarmScheduler(work, exact)
        val habit = taperHabit()
        val events = listOf(
            eventAt(habit, 0, 8),
            eventAt(habit, 0, 14),
            eventAt(habit, 1, 12),
            eventAt(habit, 2, 9)
        )

        scheduler.schedule(habit, events, zone)

        assertTrue(exact.scheduledEvents.isEmpty())
        assertEquals(events.map { it.scheduledAt }, work.scheduledEvents.map { it.scheduledAt })
    }

    @Test
    fun `cancel habit delegates to both schedulers`() {
        val work = RecordingWorkScheduler()
        val exact = RecordingExactDispatcher(canSchedule = true)
        val scheduler = AlarmScheduler(work, exact)

        scheduler.cancelHabit(42L)

        assertEquals(listOf(42L), work.cancelledHabits)
        assertEquals(listOf(42L), exact.cancelledHabits)
    }

    private fun eventAt(habit: Habit, dayOffset: Long, hour: Int): HabitEvent {
        val instant = LocalDateTime.of(habit.startDate.plusDays(dayOffset), LocalTime.of(hour, 0))
            .atZone(zone)
            .toInstant()
        return HabitEvent(
            habitId = habit.id,
            scheduledAt = instant
        )
    }

    private fun taperHabit(): Habit = Habit(
        id = 7,
        name = "Caffeine",
        description = null,
        message = "Reduce",
        startPerDay = 4,
        endPerDay = 0,
        weeks = 1,
        startDate = LocalDate.of(2025, 1, 6),
        isGoodHabit = false,
        isActive = true
    )

    private class RecordingWorkScheduler : AlarmScheduler.WorkScheduler {
        val scheduledEvents = mutableListOf<HabitEvent>()
        val cancelledHabits = mutableListOf<Long>()

        override fun schedule(habit: Habit, event: HabitEvent) {
            scheduledEvents += event
        }

        override fun cancel(habitId: Long) {
            cancelledHabits += habitId
        }
    }

    private class RecordingExactDispatcher(
        private val canSchedule: Boolean
    ) : AlarmScheduler.ExactAlarmDispatcher {
        val scheduledEvents = mutableListOf<HabitEvent>()
        val cancelledHabits = mutableListOf<Long>()

        override fun canScheduleExact(): Boolean = canSchedule

        override fun scheduleExact(habit: Habit, event: HabitEvent) {
            scheduledEvents += event
        }

        override fun cancel(habitId: Long) {
            cancelledHabits += habitId
        }
    }
}
