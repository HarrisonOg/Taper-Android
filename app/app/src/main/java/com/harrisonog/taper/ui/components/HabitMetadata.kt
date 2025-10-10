package com.harrisonog.taper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.HabitType
import com.harrisonog.taper.data.TaperLength
import com.harrisonog.taper.data.TaperLengthTimeScale

@Composable
fun HabitMetadataRow(
    habit: Habit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HabitTypeBadge(habit.habitType)
        HabitScheduleSummary(
            startPerDay = habit.startTaperAlarmsPerDay,
            endPerDay = habit.endTaperAlarmsPerDay,
            taperLength = habit.taperLength,
        )
    }
}

@Composable
fun HabitTypeBadge(
    habitType: HabitType,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val background = when (habitType) {
        HabitType.INCREASE -> colors.tertiaryContainer
        HabitType.DECREASE -> colors.primaryContainer
    }
    val foreground = when (habitType) {
        HabitType.INCREASE -> colors.onTertiaryContainer
        HabitType.DECREASE -> colors.onPrimaryContainer
    }

    Surface(
        modifier = modifier,
        color = background,
        contentColor = foreground,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = when (habitType) {
                HabitType.INCREASE -> "Increase"
                HabitType.DECREASE -> "Decrease"
            },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun HabitScheduleSummary(
    startPerDay: Int,
    endPerDay: Int,
    taperLength: TaperLength,
    modifier: Modifier = Modifier,
) {
    val scheduleText = buildString {
        append("$startPerDay → $endPerDay per day")
        append(" • ")
        append(
            when (taperLength.taperLengthTimeScale) {
                TaperLengthTimeScale.DAYS -> "${taperLength.number} day"
                TaperLengthTimeScale.WEEKS -> "${taperLength.number} week"
                TaperLengthTimeScale.MONTHS -> "${taperLength.number} month"
            }
        )
        if (taperLength.number != 1) {
            append("s")
        }
    }

    Text(
        text = scheduleText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun HabitNotificationMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun HabitMetadataRowPreview() {
    val sampleHabit = Habit(
        name = "Morning Walk",
        description = "Take a short walk before work",
        notificationMessage = "Get moving!",
        habitType = HabitType.INCREASE,
        startTaperAlarmsPerDay = 1,
        endTaperAlarmsPerDay = 2,
        taperLength = TaperLength(4, TaperLengthTimeScale.WEEKS)
    )
    HabitMetadataRow(habit = sampleHabit)
}

@Preview(showBackground = true)
@Composable
private fun HabitNotificationPreview() {
    HabitNotificationMessage(message = "Take a break and stretch")
}
