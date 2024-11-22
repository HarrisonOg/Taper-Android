package com.harrisonog.taper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.harrisonog.taper.data.AppDatabase
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.HabitRepository
import com.harrisonog.taper.data.MainViewModel
import com.harrisonog.taper.data.SampleData
import com.harrisonog.taper.ui.components.HabitList
import com.harrisonog.taper.ui.components.TaperAppBar
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
        val sampleData : List<Habit> = SampleData().getSampleHabitList()
        setContent {
            TaperTheme {
                Scaffold(
                    topBar = { TaperAppBar() },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        HabitList(sampleData)
                    }
                }
            }
        }
    }
}