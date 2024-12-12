package com.harrisonog.taper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.harrisonog.taper.data.AppDatabase
import com.harrisonog.taper.data.HabitRepository
import com.harrisonog.taper.ui.MainViewModel
import com.harrisonog.taper.ui.TaperComposableApp
import com.harrisonog.taper.ui.theme.TaperTheme
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "habit-db")
            .build()

        val mainViewModel = MainViewModel(
            HabitRepository(db.habitDao()),
            ioDispatcher = Dispatchers.IO
        )

        enableEdgeToEdge()
        setContent {
            TaperTheme {
                TaperComposableApp(
                    viewModel = mainViewModel
                )
            }
        }
    }
}