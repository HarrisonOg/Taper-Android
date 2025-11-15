package com.harrisonog.taperAndroid.ui

import com.harrisonog.taperAndroid.data.db.Habit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitEditorViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun createHabit_addsHabitToRepository() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitEditorViewModel(repository)
            var createdId: Long? = null

            viewModel.create(
                name = "Test Habit",
                description = "Test Description",
                message = "Time to test!",
                startPerDay = 5,
                endPerDay = 1,
                weeks = 4,
                isGoodHabit = false,
                startDate = LocalDate.now(),
                onDone = { createdId = it }
            )
            advanceUntilIdle()

            assertNotNull(createdId)
            val habits = repository.observeHabits().first()
            assertEquals(1, habits.size)
            assertEquals("Test Habit", habits[0].name)
            assertEquals("Test Description", habits[0].description)
            assertEquals("Time to test!", habits[0].message)
            assertEquals(5, habits[0].startPerDay)
            assertEquals(1, habits[0].endPerDay)
            assertEquals(4, habits[0].weeks)
            assertEquals(false, habits[0].isGoodHabit)
        }

    @Test
    fun createHabit_trimsWhitespace() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitEditorViewModel(repository)

            viewModel.create(
                name = "  Habit  ",
                description = "  Description  ",
                message = "  Message  ",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = true,
                startDate = LocalDate.now(),
                onDone = {}
            )
            advanceUntilIdle()

            val habits = repository.observeHabits().first()
            assertEquals("Habit", habits[0].name)
            assertEquals("Description", habits[0].description)
            assertEquals("Message", habits[0].message)
        }

    @Test
    fun createHabit_withNullDescription() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitEditorViewModel(repository)

            viewModel.create(
                name = "Habit",
                description = null,
                message = "Message",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = true,
                startDate = LocalDate.now(),
                onDone = {}
            )
            advanceUntilIdle()

            val habits = repository.observeHabits().first()
            assertNull(habits[0].description)
        }

    @Test
    fun createGoodHabit_setsIsGoodHabitTrue() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitEditorViewModel(repository)

            viewModel.create(
                name = "Good Habit",
                description = null,
                message = "Message",
                startPerDay = 1,
                endPerDay = 5,
                weeks = 3,
                isGoodHabit = true,
                startDate = LocalDate.now(),
                onDone = {}
            )
            advanceUntilIdle()

            val habits = repository.observeHabits().first()
            assertEquals(true, habits[0].isGoodHabit)
        }

    @Test
    fun updateHabit_modifiesExistingHabit() =
        runTest {
            val originalHabit =
                Habit(
                    id = 1,
                    name = "Original",
                    description = "Original Description",
                    message = "Original Message",
                    startPerDay = 3,
                    endPerDay = 1,
                    weeks = 2,
                    startDate = LocalDate.now(),
                    isGoodHabit = false,
                    isActive = true,
                )
            val repository = FakeTaperRepository(initialHabits = listOf(originalHabit))
            val viewModel = HabitEditorViewModel(repository)

            val updatedHabit =
                originalHabit.copy(
                    name = "Updated",
                    description = "Updated Description",
                    message = "Updated Message",
                    startPerDay = 5,
                    endPerDay = 2,
                    weeks = 4,
                    isGoodHabit = true,
                )

            var updateComplete = false
            viewModel.update(updatedHabit, onDone = { updateComplete = true })
            advanceUntilIdle()

            assertEquals(true, updateComplete)
            val habits = repository.observeHabits().first()
            assertEquals(1, habits.size)
            assertEquals("Updated", habits[0].name)
            assertEquals("Updated Description", habits[0].description)
            assertEquals("Updated Message", habits[0].message)
            assertEquals(5, habits[0].startPerDay)
            assertEquals(2, habits[0].endPerDay)
            assertEquals(4, habits[0].weeks)
            assertEquals(true, habits[0].isGoodHabit)
        }

    @Test
    fun createMultipleHabits_allAreAdded() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitEditorViewModel(repository)

            viewModel.create(
                name = "Habit 1",
                description = null,
                message = "Message 1",
                startPerDay = 3,
                endPerDay = 1,
                weeks = 2,
                isGoodHabit = false,
                startDate = LocalDate.now(),
                onDone = {}
            )
            advanceUntilIdle()

            viewModel.create(
                name = "Habit 2",
                description = null,
                message = "Message 2",
                startPerDay = 1,
                endPerDay = 5,
                weeks = 4,
                isGoodHabit = true,
                startDate = LocalDate.now(),
                onDone = {}
            )
            advanceUntilIdle()

            val habits = repository.observeHabits().first()
            assertEquals(2, habits.size)
            assertEquals("Habit 1", habits[0].name)
            assertEquals("Habit 2", habits[1].name)
        }
}
