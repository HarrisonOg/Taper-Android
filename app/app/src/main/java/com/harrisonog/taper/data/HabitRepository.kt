package com.harrisonog.taper.data

import com.harrisonog.taper.data.models.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    fun getSelectedHabit(habitId: Int): Flow<Habit> {
        return habitDao.getSelectedHabit(habitId)
    }

    suspend fun insert(habit: Habit) {
        habitDao.insert(habit)
    }

    suspend fun delete(habit: Habit) {
        habitDao.delete(habit)
    }

    suspend fun deleteAllHabits() {
        habitDao.deleteAllHabits()
    }
}