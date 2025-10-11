package com.harrisonog.taper_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HabitListViewModel(private val repo: TaperRepository): ViewModel() {
    val uiState = repo.observeHabits()
        .map { HabitListState(items = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HabitListState())
}

data class HabitListState(val items: List<Habit> = emptyList())