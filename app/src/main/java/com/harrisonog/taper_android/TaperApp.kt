package com.harrisonog.taper_android

import android.app.AlarmManager
import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.harrisonog.taper_android.data.TaperRepository
import com.harrisonog.taper_android.data.db.AppDatabase
import com.harrisonog.taper_android.logic.AlarmScheduler
import com.harrisonog.taper_android.logic.ReminderReceiver
import com.harrisonog.taper_android.logic.ReminderWorkRequestFactory
import com.harrisonog.taper_android.logic.TrackingPendingIntentFactory

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

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "taper.db")
            .fallbackToDestructiveMigration(false)
            .build()
        val workManager = WorkManager.getInstance(this)
        val alarmManager = getSystemService(AlarmManager::class.java)
            ?: throw IllegalStateException("AlarmManager unavailable")
        val pendingIntentFactory = TrackingPendingIntentFactory(this) { habitId, event ->
            ReminderReceiver.createIntent(this, habitId, event)
        }
        alarmScheduler = AlarmScheduler.forContext(
            context = this,
            workManager = workManager,
            requestFactory = ReminderWorkRequestFactory(),
            alarmManager = alarmManager,
            pendingIntentFactory = pendingIntentFactory
        )
        repository = TaperRepository(
            context = this,
            habitDao = database.habitDao(),
            eventDao = database.habitEventDao(),
            alarmScheduler = alarmScheduler
        )
    }
}
