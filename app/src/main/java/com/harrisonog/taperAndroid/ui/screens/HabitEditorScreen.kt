package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.ui.theme.GoodHabitPrimary
import com.harrisonog.taperAndroid.ui.theme.TaperHabitPrimary
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditorScreen(
    onSave: (HabitDraft, onDone: () -> Unit) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Time for a quick walk!") }
    var startPerDay by remember { mutableIntStateOf(3) }
    var endPerDay by remember { mutableIntStateOf(0) }
    var weeks by remember { mutableIntStateOf(4) }
    var isGood by remember { mutableStateOf(false) } // default "taper down"
    var startPerDayError by remember { mutableStateOf(false) }
    var endPerDayError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val canSave = name.isNotBlank() && weeks >= 1 && startPerDay >= 0 && endPerDay >= 0 && !startPerDayError && !endPerDayError

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Habit") }) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    "Start/day",
                    startPerDay,
                    {
                        startPerDay = it.coerceAtLeast(0)
                        startPerDayError = false
                        errorMessage = null
                    },
                    Modifier.weight(1f),
                    isError = startPerDayError
                )
                NumberField(
                    "End/day",
                    endPerDay,
                    {
                        endPerDay = it.coerceAtLeast(0)
                        endPerDayError = false
                        errorMessage = null
                    },
                    Modifier.weight(1f),
                    isError = endPerDayError
                )
                NumberField("Weeks", weeks, { weeks = it.coerceAtLeast(1) }, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Taper habit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isGood) TaperHabitPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    checked = isGood,
                    onCheckedChange = { isGood = it }
                )
                Text(
                    text = "Good habit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGood) GoodHabitPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    enabled = canSave,
                    onClick = {
                        // Validate events per day before saving
                        val startExceedsLimit = startPerDay > 15
                        val endExceedsLimit = endPerDay > 15

                        if (startExceedsLimit || endExceedsLimit) {
                            startPerDayError = startExceedsLimit
                            endPerDayError = endExceedsLimit
                            errorMessage = "Cannot set more than 15 alarms per day"
                            return@Button
                        }

                        onSave(
                            HabitDraft(
                                name,
                                desc.ifBlank { null },
                                message,
                                startPerDay,
                                endPerDay,
                                weeks,
                                isGood,
                                LocalDate.now(),
                            ),
                        ) {}
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { s ->
            text = s
            s.toIntOrNull()?.let { onChange(it) }
        },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        isError = isError,
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { text = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    )
}

data class HabitDraft(
    val name: String,
    val description: String?,
    val message: String,
    val startPerDay: Int,
    val endPerDay: Int,
    val weeks: Int,
    val isGoodHabit: Boolean,
    val startDate: LocalDate,
)

// Helper to pass draft back into VM create()
fun HabitDraft.toEntity() =
    Habit(
        name = name,
        description = description,
        message = message,
        startPerDay = startPerDay,
        endPerDay = endPerDay,
        weeks = weeks,
        isGoodHabit = isGoodHabit,
        startDate = startDate,
    )
