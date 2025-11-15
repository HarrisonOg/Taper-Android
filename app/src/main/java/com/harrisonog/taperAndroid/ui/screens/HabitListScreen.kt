package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.ui.DashboardStats
import com.harrisonog.taperAndroid.ui.HabitListState
import com.harrisonog.taperAndroid.ui.HabitWithStats
import com.harrisonog.taperAndroid.ui.permissions.PermissionChecker
import com.harrisonog.taperAndroid.ui.theme.GoodHabitContainer
import com.harrisonog.taperAndroid.ui.theme.GoodHabitOnContainer
import com.harrisonog.taperAndroid.ui.theme.GoodHabitPrimary
import com.harrisonog.taperAndroid.ui.theme.TaperHabitContainer
import com.harrisonog.taperAndroid.ui.theme.TaperHabitOnContainer
import com.harrisonog.taperAndroid.ui.theme.TaperHabitPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    state: HabitListState,
    dashboardStats: DashboardStats,
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

                // Show daily dashboard
                DailyDashboard(
                    dashboardStats = dashboardStats,
                    habits = state.items.map { it.habit },
                )

                if (state.items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No habits yet. Tap + to create one.")
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.items, key = { it.habit.id }) { habitWithStats ->
                            EnhancedHabitListItem(
                                habitWithStats = habitWithStats,
                                onClick = { onOpen(habitWithStats.habit.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDashboard(
    dashboardStats: DashboardStats,
    habits: List<Habit>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total alarms today
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dashboardStats.totalAlarmsToday.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Completed today
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dashboardStats.completedToday.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Streaks section
            if (dashboardStats.habitStreaks.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )

                Text(
                    text = "Current Streaks",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    habits.forEach { habit ->
                        val streak = dashboardStats.habitStreaks[habit.id] ?: 0
                        if (streak > 0) {
                            StreakRow(
                                habitName = habit.name,
                                streak = streak,
                                isActive = habit.isActive
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakRow(
    habitName: String,
    streak: Int,
    isActive: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        }
                    )
            )
            Text(
                text = habitName + if (!isActive) " (paused)" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "\uD83D\uDD25",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$streak ${if (streak == 1) "day" else "days"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EnhancedHabitListItem(
    habitWithStats: HabitWithStats,
    onClick: () -> Unit,
) {
    val habit = habitWithStats.habit

    // Habit-specific colors
    val habitPrimary = if (habit.isGoodHabit) GoodHabitPrimary else TaperHabitPrimary
    val habitContainer = if (habit.isGoodHabit) GoodHabitContainer else TaperHabitContainer
    val habitOnContainer = if (habit.isGoodHabit) GoodHabitOnContainer else TaperHabitOnContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Habit name and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = if (habit.isGoodHabit) {
                        Icons.Filled.ArrowUpward
                    } else {
                        Icons.Filled.ArrowDownward
                    },
                    contentDescription = if (habit.isGoodHabit) "Building habit" else "Reducing habit",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(habitContainer)
                        .padding(6.dp),
                    tint = habitOnContainer
                )

                // Habit name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name + if (!habit.isActive) " (paused)" else "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Today's progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today: ${habitWithStats.completedToday}/${habitWithStats.totalToday} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = {
                            if (habitWithStats.totalToday > 0)
                                habitWithStats.completedToday.toFloat() / habitWithStats.totalToday.toFloat()
                            else 0f
                        },
                        modifier = Modifier.size(48.dp),
                        color = habitPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = if (habitWithStats.totalToday > 0) {
                            "${(habitWithStats.completedToday.toFloat() / habitWithStats.totalToday.toFloat() * 100).toInt()}%"
                        } else {
                            "0%"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = habitPrimary
                    )
                }
            }

            // Phase and Next Alarm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current phase
                Text(
                    text = "Week ${habitWithStats.currentWeek} of ${habitWithStats.totalWeeks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Next alarm
                if (habitWithStats.nextAlarmTime != null) {
                    val now = java.time.Instant.now()
                    val duration = java.time.Duration.between(now, habitWithStats.nextAlarmTime)
                    val hours = duration.toHours()

                    val timeText = when {
                        hours < 1 -> {
                            val minutes = duration.toMinutes()
                            "${minutes}m"
                        }
                        hours < 24 -> "${hours}h"
                        else -> {
                            val days = hours / 24
                            "${days}d"
                        }
                    }

                    Text(
                        text = "Next: $timeText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "No upcoming",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
