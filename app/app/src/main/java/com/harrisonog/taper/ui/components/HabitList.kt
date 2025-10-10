package com.harrisonog.taper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.SampleData

@Composable
fun HabitList(habitList: List<Habit>) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(habitList) { habit ->
            HabitListItem(habit)
        }
    }
}

@Preview
@Composable
fun PreviewHabitList() {
    val sampleDataList: List<Habit> = SampleData().getSampleHabitList()
    HabitList(sampleDataList)
}