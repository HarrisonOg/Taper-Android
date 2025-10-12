package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.settings.AppSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleGeneratorTest {

    private val zone = ZoneId.of("America/New_York")
    private val settings = AppSettings(
        wakeStart = LocalTime.of(7, 0),
        wakeEnd = LocalTime.of(22, 0)
    )

    @Test
    fun `taper schedule is monotonic and respects wake window`() {
        val habit = Habit(
            id = 1,
            name = "Caffeine",
            description = null,
            message = "Reduce intake",
            startPerDay = 8,
            endPerDay = 0,
            weeks = 6,
            startDate = LocalDate.of(2025, 1, 6),
            isGoodHabit = false,
            isActive = true
        )

        val events = ScheduleGenerator.generate(habit.id, habit, settings, zone)

        // All events occur while the user is awake.
        events.forEach { event ->
            val local = event.scheduledAt.atZone(zone)
            assertFalse(local.toLocalTime().isBefore(settings.wakeStart), "Event before wake start")
            assertFalse(local.toLocalTime().isAfter(settings.wakeEnd), "Event after wake end")
        }

        // Extract daily counts and ensure they monotonically decrease.
        val countsByDate = events.groupBy { it.scheduledAt.atZone(zone).toLocalDate() }
            .toSortedMap()
            .mapValues { (_, value) -> value.size }
            .values
            .toList()

        assertTrue(countsByDate.isNotEmpty())
        assertEquals(8, countsByDate.first())
        countsByDate.zipWithNext().forEach { (current, next) ->
            assertTrue(current >= next, "Counts should not increase")
        }
    }

    @Test
    fun `handles DST spring forward without leaving wake window`() {
        val zone = ZoneId.of("America/New_York")
        val habit = Habit(
            id = 9,
            name = "Stretch",
            description = null,
            message = "Stay limber",
            startPerDay = 3,
            endPerDay = 3,
            weeks = 1,
            startDate = LocalDate.of(2025, 3, 8),
            isGoodHabit = true,
            isActive = true
        )
        val settings = AppSettings(
            wakeStart = LocalTime.of(6, 0),
            wakeEnd = LocalTime.of(22, 0)
        )

        val events = ScheduleGenerator.generate(habit.id, habit, settings, zone)
        val dstSunday = LocalDate.of(2025, 3, 9)
        val eventsOnDstDay = events.filter { it.scheduledAt.atZone(zone).toLocalDate() == dstSunday }

        assertEquals(3, eventsOnDstDay.size, "DST day should keep configured cadence")
        eventsOnDstDay.forEach { event ->
            val local = event.scheduledAt.atZone(zone)
            assertFalse(local.toLocalTime().isBefore(settings.wakeStart))
            assertFalse(local.toLocalTime().isAfter(settings.wakeEnd))
        }
    }
}
