package com.harrisonog.taper_android.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitDao
import com.harrisonog.taper_android.data.db.HabitEventDao
import com.harrisonog.taper_android.data.settings.AppSettings
import com.harrisonog.taper_android.data.settings.observeSettings
import com.harrisonog.taper_android.data.settings.saveSettings
import com.harrisonog.taper_android.logic.ScheduleGenerator
import kotlinx.coroutines.flow.first
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class TaperRepository(
    private val context: Context,
    private val habitDao: HabitDao,
    private val eventDao: HabitEventDao,
) {
    // Room-backed domain data
    fun observeHabits() = habitDao.observeAll()
    fun observeHabit(id: Long) = habitDao.observe(id)
    fun observeEvents(habitId: Long) = eventDao.observeForHabit(habitId)

    // DataStore-backed settings
    fun observeSettings() = context.observeSettings()

    suspend fun upsertSettings(wakeStart: LocalTime, wakeEnd: LocalTime) {
        context.saveSettings(AppSettings(wakeStart, wakeEnd))
    }

    suspend fun createHabitAndPlan(habit: Habit): Long {
        val id = habitDao.insert(habit)
        regenerateEvents(id)
        return id
    }

    suspend fun updateHabitAndPlan(habit: Habit) {
        habitDao.update(habit); regenerateEvents(habit.id)
    }

    suspend fun deleteHabit(habit: Habit) {
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