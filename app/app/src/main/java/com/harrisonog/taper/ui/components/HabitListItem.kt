package com.harrisonog.taper.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.HabitType
import com.harrisonog.taper.data.TaperLength
import com.harrisonog.taper.data.TaperLengthTimeScale

@Composable
fun HabitListItem(habit: Habit) {
    Surface(
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.size(width = 360.dp, height = 100.dp),
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp),
        ) {
            Row {
                HabitTitleText(habit.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row{
                HabitDescriptionText(habit.description)
            }
        }
    }
}

@Composable
fun HabitDescriptionText(desc: String) {
    Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun HabitTitleText(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
}

@Preview
@Composable
fun PreviewHabitListItem() {
    HabitListItem(
        Habit(
            name = "Sample Habit 1",
            description = "Reduce habit description 1",
            notificationMessage = "Take 1 gum",
            habitType = HabitType.INCREASE,
            startTaperAlarmsPerDay = 5,
            taperLength = TaperLength(5, TaperLengthTimeScale.WEEKS),
            )
    )
}