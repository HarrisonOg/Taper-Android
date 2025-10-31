package com.harrisonog.taper_android.logic

import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.settings.AppSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleGeneratorTest {

    private val zone = ZoneId.of("America/New_York")
    private val settings = AppSettings(
        wakeStart = LocalTime.of(6, 0),
        wakeEnd = LocalTime.of(22, 0)
    )

    @Test
    fun `taper down schedule remains monotonic through dst change`() {
        val habit = Habit(
            id = 1,
            name = "Caffeine",
            description = null,
            message = "Reduce",
            startPerDay = 8,
            endPerDay = 0,
            weeks = 6,
            startDate = LocalDate.of(2025, 2, 10),
            isGoodHabit = false,
            isActive = true
        )

        val events = ScheduleGenerator.generate(
            habitId = habit.id,
            habit = habit,
            settings = settings,
            zone = zone
        )

        val totalDays = habit.weeks * 7
        val countsByDay = mutableListOf<Int>()
        for (offset in 0 until totalDays) {
            val date = habit.startDate.plusDays(offset.toLong())
            val count = events.count { event ->
                event.scheduledAt.atZone(zone).toLocalDate() == date
            }
            countsByDay += count
            assertTrue("Day $offset should not exceed previous day", countsByDay.size == 1 || count <= countsByDay[countsByDay.size - 2])
        }

        assertTrue("Schedule should taper to zero by the end", countsByDay.last() == 0)

        for (event in events) {
            val localDateTime = event.scheduledAt.atZone(zone)
            val localTime = localDateTime.toLocalTime()
            assertFalse(localTime.isBefore(settings.wakeStart))
            assertFalse(localTime.isAfter(settings.wakeEnd))
        }
    }
}
