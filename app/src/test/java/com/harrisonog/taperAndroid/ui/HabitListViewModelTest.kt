package com.harrisonog.taperAndroid.ui

import com.harrisonog.taperAndroid.data.db.Habit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitListViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun deleteHabitRemovesItFromUiState() =
        runTest {
            val habit1 =
                Habit(
                    id = 1,
                    name = "Habit 1",
                    description = "Description",
                    message = "Message",
                    startPerDay = 2,
                    endPerDay = 1,
                    weeks = 4,
                    startDate = LocalDate.now(),
                    isGoodHabit = true,
                    isActive = true,
                )
            val habit2 = habit1.copy(id = 2, name = "Habit 2")
            val repository = FakeTaperRepository(initialHabits = listOf(habit1, habit2))
            val viewModel = HabitListViewModel(repository)

            val collection = backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            // Extract habits from HabitWithStats for comparison
            val initialHabits = viewModel.uiState.value.items.map { it.habit }
            assertEquals(listOf(habit1, habit2), initialHabits)

            viewModel.deleteHabit(habit1)
            advanceUntilIdle()

            val remainingHabits = viewModel.uiState.value.items.map { it.habit }
            assertEquals(listOf(habit2), remainingHabits)

            collection.cancel()
        }

    @Test
    fun initialState_isEmpty() =
        runTest {
            val repository = FakeTaperRepository()
            val viewModel = HabitListViewModel(repository)

            val collection = backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.items.isEmpty())

            collection.cancel()
        }

    @Test
    fun initialState_containsHabits() =
        runTest {
            val habit1 =
                Habit(
                    id = 1,
                    name = "Habit 1",
                    description = null,
                    message = "Message",
                    startPerDay = 3,
                    endPerDay = 1,
                    weeks = 2,
                    startDate = LocalDate.now(),
                    isGoodHabit = false,
                    isActive = true,
                )
            val habit2 = habit1.copy(id = 2, name = "Habit 2", isGoodHabit = true)

            val repository = FakeTaperRepository(initialHabits = listOf(habit1, habit2))
            val viewModel = HabitListViewModel(repository)

            val collection = backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.items.size)
            assertEquals(habit1, viewModel.uiState.value.items[0].habit)
            assertEquals(habit2, viewModel.uiState.value.items[1].habit)

            collection.cancel()
        }

    @Test
    fun deleteAllHabits_resultsInEmptyState() =
        runTest {
            val habit1 =
                Habit(
                    id = 1,
                    name = "Habit 1",
                    description = null,
                    message = "Message",
                    startPerDay = 3,
                    endPerDay = 1,
                    weeks = 2,
                    startDate = LocalDate.now(),
                    isGoodHabit = false,
                    isActive = true,
                )
            val habit2 = habit1.copy(id = 2, name = "Habit 2")

            val repository = FakeTaperRepository(initialHabits = listOf(habit1, habit2))
            val viewModel = HabitListViewModel(repository)

            val collection = backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.deleteHabit(habit1)
            advanceUntilIdle()

            viewModel.deleteHabit(habit2)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.items.isEmpty())

            collection.cancel()
        }

    @Test
    fun deleteHabit_doesNotAffectOtherHabits() =
        runTest {
            val habit1 =
                Habit(
                    id = 1,
                    name = "Habit 1",
                    description = null,
                    message = "Message",
                    startPerDay = 3,
                    endPerDay = 1,
                    weeks = 2,
                    startDate = LocalDate.now(),
                    isGoodHabit = false,
                    isActive = true,
                )
            val habit2 = habit1.copy(id = 2, name = "Habit 2")
            val habit3 = habit1.copy(id = 3, name = "Habit 3")

            val repository = FakeTaperRepository(initialHabits = listOf(habit1, habit2, habit3))
            val viewModel = HabitListViewModel(repository)

            val collection = backgroundScope.launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            viewModel.deleteHabit(habit2)
            advanceUntilIdle()

            val remaining = viewModel.uiState.value.items
            assertEquals(2, remaining.size)
            assertEquals(habit1, remaining[0].habit)
            assertEquals(habit3, remaining[1].habit)

            collection.cancel()
        }
}
