package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.ui.HabitListState
import com.harrisonog.taperAndroid.ui.permissions.PermissionChecker
import kotlinx.coroutines.launch

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
        // Track which habit is currently swiped to reveal delete button
        var swipedHabitId by remember { mutableStateOf<Long?>(null) }

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
                        val dismissState =
                            rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            // Update swiped habit when this one is swiped
                                            swipedHabitId = habit.id
                                            true
                                        }
                                        SwipeToDismissBoxValue.Settled -> {
                                            // Allow resetting to settled state
                                            true
                                        }
                                        else -> false
                                    }
                                },
                                positionalThreshold = { distance -> distance * 0.75f },
                            )
                        val coroutineScope = rememberCoroutineScope()

                        // Reset this item if another habit is swiped or if swipedHabitId is cleared
                        LaunchedEffect(swipedHabitId) {
                            if (swipedHabitId != habit.id) {
                                dismissState.reset()
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    IconButton(
                                        onClick = {
                                            onDelete(habit)
                                            coroutineScope.launch {
                                                dismissState.reset()
                                                swipedHabitId = null
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete habit",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                        ) {
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
                                        .clickable {
                                            // If swiped, dismiss swipe; otherwise open detail
                                            if (swipedHabitId == habit.id) {
                                                swipedHabitId = null
                                            } else {
                                                onOpen(habit.id)
                                            }
                                        }
                                        .padding(horizontal = 8.dp),
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

            // Transparent overlay when any item is swiped
            // Tapping it dismisses the swiped item
            if (swipedHabitId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            swipedHabitId = null
                        }
                )
            }
        }
    }
}
