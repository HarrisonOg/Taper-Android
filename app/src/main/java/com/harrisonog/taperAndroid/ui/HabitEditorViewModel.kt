package com.harrisonog.taperAndroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.Habit
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitEditorViewModel(private val repo: TaperRepository) : ViewModel() {
    fun create(
        name: String,
        description: String?,
        message: String,
        startPerDay: Int,
        endPerDay: Int,
        weeks: Int,
        isGoodHabit: Boolean,
        startDate: LocalDate,
        onDone: (Long) -> Unit,
    ) = viewModelScope.launch {
        val id =
            repo.createHabitAndPlan(
                Habit(
                    name = name.trim(),
                    description = description?.trim(),
                    message = message.trim(),
                    startPerDay = startPerDay,
                    endPerDay = endPerDay,
                    weeks = weeks,
                    isGoodHabit = isGoodHabit,
                    startDate = startDate,
                ),
            )
        onDone(id)
    }

    fun update(
        habit: Habit,
        onDone: () -> Unit,
    ) = viewModelScope.launch {
        repo.updateHabitAndPlan(habit)
        onDone()
    }
}
