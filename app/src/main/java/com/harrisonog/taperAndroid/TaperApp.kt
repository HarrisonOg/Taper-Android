package com.harrisonog.taperAndroid

import android.app.Application
import com.harrisonog.taperAndroid.data.DefaultTaperRepository
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.AppDatabase

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
        database = AppDatabase.getInstance(this)
        repository =
            DefaultTaperRepository(
                context = this,
                habitDao = database.habitDao(),
                eventDao = database.habitEventDao(),
            )
    }
}
