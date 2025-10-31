package com.harrisonog.taper_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harrisonog.taper_android.ui.HabitListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    state: HabitListState,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Taper") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { pad ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No habits yet. Tap + to create one.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                items(state.items) { habit ->
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
                    HorizontalDivider()
                }
            }
        }
    }
}
