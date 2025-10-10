package com.harrisonog.taper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.HabitType
import com.harrisonog.taper.data.TaperLength
import com.harrisonog.taper.data.TaperLengthTimeScale
import com.harrisonog.taper.ui.theme.TaperTheme

@Composable
fun HabitListItem(
    habit: Habit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                HabitTitleText(
                    title = habit.name,
                    modifier = Modifier.weight(1f)
                )
                HabitTypeBadge(habit.habitType)
            }
            HabitDescriptionText(habit.description)
            HabitMetadataRow(habit = habit)
            HabitNotificationMessage(
                message = habit.notificationMessage,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun HabitDescriptionText(
    desc: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun HabitTitleText(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewHabitListItem() {
    TaperTheme {
        HabitListItem(
            Habit(
                name = "Sample Habit 1",
                description = "Reduce habit description 1",
                notificationMessage = "Take 1 gum",
                habitType = HabitType.INCREASE,
                startTaperAlarmsPerDay = 5,
                endTaperAlarmsPerDay = 10,
                taperLength = TaperLength(5, TaperLengthTimeScale.WEEKS),
            )
        )
    }
}