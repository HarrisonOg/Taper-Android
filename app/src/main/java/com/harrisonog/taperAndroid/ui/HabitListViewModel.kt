package com.harrisonog.taperAndroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.Habit
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class HabitListViewModel(private val repo: TaperRepository) : ViewModel() {
    val uiState =
        repo.observeHabits()
            .map { HabitListState(items = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HabitListState())

    val dashboardState =
        repo.observeAllEvents()
            .map { events -> computeDashboardStats(events, repo.observeHabits()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DashboardStats())

    private suspend fun computeDashboardStats(events: List<com.harrisonog.taperAndroid.data.db.HabitEvent>, habitsFlow: kotlinx.coroutines.flow.Flow<List<Habit>>): DashboardStats {
        val habits = habitsFlow.first()
        return computeDashboardStatsSync(events, habits)
    }

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

data class DashboardStats(
    val totalAlarmsToday: Int = 0,
    val completedToday: Int = 0,
    val habitStreaks: Map<Long, Int> = emptyMap()
)

private fun computeDashboardStatsSync(events: List<com.harrisonog.taperAndroid.data.db.HabitEvent>, habits: List<Habit>): DashboardStats {
    val today = java.time.LocalDate.now()
    val todayStart = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
    val todayEnd = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()

    // Get today's events
    val todayEvents = events.filter { event ->
        event.scheduledAt >= todayStart && event.scheduledAt < todayEnd
    }

    val totalAlarmsToday = todayEvents.size
    val completedToday = todayEvents.count { it.responseType == "completed" }

    // Calculate streaks for each habit
    val habitStreaks = habits.associate { habit ->
        val habitEvents = events.filter { it.habitId == habit.id }
        val streak = calculateStreak(habitEvents, habit.isGoodHabit)
        habit.id to streak
    }

    return DashboardStats(
        totalAlarmsToday = totalAlarmsToday,
        completedToday = completedToday,
        habitStreaks = habitStreaks
    )
}

private fun calculateStreak(events: List<com.harrisonog.taperAndroid.data.db.HabitEvent>, isGoodHabit: Boolean): Int {
    // Calculate consecutive days with successful responses
    // For good habits, "completed" is success. For taper habits, "denied" is success.
    val successType = if (isGoodHabit) "completed" else "denied"

    // Group events by date
    val eventsByDate = events.groupBy { event ->
        event.scheduledAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }

    // Sort dates in descending order (most recent first)
    val sortedDates = eventsByDate.keys.sortedDescending()

    var streak = 0
    var currentDate = java.time.LocalDate.now()

    for (date in sortedDates) {
        // Check if this date is consecutive
        if (date.isAfter(currentDate)) continue
        if (date.isBefore(currentDate.minusDays(streak.toLong()))) break

        // Check if any event on this date was successful
        val dayEvents = eventsByDate[date] ?: continue
        val hasSuccess = dayEvents.any { it.responseType == successType }

        if (hasSuccess) {
            if (date == currentDate.minusDays(streak.toLong())) {
                streak++
            }
        } else {
            // If there's a day with events but no success, break the streak
            if (dayEvents.any { it.responseType != null }) {
                break
            }
        }
    }

    return streak
}
