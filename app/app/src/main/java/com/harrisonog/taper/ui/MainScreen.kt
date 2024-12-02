package com.harrisonog.taper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.SampleData
import com.harrisonog.taper.ui.components.HabitList

@Composable
fun MainScreen(
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