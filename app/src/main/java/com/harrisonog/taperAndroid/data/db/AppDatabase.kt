package com.harrisonog.taperAndroid.data.db

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Habit::class, HabitEvent::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    abstract fun habitEventDao(): HabitEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to habit_events table
                db.execSQL("ALTER TABLE habit_events ADD COLUMN responseType TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE habit_events ADD COLUMN respondedAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE habit_events ADD COLUMN isSnoozed INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "taper.db",
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration(false)
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
