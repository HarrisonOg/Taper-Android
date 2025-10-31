package com.harrisonog.taper_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitDetailState(
    val habit: Habit? = null,
    val events: List<HabitEvent> = emptyList()
)

class HabitDetailViewModel(private val repo: TaperRepository, habitId: Long): ViewModel() {
    val ui = combine(
        repo.observeHabit(habitId),
        repo.observeEvents(habitId)
    ) { h, e -> HabitDetailState(h, e) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HabitDetailState()
        )

    fun deleteHabit(habit: Habit): Job = viewModelScope.launch {
        repo.deleteHabit(habit)
    }
}