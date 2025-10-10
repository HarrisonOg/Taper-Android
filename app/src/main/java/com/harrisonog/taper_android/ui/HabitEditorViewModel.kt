package com.harrisonog.taper_android.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.Habit
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class HabitEditorViewModel(private val repo: TaperRepository): ViewModel() {
    fun create(
        name: String,
        description: String?,
        message: String,
        startPerDay: Int,
        endPerDay: Int,
        weeks: Int,
        isGoodHabit: Boolean,
        startDate: LocalDate
        , onDone: (Long) -> Unit) = viewModelScope.launch {
        val id = repo.createHabitAndPlan(
            Habit(
                name = name.trim(),
                description = description?.trim(),
                message = message.trim(),
                startPerDay = startPerDay,
                endPerDay = endPerDay,
                weeks = weeks,
                isGoodHabit = isGoodHabit,
                startDate = startDate
            )
        )
        onDone(id)
    }

    fun update(habit: Habit, onDone: () -> Unit) = viewModelScope.launch {
        repo.updateHabitAndPlan(habit)
        onDone()
    }
}