package com.harrisonog.taper.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taper.data.HabitRepository
import com.harrisonog.taper.data.models.Habit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: HabitRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val habits = repository.allHabits

    fun addHabit(habit: Habit) =
        viewModelScope.launch(ioDispatcher) { repository.insert(habit) }

    fun deleteHabit(habit: Habit) =
        viewModelScope.launch(ioDispatcher) { repository.delete(habit) }
}