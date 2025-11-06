package com.harrisonog.taperAndroid

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.harrisonog.taperAndroid.data.DefaultTaperRepository
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.data.db.AppDatabase
import com.harrisonog.taperAndroid.scheduling.RescheduleWorker
import java.util.concurrent.TimeUnit

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

        // Schedule periodic rescheduling to keep the next 14 days of events scheduled
        schedulePeriodicRescheduling()
    }

    private fun schedulePeriodicRescheduling() {
        val rescheduleWork =
            PeriodicWorkRequestBuilder<RescheduleWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "habit_reschedule",
            ExistingPeriodicWorkPolicy.KEEP,
            rescheduleWork
        )
    }
}
