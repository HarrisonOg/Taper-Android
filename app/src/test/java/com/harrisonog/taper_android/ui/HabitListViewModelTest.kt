package com.harrisonog.taper_android.ui

import com.harrisonog.taper_android.data.db.Habit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun deleteHabitRemovesItFromUiState() = runTest {
        val habit1 = Habit(
            id = 1,
            name = "Habit 1",
            description = "Description",
            message = "Message",
            startPerDay = 2,
            endPerDay = 1,
            weeks = 4,
            startDate = LocalDate.now(),
            isGoodHabit = true,
            isActive = true
        )
        val habit2 = habit1.copy(id = 2, name = "Habit 2")
        val repository = FakeTaperRepository(initialHabits = listOf(habit1, habit2))
        val viewModel = HabitListViewModel(repository)

        val collection = backgroundScope.launch { viewModel.uiState.collect { } }

        assertEquals(listOf(habit1, habit2), viewModel.uiState.value.items)

        viewModel.deleteHabit(habit1)
        advanceUntilIdle()

        assertEquals(listOf(habit2), viewModel.uiState.value.items)

        collection.cancel()
    }
}
