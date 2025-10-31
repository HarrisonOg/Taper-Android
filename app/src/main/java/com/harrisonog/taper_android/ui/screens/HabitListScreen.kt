package com.harrisonog.taper_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harrisonog.taper_android.ui.HabitListState
import com.harrisonog.taper_android.data.db.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    state: HabitListState,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Habit) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Taper") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { pad ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("No habits yet. Tap + to create one.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                items(state.items, key = { it.id }) { habit ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(habit)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete habit",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(habit.name + if (!habit.isActive) " (paused)" else "")
                            },
                            supportingContent = {
                                Text(
                                    (habit.description ?: habit.message),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                Text("${habit.startPerDay}→${habit.endPerDay} / ${habit.weeks}w")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(habit.id) }
                                .padding(horizontal = 8.dp)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
