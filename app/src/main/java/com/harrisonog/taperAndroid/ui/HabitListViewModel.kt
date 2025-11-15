package com.harrisonog.taperAndroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class HabitListViewModel(private val repo: TaperRepository) : ViewModel() {
    val uiState =
        combine(
            repo.observeHabits(),
            repo.observeAllEvents()
        ) { habits, events ->
            val habitsWithStats = habits.map { habit ->
                val habitEvents = events.filter { it.habitId == habit.id }
                computeHabitStats(habit, habitEvents)
            }
            HabitListState(items = habitsWithStats)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HabitListState())

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

data class HabitListState(val items: List<HabitWithStats> = emptyList())

data class HabitWithStats(
    val habit: Habit,
    val completedToday: Int,
    val totalToday: Int,
    val currentWeek: Int,
    val totalWeeks: Int,
    val nextAlarmTime: Instant?
)

data class DashboardStats(
    val totalAlarmsToday: Int = 0,
    val completedToday: Int = 0,
    val habitStreaks: Map<Long, Int> = emptyMap()
)

private fun computeHabitStats(habit: Habit, events: List<HabitEvent>): HabitWithStats {
    val today = LocalDate.now()
    val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
    val todayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

    // Get today's events
    val todayEvents = events.filter { event ->
        event.scheduledAt >= todayStart && event.scheduledAt < todayEnd
    }

    val completedToday = todayEvents.count { it.responseType == "completed" }
    val totalToday = todayEvents.size

    // Calculate current week
    val startDate = habit.startDate
    val currentDay = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val currentWeek = ((currentDay - 1) / 7) + 1

    // Find next alarm
    val now = Instant.now()
    val upcomingEvents = events.filter { event ->
        event.scheduledAt > now && event.responseType == null
    }.sortedBy { it.scheduledAt }
    val nextAlarm = upcomingEvents.firstOrNull()

    return HabitWithStats(
        habit = habit,
        completedToday = completedToday,
        totalToday = totalToday,
        currentWeek = currentWeek,
        totalWeeks = habit.weeks,
        nextAlarmTime = nextAlarm?.scheduledAt
    )
}

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
    // For both good habits and taper habits, "completed" is success.
    val successType = "completed"

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
