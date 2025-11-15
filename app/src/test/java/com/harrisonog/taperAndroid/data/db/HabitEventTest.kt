package com.harrisonog.taperAndroid.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class HabitEventTest {
    private val testInstant = Instant.parse("2024-01-01T10:00:00Z")

    @Test
    fun habitEvent_defaultValues() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
            )

        assertEquals(0L, event.id)
        assertEquals(1L, event.habitId)
        assertEquals(testInstant, event.scheduledAt)
        assertNull(event.sentAt)
        assertNull(event.responseType)
        assertNull(event.respondedAt)
        assertFalse(event.isSnoozed)
    }

    @Test
    fun habitEvent_withAllFields() {
        val scheduledAt = Instant.parse("2024-01-01T10:00:00Z")
        val sentAt = Instant.parse("2024-01-01T10:00:05Z")
        val respondedAt = Instant.parse("2024-01-01T10:05:00Z")

        val event =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = scheduledAt,
                sentAt = sentAt,
                responseType = "completed",
                respondedAt = respondedAt,
                isSnoozed = true,
            )

        assertEquals(1L, event.id)
        assertEquals(2L, event.habitId)
        assertEquals(scheduledAt, event.scheduledAt)
        assertEquals(sentAt, event.sentAt)
        assertEquals("completed", event.responseType)
        assertEquals(respondedAt, event.respondedAt)
        assertTrue(event.isSnoozed)
    }

    @Test
    fun habitEvent_copy_preservesValues() {
        val original =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
                sentAt = testInstant.plusSeconds(5),
                responseType = "denied",
                respondedAt = testInstant.plusSeconds(300),
                isSnoozed = true,
            )

        val copied = original.copy()

        assertEquals(original.id, copied.id)
        assertEquals(original.habitId, copied.habitId)
        assertEquals(original.scheduledAt, copied.scheduledAt)
        assertEquals(original.sentAt, copied.sentAt)
        assertEquals(original.responseType, copied.responseType)
        assertEquals(original.respondedAt, copied.respondedAt)
        assertEquals(original.isSnoozed, copied.isSnoozed)
    }

    @Test
    fun habitEvent_completedResponse() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                responseType = "completed",
                respondedAt = testInstant.plusSeconds(300),
            )

        assertEquals("completed", event.responseType)
        assertEquals(testInstant.plusSeconds(300), event.respondedAt)
    }

    @Test
    fun habitEvent_deniedResponse() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                responseType = "denied",
                respondedAt = testInstant.plusSeconds(300),
            )

        assertEquals("denied", event.responseType)
        assertEquals(testInstant.plusSeconds(300), event.respondedAt)
    }

    @Test
    fun habitEvent_snoozedResponse() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                responseType = "snoozed",
                respondedAt = testInstant.plusSeconds(300),
            )

        assertEquals("snoozed", event.responseType)
    }

    @Test
    fun habitEvent_noResponse() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
            )

        assertNull(event.responseType)
        assertNull(event.respondedAt)
    }

    @Test
    fun habitEvent_isSnoozedFlag() {
        val normalEvent =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                isSnoozed = false,
            )

        val snoozedEvent =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant.plusSeconds(1800),
                isSnoozed = true,
            )

        assertFalse(normalEvent.isSnoozed)
        assertTrue(snoozedEvent.isSnoozed)
    }

    @Test
    fun habitEvent_equality() {
        val event1 =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
                sentAt = testInstant.plusSeconds(5),
                responseType = "completed",
                respondedAt = testInstant.plusSeconds(300),
                isSnoozed = false,
            )

        val event2 =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
                sentAt = testInstant.plusSeconds(5),
                responseType = "completed",
                respondedAt = testInstant.plusSeconds(300),
                isSnoozed = false,
            )

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun habitEvent_inequality_differentId() {
        val event1 =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
            )

        val event2 = event1.copy(id = 2)

        assertNotEquals(event1, event2)
    }

    @Test
    fun habitEvent_inequality_differentHabitId() {
        val event1 =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
            )

        val event2 = event1.copy(habitId = 3)

        assertNotEquals(event1, event2)
    }

    @Test
    fun habitEvent_inequality_differentScheduledAt() {
        val event1 =
            HabitEvent(
                id = 1,
                habitId = 2,
                scheduledAt = testInstant,
            )

        val event2 = event1.copy(scheduledAt = testInstant.plusSeconds(3600))

        assertNotEquals(event1, event2)
    }

    @Test
    fun habitEvent_sentAtAfterScheduled() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                sentAt = testInstant.plusSeconds(5),
            )

        assertTrue(event.sentAt!! > event.scheduledAt)
    }

    @Test
    fun habitEvent_respondedAtAfterScheduled() {
        val event =
            HabitEvent(
                habitId = 1,
                scheduledAt = testInstant,
                responseType = "completed",
                respondedAt = testInstant.plusSeconds(300),
            )

        assertTrue(event.respondedAt!! > event.scheduledAt)
    }
}
