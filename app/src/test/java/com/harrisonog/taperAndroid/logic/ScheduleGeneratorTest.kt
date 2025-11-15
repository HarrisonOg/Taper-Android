package com.harrisonog.taperAndroid.logic

import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.settings.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ScheduleGeneratorTest {
    private val testZone = ZoneId.of("America/New_York")
    private val defaultSettings =
        AppSettings(
            wakeStart = LocalTime.of(8, 0),
            wakeEnd = LocalTime.of(22, 0),
        )

    @Test
    fun generate_taperHabit_decreasesEventsOverTime() {
        val habit =
            Habit(
                id = 1,
                name = "Taper Habit",
                description = null,
                message = "Message",
                startPerDay = 5,
                endPerDay = 1,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        // Should have events for 14 days (2 weeks)
        // Day 1: 5 events, gradually decreasing to Day 14: 1 event
        assertTrue(events.isNotEmpty())

        // Group events by day
        val eventsByDay =
            events.groupBy {
                it.scheduledAt.atZone(testZone).toLocalDate()
            }

        // First day should have 5 events
        val firstDay = habit.startDate
        assertEquals(5, eventsByDay[firstDay]?.size)

        // Last day should have 1 event
        val lastDay = habit.startDate.plusDays(13)
        assertEquals(1, eventsByDay[lastDay]?.size)

        // Total days should be 14
        assertEquals(14, eventsByDay.size)
    }

    @Test
    fun generate_goodHabit_increasesEventsOverTime() {
        val habit =
            Habit(
                id = 1,
                name = "Good Habit",
                description = null,
                message = "Message",
                startPerDay = 1,
                endPerDay = 5,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = true,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        val eventsByDay =
            events.groupBy {
                it.scheduledAt.atZone(testZone).toLocalDate()
            }

        // First day should have 1 event
        assertEquals(1, eventsByDay[habit.startDate]?.size)

        // Last day should have 5 events
        val lastDay = habit.startDate.plusDays(13)
        assertEquals(5, eventsByDay[lastDay]?.size)
    }

    @Test
    fun generate_eventsWithinWakeWindow() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 3,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        // All events should be within wake window
        events.forEach { event ->
            val time = event.scheduledAt.atZone(testZone).toLocalTime()
            assertTrue(
                "Event time $time should be after ${defaultSettings.wakeStart}",
                !time.isBefore(defaultSettings.wakeStart)
            )
            assertTrue(
                "Event time $time should be before ${defaultSettings.wakeEnd}",
                !time.isAfter(defaultSettings.wakeEnd)
            )
        }
    }

    @Test
    fun generate_eventsSpreadThroughoutDay() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 5,
                endPerDay = 5,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        // Get events for the first day
        val firstDayEvents =
            events
                .filter {
                    it.scheduledAt.atZone(testZone).toLocalDate() == habit.startDate
                }.sortedBy { it.scheduledAt }

        assertEquals(5, firstDayEvents.size)

        // Events should be spread throughout the day
        val times = firstDayEvents.map { it.scheduledAt.atZone(testZone).toLocalTime() }

        // First event should be relatively early in wake window
        val firstEventMinutes = Duration.between(defaultSettings.wakeStart, times[0]).toMinutes()
        assertTrue("First event should be in first half of day", firstEventMinutes < 7 * 60)

        // Last event should be relatively late in wake window
        val lastEventMinutes = Duration.between(times[4], defaultSettings.wakeEnd).toMinutes()
        assertTrue("Last event should be in second half of day", lastEventMinutes < 7 * 60)
    }

    @Test
    fun generate_singleWeek_correctNumberOfDays() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 2,
                endPerDay = 2,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        val eventsByDay =
            events.groupBy {
                it.scheduledAt.atZone(testZone).toLocalDate()
            }

        assertEquals(7, eventsByDay.size)
    }

    @Test
    fun generate_multipleWeeks_correctNumberOfDays() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 2,
                endPerDay = 2,
                weeks = 4,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        val eventsByDay =
            events.groupBy {
                it.scheduledAt.atZone(testZone).toLocalDate()
            }

        assertEquals(28, eventsByDay.size)
    }

    @Test
    fun generate_zeroEndPerDay_noEventsOnLastDay() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 5,
                endPerDay = 0,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        val lastDay = habit.startDate.plusDays(13)
        val lastDayEvents =
            events.filter {
                it.scheduledAt.atZone(testZone).toLocalDate() == lastDay
            }

        // Last day should have 0 events (or be absent from the map)
        assertTrue(lastDayEvents.isEmpty())
    }

    @Test
    fun generate_eventsSorted() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 5,
                endPerDay = 1,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        // Events should be sorted by scheduled time
        val sortedEvents = events.sortedBy { it.scheduledAt }
        assertEquals(events, sortedEvents)
    }

    @Test
    fun generate_customWakeWindow_respectsSettings() {
        val customSettings =
            AppSettings(
                wakeStart = LocalTime.of(6, 0),
                wakeEnd = LocalTime.of(23, 0),
            )

        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 3,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, customSettings, testZone)

        // All events should be within custom wake window
        events.forEach { event ->
            val time = event.scheduledAt.atZone(testZone).toLocalTime()
            assertTrue(!time.isBefore(customSettings.wakeStart))
            assertTrue(!time.isAfter(customSettings.wakeEnd))
        }
    }

    @Test
    fun generate_allEventsHaveCorrectHabitId() {
        val habitId = 42L
        val habit =
            Habit(
                id = habitId,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(habitId, habit, defaultSettings, testZone)

        events.forEach { event ->
            assertEquals(habitId, event.habitId)
        }
    }

    @Test
    fun generate_eventResponseFieldsAreNull() {
        val habit =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 2,
                endPerDay = 2,
                weeks = 1,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val events = ScheduleGenerator.generate(1, habit, defaultSettings, testZone)

        events.forEach { event ->
            assertEquals(null, event.sentAt)
            assertEquals(null, event.responseType)
            assertEquals(null, event.respondedAt)
            assertEquals(false, event.isSnoozed)
        }
    }
}
