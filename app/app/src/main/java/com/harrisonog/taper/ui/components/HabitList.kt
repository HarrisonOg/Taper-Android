package com.harrisonog.taper.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.SampleData

@Composable
fun HabitList(habitList: List<Habit>) {
    LazyColumn {
        items(habitList) { habit ->
            Column(
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                HabitListItem(habit)
            }
        }
    }
}

@Preview
@Composable
fun PreviewHabitList() {
    val sampleDataList: List<Habit> = SampleData().getSampleHabitList()
    HabitList(sampleDataList)
}