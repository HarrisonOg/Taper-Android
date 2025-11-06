package com.harrisonog.taperAndroid.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEventDao {
    @Query("SELECT * FROM habit_events WHERE habitId = :habitId ORDER BY scheduledAt")
    fun observeForHabit(habitId: Long): Flow<List<HabitEvent>>

    @Query("SELECT * FROM habit_events WHERE id = :id")
    suspend fun getById(id: Long): HabitEvent?

    @Query("SELECT * FROM habit_events")
    suspend fun getAll(): List<HabitEvent>

    @Query("DELETE FROM habit_events WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)

    @Insert
    suspend fun insertAll(events: List<HabitEvent>)

    @Update
    suspend fun update(event: HabitEvent)
}
