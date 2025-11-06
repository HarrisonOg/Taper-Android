package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(
    habit: Habit?,
    onSave: (String, String?, String) -> Unit,
    onCancel: () -> Unit,
) {
    if (habit == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Loading…")
        }
        return
    }

    var name by remember { mutableStateOf(habit.name) }
    var desc by remember { mutableStateOf(habit.description ?: "") }
    var message by remember { mutableStateOf(habit.message) }
    val canSave = name.isNotBlank()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Habit") }) },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                name,
                { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
            OutlinedTextField(
                desc,
                { desc = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (desc.isNotEmpty()) {
                        IconButton(onClick = { desc = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
            OutlinedTextField(
                message,
                { message = it },
                label = { Text("Notification message") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (message.isNotEmpty()) {
                        IconButton(onClick = { message = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    enabled = canSave,
                    onClick = {
                        onSave(name, desc.ifBlank { null }, message)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}
