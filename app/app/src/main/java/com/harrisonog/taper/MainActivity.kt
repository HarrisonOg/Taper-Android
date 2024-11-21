package com.harrisonog.taper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.SampleData
import com.harrisonog.taper.ui.components.HabitList
import com.harrisonog.taper.ui.components.HabitListItem
import com.harrisonog.taper.ui.components.TaperAppBar
import com.harrisonog.taper.ui.theme.TaperTheme


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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