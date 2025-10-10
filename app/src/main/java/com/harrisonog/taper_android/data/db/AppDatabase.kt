package com.harrisonog.taper_android.data.db

import androidx.room.*
import androidx.room.RoomDatabase

@Database(
    entities = [Habit::class, HabitEvent::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEventDao(): HabitEventDao
}