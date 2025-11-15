package com.harrisonog.taperAndroid.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitTest {
    @Test
    fun habit_defaultValues() {
        val habit =
            Habit(
                name = "Test",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        assertEquals(0L, habit.id)
        assertEquals(LocalDate.now(), habit.startDate)
        assertTrue(habit.isActive)
    }

    @Test
    fun habit_copy_preservesValues() {
        val original =
            Habit(
                id = 1,
                name = "Original",
                description = "Description",
                message = "Message",
                startPerDay = 5,
                endPerDay = 2,
                weeks = 4,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = true,
                isActive = false,
            )

        val copied = original.copy()

        assertEquals(original.id, copied.id)
        assertEquals(original.name, copied.name)
        assertEquals(original.description, copied.description)
        assertEquals(original.message, copied.message)
        assertEquals(original.startPerDay, copied.startPerDay)
        assertEquals(original.endPerDay, copied.endPerDay)
        assertEquals(original.weeks, copied.weeks)
        assertEquals(original.startDate, copied.startDate)
        assertEquals(original.isGoodHabit, copied.isGoodHabit)
        assertEquals(original.isActive, copied.isActive)
    }

    @Test
    fun habit_copy_canModifyFields() {
        val original =
            Habit(
                id = 1,
                name = "Original",
                description = "Description",
                message = "Message",
                startPerDay = 5,
                endPerDay = 2,
                weeks = 4,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = true,
                isActive = true,
            )

        val modified =
            original.copy(
                name = "Modified",
                startPerDay = 10,
                isGoodHabit = false,
            )

        assertEquals("Modified", modified.name)
        assertEquals(10, modified.startPerDay)
        assertFalse(modified.isGoodHabit)

        // Other fields should remain unchanged
        assertEquals(original.id, modified.id)
        assertEquals(original.description, modified.description)
        assertEquals(original.endPerDay, modified.endPerDay)
    }

    @Test
    fun habit_equality() {
        val habit1 =
            Habit(
                id = 1,
                name = "Habit",
                description = "Description",
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        val habit2 =
            Habit(
                id = 1,
                name = "Habit",
                description = "Description",
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                startDate = LocalDate.of(2024, 1, 1),
                isGoodHabit = false,
                isActive = true,
            )

        assertEquals(habit1, habit2)
        assertEquals(habit1.hashCode(), habit2.hashCode())
    }

    @Test
    fun habit_inequality_differentId() {
        val habit1 =
            Habit(
                id = 1,
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        val habit2 = habit1.copy(id = 2)

        assertNotEquals(habit1, habit2)
    }

    @Test
    fun habit_inequality_differentName() {
        val habit1 =
            Habit(
                id = 1,
                name = "Habit 1",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        val habit2 = habit1.copy(name = "Habit 2")

        assertNotEquals(habit1, habit2)
    }

    @Test
    fun habit_nullDescription() {
        val habit =
            Habit(
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        assertEquals(null, habit.description)
    }

    @Test
    fun habit_nonNullDescription() {
        val habit =
            Habit(
                name = "Habit",
                description = "This is a description",
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        assertEquals("This is a description", habit.description)
    }

    @Test
    fun habit_taperHabit_isGoodHabitFalse() {
        val habit =
            Habit(
                name = "Taper Habit",
                description = null,
                message = "Message",
                startPerDay = 5,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
            )

        assertFalse(habit.isGoodHabit)
    }

    @Test
    fun habit_goodHabit_isGoodHabitTrue() {
        val habit =
            Habit(
                name = "Good Habit",
                description = null,
                message = "Message",
                startPerDay = 1,
                endPerDay = 5,
                weeks = 2,
                isGoodHabit = true,
            )

        assertTrue(habit.isGoodHabit)
    }
}
