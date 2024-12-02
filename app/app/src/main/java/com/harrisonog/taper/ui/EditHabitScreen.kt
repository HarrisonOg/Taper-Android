package com.harrisonog.taper.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harrisonog.taper.data.Habit
import com.harrisonog.taper.data.TaperLengthTimeUnit

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
    var habitType : Boolean by remember { mutableStateOf(true) }
    var habitStartPerDay : Int by remember { mutableIntStateOf(0) }
    var habitTaperLength : Int by remember { mutableIntStateOf(0) }
    var habitTaperLengthTimeUnit : TaperLengthTimeUnit by remember { mutableStateOf(TaperLengthTimeUnit.WEEKS) }

    MaterialTheme {
        Column(
            modifier = modifier.background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
            ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title)
            Row {
                TextField(
                    value = habitName,
                    onValueChange = { it: String ->
                        habitName = it
                    },
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

            Row {
                // Habit Type
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                // Habit start alarms per day
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                // Habit taper length
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row {
                // Habit taper length time scale
            }
        }
    }

}


@Preview
@Composable
fun EditHabitScreenPreview() {
    EditHabitScreen()
}