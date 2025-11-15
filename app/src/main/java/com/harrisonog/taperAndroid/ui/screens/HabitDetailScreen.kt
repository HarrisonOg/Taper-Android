package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.ui.HabitDetailState
import com.harrisonog.taperAndroid.ui.theme.GoodHabitPrimary
import com.harrisonog.taperAndroid.ui.theme.TaperHabitPrimary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    onBack: () -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
) {
    val habit = state.habit
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { habit?.let(onDelete) },
                        enabled = habit != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete habit",
                        )
                    }
                },
            )
        },
    ) { pad ->
        if (habit == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Loading…")
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(pad)) {
            // Tab strip
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text("Dashboard") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text("Statistics") }
                )
            }

            // Swipeable content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Main Dashboard page
                        HabitDashboard(
                            habit = habit,
                            events = state.events,
                            onEdit = onEdit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        // Overall Statistics page
                        Column(Modifier.fillMaxSize()) {
                            HabitStatisticsPage(
                                habit = habit,
                                events = state.events,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitStatisticsPage(
    habit: Habit,
    events: List<HabitEvent>,
    modifier: Modifier = Modifier,
) {
    val startDate = habit.startDate
    val today = LocalDate.now()
    val currentDay = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val totalDays = habit.weeks * 7
    val daysLeft = (totalDays - currentDay + 1).coerceAtLeast(0)

    // Calculate statistics
    val completedCount = events.count { it.responseType == "completed" }
    val deniedCount = events.count { it.responseType == "denied" }
    val snoozedCount = events.count { it.responseType == "snoozed" }

    // Find events that were snoozed and then had a follow-up response
    val snoozedEventIds = events.filter { it.responseType == "snoozed" }.map { it.id }.toSet()
    val followUpEvents = events.filter { it.isSnoozed }
    val snoozedThenCompleted = followUpEvents.count { it.responseType == "completed" }
    val snoozedThenDenied = followUpEvents.count { it.responseType == "denied" }

    // Calculate success rate
    val totalResponded = completedCount + deniedCount
    val successRate = if (totalResponded > 0) {
        if (habit.isGoodHabit) {
            (completedCount.toFloat() / totalResponded.toFloat()) * 100
        } else {
            (deniedCount.toFloat() / totalResponded.toFloat()) * 100
        }
    } else {
        0f
    }

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "Overall Statistics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Current Day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Day $currentDay of $totalDays",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Days Remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$daysLeft days",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Success Rate Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (successRate >= 70) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (successRate >= 40) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (successRate >= 70) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else if (successRate >= 40) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    val successRateString = String.format(Locale.getDefault(),"%.1f%%", successRate)
                    Text(
                        text = successRateString,
                        style = MaterialTheme.typography.displayMedium,
                        color = if (successRate >= 70) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else if (successRate >= 40) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = if (habit.isGoodHabit) {
                            "Completed / Total Responses"
                        } else {
                            "Denied / Total Responses"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (successRate >= 70) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else if (successRate >= 40) {
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }

        // Response Statistics Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Response Statistics",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Completed
                    StatisticRow(
                        label = "Completed",
                        count = completedCount,
                        color = if (habit.isGoodHabit) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )

                    // Denied
                    StatisticRow(
                        label = "Denied",
                        count = deniedCount,
                        color = if (habit.isGoodHabit) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )

                    // Snoozed
                    StatisticRow(
                        label = "Snoozed",
                        count = snoozedCount,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    if (snoozedCount > 0) {
                        // Indented sub-statistics for snoozed events
                        Column(
                            modifier = Modifier.padding(start = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatisticRow(
                                label = "→ Then Completed",
                                count = snoozedThenCompleted,
                                color = if (habit.isGoodHabit) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                isSecondary = true
                            )

                            StatisticRow(
                                label = "→ Then Denied",
                                count = snoozedThenDenied,
                                color = if (habit.isGoodHabit) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                isSecondary = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticRow(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    isSecondary: Boolean = false,
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
                    .size(if (isSecondary) 6.dp else 10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = if (isSecondary) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }
        Text(
            text = count.toString(),
            style = if (isSecondary) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = color
        )
    }
}

@Composable
private fun HabitDashboard(
    habit: Habit,
    events: List<HabitEvent>,
    onEdit: (Habit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
    val todayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

    // Get today's events
    val todayEvents = events.filter { event ->
        event.scheduledAt in todayStart..<todayEnd
    }.sortedBy { it.scheduledAt }

    val confirmedToday = todayEvents.count { it.responseType == "completed" }
    val totalToday = todayEvents.size

    // Calculate current week and day
    val startDate = habit.startDate
    val currentDay = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val currentWeek = ((currentDay - 1) / 7) + 1
    val totalWeeks = habit.weeks
    val totalDays = totalWeeks * 7

    // Habit-specific colors
    val habitPrimary = if (habit.isGoodHabit) GoodHabitPrimary else TaperHabitPrimary

    // State for collapsible Today's Alarms section
    var isAlarmsExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Habit type indicator icon
                    Icon(
                        imageVector = if (habit.isGoodHabit) {
                            Icons.Filled.ArrowUpward
                        } else {
                            Icons.Filled.ArrowDownward
                        },
                        contentDescription = if (habit.isGoodHabit) "Good habit" else "Taper habit",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(habitPrimary)
                            .padding(12.dp),
                        tint = androidx.compose.ui.graphics.Color.White
                    )

                    // Habit name and description
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (habit.description != null) {
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Edit button
                    IconButton(onClick = { onEdit(habit) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit habit",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 2. Progress Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Progress Overview",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Current week and day
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Week $currentWeek of $totalWeeks",
                                style = MaterialTheme.typography.bodyLarge,
                                color = habitPrimary
                            )
                            Text(
                                text = "Day $currentDay of $totalDays",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Two circular progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Today's alarms progress
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { if (totalToday > 0) confirmedToday.toFloat() / totalToday.toFloat() else 0f },
                                    modifier = Modifier.size(100.dp),
                                    color = habitPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 8.dp
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$confirmedToday/$totalToday",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = habitPrimary
                                    )
                                }
                            }
                            Text(
                                text = "Today's Alarms",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Overall taper period progress
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { currentDay.toFloat() / totalDays.toFloat() },
                                    modifier = Modifier.size(100.dp),
                                    color = habitPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 8.dp
                                )
                                Text(
                                    text = "${(currentDay.toFloat() / totalDays.toFloat() * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = habitPrimary
                                )
                            }
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Today's Alarms Section (Collapsible)
        item {
            val arrowRotation by animateFloatAsState(
                targetValue = if (isAlarmsExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "arrow rotation"
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clickable header with arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAlarmsExpanded = !isAlarmsExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Alarms",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isAlarmsExpanded) "Collapse" else "Expand",
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }

                    // Collapsible content
                    if (isAlarmsExpanded) {
                        if (todayEvents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No alarms scheduled for today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            // Timeline view of today's alarms
                            Column(
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                todayEvents.forEachIndexed { index, event ->
                                    AlarmTimelineItem(
                                        event = event,
                                        isGoodHabit = habit.isGoodHabit,
                                        isLast = index == todayEvents.lastIndex
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmTimelineItem(
    event: HabitEvent,
    isGoodHabit: Boolean,
    isLast: Boolean
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val alarmTime = event.scheduledAt
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val (statusText, statusColor, statusIcon) = when {
        event.responseType == "completed" -> Triple(
            "Confirmed",
            if (isGoodHabit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            "✓"
        )
        event.responseType == "denied" -> Triple(
            "Denied",
            if (isGoodHabit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            "✗"
        )
        event.responseType == "snoozed" -> Triple(
            "Snoozed",
            MaterialTheme.colorScheme.tertiary,
            "⏰"
        )
        event.scheduledAt < java.time.Instant.now() -> Triple(
            "Missed",
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            "○"
        )
        else -> Triple(
            "Pending",
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            "○"
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Status indicator circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusIcon,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            // Vertical line to next item
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            }
        }

        // Alarm details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Text(
                text = alarmTime.format(timeFormatter),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
    }
}
