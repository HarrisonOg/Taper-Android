package com.harrisonog.taper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.harrisonog.taper.data.models.Habit

@Database(entities = [Habit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}