package com.harrisonog.taper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.harrisonog.taper.data.models.Habit
import com.harrisonog.taper.data.models.SampleData
import com.harrisonog.taper.ui.components.HabitList

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sampleData : List<Habit> = SampleData().getSampleHabitList()


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HabitList(sampleData)
    }
}