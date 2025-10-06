package com.harrisonog.taper_android.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent

import kotlinx.coroutines.flow.*

data class HabitDetailState(
    val habit: Habit? = null,
    val events: List<HabitEvent> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
class HabitDetailViewModel(private val repo: TaperRepository, habitId: Long): ViewModel() {
    val ui = combine(
        repo.observeHabit(habitId),
        repo.observeEvents(habitId)
    ) { h, e -> HabitDetailState(h, e) }
        .stateInWhileSubscribed()

    private fun <T> Flow<T>.stateInWhileSubscribed() =
        this.stateIn(
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate),
            SharingStarted.WhileSubscribed(5000),
            HabitDetailState()
        )
}