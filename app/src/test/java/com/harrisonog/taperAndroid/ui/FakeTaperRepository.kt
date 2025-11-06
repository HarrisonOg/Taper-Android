package com.harrisonog.taperAndroid.ui

import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.data.settings.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class FakeTaperRepository(
    initialHabits: List<Habit> = emptyList(),
    initialEvents: Map<Long, List<HabitEvent>> = emptyMap(),
    initialSettings: AppSettings = AppSettings(),
) : TaperRepository {
    private val habitsFlow = MutableStateFlow(initialHabits)
    private val eventsFlow = MutableStateFlow(initialEvents)
    private val settingsFlow = MutableStateFlow(initialSettings)
    private var nextHabitId = (initialHabits.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun observeHabits(): Flow<List<Habit>> = habitsFlow

    override fun observeHabit(id: Long): Flow<Habit?> = habitsFlow.map { habits -> habits.firstOrNull { it.id == id } }

    override fun observeEvents(habitId: Long): Flow<List<HabitEvent>> =
        eventsFlow.map { events -> events[habitId].orEmpty() }

    override fun observeSettings(): Flow<AppSettings> = settingsFlow

    override suspend fun upsertSettings(
        wakeStart: LocalTime,
        wakeEnd: LocalTime,
    ) {
        settingsFlow.value = AppSettings(wakeStart, wakeEnd)
    }

    override suspend fun createHabitAndPlan(habit: Habit): Long {
        val assigned = if (habit.id == 0L) habit.copy(id = nextHabitId++) else habit
        habitsFlow.value = habitsFlow.value + assigned
        return assigned.id
    }

    override suspend fun updateHabitAndPlan(habit: Habit) {
        habitsFlow.value =
            habitsFlow.value.map { existing ->
                if (existing.id == habit.id) habit else existing
            }
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitsFlow.value = habitsFlow.value.filterNot { it.id == habit.id }
        eventsFlow.value = eventsFlow.value - habit.id
    }

    fun setEvents(
        habitId: Long,
        events: List<HabitEvent>,
    ) {
        eventsFlow.value =
            eventsFlow.value.toMutableMap().apply {
                put(habitId, events)
            }
    }
}
