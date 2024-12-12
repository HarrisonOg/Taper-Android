package com.harrisonog.taper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harrisonog.taper.data.models.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit_table")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habit_table WHERE id=:habitId")
    fun getSelectedHabit(habitId: Int) : Flow<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("DELETE FROM habit_table")
    suspend fun deleteAllHabits()
}