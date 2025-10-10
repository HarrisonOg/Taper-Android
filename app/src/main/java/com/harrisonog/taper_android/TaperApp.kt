package com.harrisonog.taper_android

import android.app.Application
import androidx.room.Room
import com.harrisonog.taper_android.data.db.AppDatabase

class TaperApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(this, AppDatabase::class.java, "taper.db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

}