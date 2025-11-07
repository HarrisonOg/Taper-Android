package com.harrisonog.taperAndroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.Habit
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class HabitListViewModel(private val repo: TaperRepository) : ViewModel() {
    val uiState =
        repo.observeHabits()
            .map { HabitListState(items = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HabitListState())

    fun deleteHabit(habit: Habit): Job =
        viewModelScope.launch {
            repo.deleteHabit(habit)
        }

    fun updateSettings(wakeStart: LocalTime, wakeEnd: LocalTime, onDone: () -> Unit) =
        viewModelScope.launch {
            repo.updateSettingsAndReschedule(wakeStart, wakeEnd)
            onDone()
        }
}

data class HabitListState(val items: List<Habit> = emptyList())
