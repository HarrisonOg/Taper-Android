package com.harrisonog.taper_android.data

import android.content.Context
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitDao
import com.harrisonog.taper_android.data.db.HabitEventDao
import com.harrisonog.taper_android.data.db.HabitEvent
import com.harrisonog.taper_android.data.settings.AppSettings
import com.harrisonog.taper_android.data.settings.observeSettings
import com.harrisonog.taper_android.data.settings.saveSettings
import com.harrisonog.taper_android.logic.ScheduleGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalTime

interface TaperRepository {
    fun observeHabits(): Flow<List<Habit>>
    fun observeHabit(id: Long): Flow<Habit?>
    fun observeEvents(habitId: Long): Flow<List<HabitEvent>>

    fun observeSettings(): Flow<AppSettings>
    suspend fun upsertSettings(wakeStart: LocalTime, wakeEnd: LocalTime)

    suspend fun createHabitAndPlan(habit: Habit): Long
    suspend fun updateHabitAndPlan(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
}

class DefaultTaperRepository(
    private val context: Context,
    private val habitDao: HabitDao,
    private val eventDao: HabitEventDao,
) : TaperRepository {
    // Room-backed domain data
    override fun observeHabits() = habitDao.observeAll()
    override fun observeHabit(id: Long) = habitDao.observe(id)
    override fun observeEvents(habitId: Long) = eventDao.observeForHabit(habitId)

    // DataStore-backed settings
    override fun observeSettings(): Flow<AppSettings> = context.observeSettings()

    override suspend fun upsertSettings(wakeStart: LocalTime, wakeEnd: LocalTime) {
        context.saveSettings(AppSettings(wakeStart, wakeEnd))
    }

    override suspend fun createHabitAndPlan(habit: Habit): Long {
        val id = habitDao.insert(habit)
        regenerateEvents(id)
        return id
    }

    override suspend fun updateHabitAndPlan(habit: Habit) {
        habitDao.update(habit); regenerateEvents(habit.id)
    }

    override suspend fun deleteHabit(habit: Habit) {
        eventDao.deleteForHabit(habit.id); habitDao.delete(habit)
    }

    private suspend fun regenerateEvents(habitId: Long) {
        val habit = habitDao.observe(habitId).first() ?: return
        val settings = observeSettings().first()
        eventDao.deleteForHabit(habitId)
        val events = ScheduleGenerator.generateUsingPrefs(
            context = context,
            habitId = habitId,
            habit = habit
        )
        if (events.isNotEmpty()) eventDao.insertAll(events)
    }
}