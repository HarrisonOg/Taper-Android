package com.harrisonog.taper.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.harrisonog.taper.data.models.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    isNewHabit: Boolean = true,
    habit: Habit? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    val title = if (isNewHabit) "New Habit" else "Edit Habit"

    var habitName : String by remember { mutableStateOf("Enter name") }
    var habitDescription  : String by remember { mutableStateOf("Enter description") }
    var habitNotificationMsg  : String by remember { mutableStateOf("Enter notification message") }
    var habitType : Int by remember { mutableIntStateOf(0) }
    val habitTypeOptions = listOf("Increase", "Decrease")
    var habitStartPerDay : Int by remember { mutableIntStateOf(0) }
    var habitTaperLength : Int by remember { mutableIntStateOf(0) }
    var habitTaperLengthTimeUnit : Int by remember { mutableIntStateOf(0) }
    val habitTaperLengthTimeUnitOptions = listOf("Days", "Weeks")

    MaterialTheme {
        Column(
            modifier = modifier.background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                ).padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title)
            Row {
                TextField(
                    value = habitName,
                    onValueChange = { it: String ->
                        habitName = it                    },
                    label = { Text("Name of habit: ") },
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                TextField(
                    value = habitDescription,
                    onValueChange = { it: String ->
                        habitDescription = it
                    },
                    label = { Text("Habit description: ") },
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                TextField(
                    value = habitNotificationMsg,
                    onValueChange = { it: String ->
                        habitNotificationMsg = it
                    },
                    label = { Text("Habit notification message: ") },
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            // Habit Type
            SingleChoiceSegmentedButtonRow {
                habitTypeOptions.forEachIndexed{ index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = habitTypeOptions.size),
                        onClick = { habitType = index },
                        selected = index == habitType
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                TextField(
                    value = habitStartPerDay.toString(),
                    onValueChange = { it : String ->
                        if (it.isDigitsOnly()) {
                            habitStartPerDay = it.toInt()
                        }
                    },
                    label = {
                        Text("Enter habit alarms per day at start of taper")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Habit start alarms per day
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                // Habit taper length
                TextField(
                    value = habitTaperLength.toString(),
                    onValueChange = { it : String ->
                        if (it.isDigitsOnly()) {
                            habitTaperLength = it.toInt()
                        }
                    },
                    label = {
                        Text("Enter habit taper length")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            // Habit taper length time scale
            SingleChoiceSegmentedButtonRow {
                habitTaperLengthTimeUnitOptions.forEachIndexed{ index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = habitTaperLengthTimeUnitOptions.size),
                        onClick = { habitTaperLengthTimeUnit = index },
                        selected = index == habitTaperLengthTimeUnit
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }

}


@Preview
@Composable
fun EditHabitScreenPreview() {
    EditHabitScreen(
        isNewHabit = true,
    )
}