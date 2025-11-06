package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
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
    val canSave = name.isNotBlank() && weeks >= 1 && startPerDay >= 0 && endPerDay >= 0

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
            )
            OutlinedTextField(
                desc,
                { desc = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(message, {
                message = it
            }, label = { Text("Notification message") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Start/day", startPerDay, { startPerDay = it.coerceAtLeast(0) }, Modifier.weight(1f))
                NumberField("End/day", endPerDay, { endPerDay = it.coerceAtLeast(0) }, Modifier.weight(1f))
                NumberField("Weeks", weeks, { weeks = it.coerceAtLeast(1) }, Modifier.weight(1f))
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isGood, onCheckedChange = { isGood = it })
                Text("Good habit (ramp up). Unchecked = taper down.")
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    enabled = canSave,
                    onClick = {
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
    val startDate: java.time.LocalDate,
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
