package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.ui.HabitListState
import com.harrisonog.taperAndroid.ui.permissions.PermissionChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    state: HabitListState,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Habit) -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Taper") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(pad)) {
                // Show permission request if needed
                PermissionChecker()

                if (state.items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No habits yet. Tap + to create one.")
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.items, key = { it.id }) { habit ->
                            ListItem(
                                headlineContent = {
                                    Text(habit.name + if (!habit.isActive) " (paused)" else "")
                                },
                                supportingContent = {
                                    Text(
                                        (habit.description ?: habit.message),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                trailingContent = {
                                    Text("${habit.startPerDay}→${habit.endPerDay} / ${habit.weeks}w")
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { onOpen(habit.id) }
                                        .padding(horizontal = 8.dp),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
