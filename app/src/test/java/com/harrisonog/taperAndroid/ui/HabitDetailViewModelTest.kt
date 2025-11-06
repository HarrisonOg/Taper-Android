package com.harrisonog.taperAndroid.ui

import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun deleteHabitClearsDetailState() =
        runTest {
            val habit =
                Habit(
                    id = 5,
                    name = "Read",
                    description = "Read a book",
                    message = "Keep reading",
                    startPerDay = 1,
                    endPerDay = 3,
                    weeks = 6,
                    startDate = LocalDate.now(),
                    isGoodHabit = true,
                    isActive = true,
                )
            val event =
                HabitEvent(
                    id = 10,
                    habitId = habit.id,
                    scheduledAt = Instant.now(),
                    sentAt = null,
                )
            val repository = FakeTaperRepository(initialHabits = listOf(habit))
            repository.setEvents(habit.id, listOf(event))
            val viewModel = HabitDetailViewModel(repository, habit.id)

            val collection = backgroundScope.launch { viewModel.ui.collect { } }
            advanceUntilIdle()

            assertEquals(habit, viewModel.ui.value.habit)
            assertEquals(listOf(event), viewModel.ui.value.events)

            viewModel.deleteHabit(habit)
            advanceUntilIdle()

            assertNull(viewModel.ui.value.habit)
            assertEquals(emptyList<HabitEvent>(), viewModel.ui.value.events)

            collection.cancel()
        }
}
