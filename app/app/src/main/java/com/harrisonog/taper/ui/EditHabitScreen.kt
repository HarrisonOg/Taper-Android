package com.harrisonog.taper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.harrisonog.taper.data.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreen(
    isNewHabit: Boolean = true,
    habit: Habit? = null,
    modifier: Modifier = Modifier,
) {
    val title = if (isNewHabit) "New Habit" else "Edit Habit"

    var habitName : String by remember { mutableStateOf("Enter name") }

    Column(
        modifier = modifier,
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
                colors = TextFieldDefaults.colors()
            )
        }
    }
}


@Preview
@Composable
fun EditHabitScreenPreview() {
    EditHabitScreen()
}