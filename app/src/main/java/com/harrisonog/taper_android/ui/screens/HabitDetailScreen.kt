package com.harrisonog.taper_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harrisonog.taper_android.data.db.HabitEvent
import com.harrisonog.taper_android.ui.HabitDetailState
import com.harrisonog.taper_android.data.db.Habit
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    onBack: () -> Unit,
    onDelete: (Habit) -> Unit
) {
    val habit = state.habit
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.name ?: "Habit") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    IconButton(
                        onClick = { habit?.let(onDelete) },
                        enabled = habit != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete habit"
                        )
                    }
                }
            )
        }
    ) { pad ->
        if (habit == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Loading…")
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(pad)) {
            Card(Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(habit.description ?: habit.message)
                    Text("Plan: ${habit.startPerDay} → ${habit.endPerDay} per day over ${habit.weeks} weeks")
                    Text(if (habit.isGoodHabit) "Type: Good (ramp up)" else "Type: Taper down")
                    Text(if (habit.isActive) "Status: Active" else "Status: Paused")
                }
            }

            Text("Planned notifications", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(state.events) { ev ->
                    EventRow(ev)
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: HabitEvent) {
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    val local = event.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDateTime()
    ListItem(
        headlineContent = { Text(fmt.format(local)) },
        supportingContent = { Text(if (event.sentAt == null) "Scheduled" else "Sent") }
    )
}