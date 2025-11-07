package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.settings.AppSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings?,
    onSave: (LocalTime, LocalTime) -> Unit,
    onBack: () -> Unit,
) {
    if (settings == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…")
        }
        return
    }

    var wakeStart by remember { mutableStateOf(settings.wakeStart) }
    var wakeEnd by remember { mutableStateOf(settings.wakeEnd) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Wake Window",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Set the time window when you're awake. Habit notifications will be scheduled during this time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Wake Start Time
            OutlinedCard(
                onClick = { showStartTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Wake Start", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Time you wake up",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        wakeStart.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Wake End Time
            OutlinedCard(
                onClick = { showEndTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Wake End", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Time you go to sleep",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        wakeEnd.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Save Button
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(wakeStart, wakeEnd)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }

    // Time Pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = wakeStart,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { time ->
                wakeStart = time
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = wakeEnd,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { time ->
                wakeEnd = time
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onConfirm(time)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
