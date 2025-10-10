package com.harrisonog.taper_android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEventDao {
    @Query("SELECT * FROM habit_events WHERE habitId = :habitId ORDER BY scheduledAt")
    fun observeForHabit(habitId: Long): Flow<List<HabitEvent>>

    @Query("DELETE FROM habit_events WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)

    @Insert
    suspend fun insertAll(events: List<HabitEvent>)
}