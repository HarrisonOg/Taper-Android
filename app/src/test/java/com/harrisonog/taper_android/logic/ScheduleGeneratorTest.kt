package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import com.harrisonog.taper_android.data.settings.AppSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleGeneratorTest {

    @Test
    fun taperToZeroMonotonicWithinWakeWindow() {
        val habit = taperHabit(
            startPerDay = 8,
            endPerDay = 0,
            weeks = 6,
            startDate = LocalDate.of(2025, 1, 6),
        )
        val settings = AppSettings(
            wakeStart = LocalTime.of(7, 0),
            wakeEnd = LocalTime.of(22, 0),
        )
        val zone = ZoneId.of("America/New_York")

        val events = ScheduleGenerator.generate(1L, habit, settings, zone)

        val countsByDay = dailyCounts(habit, events, zone)
        assertEquals(habit.startPerDay, countsByDay.first(), "First day count should match startPerDay")
        assertEquals(0, countsByDay.last(), "Final day must taper to zero")
        assertTrue(
            countsByDay.zipWithNext().all { (prev, next) -> prev >= next },
            "Daily counts must be monotonically non-increasing",
        )

        events.forEach { event ->
            val zoned = event.scheduledAt.atZone(zone)
            val time = zoned.toLocalTime()
            assertTrue(!time.isBefore(settings.wakeStart), "Event occurs before wake window")
            assertTrue(time.isBefore(settings.wakeEnd), "Event occurs after wake window closes")
        }
    }

    @Test
    fun eventsStayInWindowThroughDstGap() {
        val habit = taperHabit(
            startPerDay = 4,
            endPerDay = 4,
            weeks = 1,
            startDate = LocalDate.of(2025, 3, 8),
        )
        val settings = AppSettings(
            wakeStart = LocalTime.of(6, 0),
            wakeEnd = LocalTime.of(23, 0),
        )
        val zone = ZoneId.of("America/Los_Angeles")

        val events = ScheduleGenerator.generate(42L, habit, settings, zone)
        assertWithinWindow(habit, events, settings, zone)
    }

    @Test
    fun eventsStayInWindowThroughDstOverlap() {
        val habit = taperHabit(
            startPerDay = 3,
            endPerDay = 3,
            weeks = 1,
            startDate = LocalDate.of(2025, 11, 1),
        )
        val settings = AppSettings(
            wakeStart = LocalTime.of(6, 30),
            wakeEnd = LocalTime.of(22, 30),
        )
        val zone = ZoneId.of("America/New_York")

        val events = ScheduleGenerator.generate(99L, habit, settings, zone)
        assertWithinWindow(habit, events, settings, zone)
    }

    private fun taperHabit(
        startPerDay: Int,
        endPerDay: Int,
        weeks: Int,
        startDate: LocalDate,
    ): Habit = Habit(
        id = 0L,
        name = "Test",
        description = null,
        message = "",
        startPerDay = startPerDay,
        endPerDay = endPerDay,
        weeks = weeks,
        startDate = startDate,
        isGoodHabit = false,
        isActive = true,
    )

    private fun dailyCounts(
        habit: Habit,
        events: List<HabitEvent>,
        zone: ZoneId,
    ): List<Int> {
        val totalDays = habit.weeks * 7
        return (0 until totalDays).map { dayIndex ->
            val date = habit.startDate.plusDays(dayIndex.toLong())
            events.count { event ->
                val eventDate = LocalDateTime.ofInstant(event.scheduledAt, zone).toLocalDate()
                eventDate == date
            }
        }
    }

    private fun assertWithinWindow(
        habit: Habit,
        events: List<HabitEvent>,
        settings: AppSettings,
        zone: ZoneId,
    ) {
        val totalDays = habit.weeks * 7
        (0 until totalDays).forEach { dayIndex ->
            val date = habit.startDate.plusDays(dayIndex.toLong())
            val windowStart = LocalDateTime.of(date, settings.wakeStart).atZone(zone)
            var windowEnd = LocalDateTime.of(date, settings.wakeEnd).atZone(zone)
            if (!windowEnd.isAfter(windowStart)) {
                windowEnd = windowEnd.plusDays(1)
            }
            events.filter { event ->
                val scheduled = event.scheduledAt.atZone(zone)
                scheduled.toLocalDate() == date
            }.forEach { event ->
                val scheduled = event.scheduledAt.atZone(zone)
                assertTrue(!scheduled.isBefore(windowStart), "Event before wake window on $date")
                assertTrue(scheduled.isBefore(windowEnd), "Event after wake window on $date")
            }
        }
    }
}
