package com.harrisonog.taper_android

import android.app.Application
import androidx.room.Room
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.AppDatabase

/**
 * Application entry point responsible for wiring up the data layer.
 *
 * It initialises the Room [AppDatabase] as well as the shared [TaperRepository] that the
 * composable navigation graph consumes via [MainActivity].
 */
class TaperApp : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var repository: TaperRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "taper.db")
            .fallbackToDestructiveMigration(false)
            .build()
        repository = TaperRepository(
            context = this,
            habitDao = database.habitDao(),
            eventDao = database.habitEventDao()
        )
    }
}
