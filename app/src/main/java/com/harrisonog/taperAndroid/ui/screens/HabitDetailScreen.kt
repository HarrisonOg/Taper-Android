package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.ui.HabitDetailState
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
        pageCount = { 4 }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.name ?: "Habit") },
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
                        onClick = { habit?.let(onEdit) },
                        enabled = habit != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit habit",
                        )
                    }
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
            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { page ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == page) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }

            // Swipeable content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // Habit Dashboard page (new)
                        HabitDashboard(
                            habit = habit,
                            events = state.events,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        // Weekly Dashboard page
                        Column(Modifier.fillMaxSize()) {
                            WeeklyResponseDashboard(
                                events = state.events,
                                isGoodHabit = habit.isGoodHabit,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    2 -> {
                        // Planned Notifications page
                        Column(Modifier.fillMaxSize()) {
                            HabitPlannedNotificationsSection(state, habit)
                        }
                    }
                    3 -> {
                        // Total Statistics page
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
private fun HabitDetailHeader(habit: Habit) {
    Card(Modifier.padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(habit.description ?: habit.message)
            Text("Plan: ${habit.startPerDay} → ${habit.endPerDay} per day over ${habit.weeks} weeks")
            Text(if (habit.isGoodHabit) "Type: Good (ramp up)" else "Type: Taper down")
            Text(if (habit.isActive) "Status: Active" else "Status: Paused")
        }
    }
}

@Composable
private fun HabitPlannedNotificationsSection(
    state: HabitDetailState,
    habit: Habit,
) {
    Text(
        "Planned notifications",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
    )

    PlannedNotificationsCalendar(
        events = state.events,
        isGoodHabit = habit.isGoodHabit,
        modifier = Modifier.padding(16.dp)
    )
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
                    Text(
                        text = String.format("%.1f%%", successRate),
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
private fun WeeklyResponseDashboard(
    events: List<HabitEvent>,
    isGoodHabit: Boolean,
    modifier: Modifier = Modifier,
) {
    // Get the current week's events (Monday to Sunday)
    val now = LocalDate.now()
    val startOfWeek = now.with(DayOfWeek.MONDAY)
    val endOfWeek = startOfWeek.plusDays(6)

    // Group events by date for the current week
    val weekEvents = events.filter { event ->
        val eventDate = event.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDate()
        !eventDate.isBefore(startOfWeek) && !eventDate.isAfter(endOfWeek)
    }

    val completedCount = weekEvents.count { it.responseType == "completed" }
    val deniedCount = weekEvents.count { it.responseType == "denied" }

    // Color scheme based on habit type
    val completedColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val completedOnColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    val deniedColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val deniedOnColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium
            )

            // Summary counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = completedColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = completedCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = completedOnColor
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = completedOnColor
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = deniedColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = deniedCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = deniedOnColor
                        )
                        Text(
                            text = "Denied",
                            style = MaterialTheme.typography.bodySmall,
                            color = deniedOnColor
                        )
                    }
                }
            }

            // Daily breakdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0..6) {
                    val date = startOfWeek.plusDays(i.toLong())
                    val dayEvents = weekEvents.filter {
                        it.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDate() == date
                    }
                    val dayCompleted = dayEvents.count { it.responseType == "completed" }
                    val dayDenied = dayEvents.count { it.responseType == "denied" }

                    DayResponseRow(
                        date = date,
                        completedCount = dayCompleted,
                        deniedCount = dayDenied,
                        isGoodHabit = isGoodHabit
                    )
                }
            }
        }
    }
}

@Composable
private fun DayResponseRow(
    date: LocalDate,
    completedCount: Int,
    deniedCount: Int,
    isGoodHabit: Boolean,
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val isToday = date == LocalDate.now()

    // Color scheme based on habit type
    val completedColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    val deniedColor = if (isGoodHabit) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) "Today" else date.format(dayFormatter),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completed indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(completedColor)
                )
                Text(
                    text = completedCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = completedColor
                )
            }

            // Denied indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(deniedColor)
                )
                Text(
                    text = deniedCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = deniedColor
                )
            }
        }
    }
}

@Composable
private fun PlannedNotificationsCalendar(
    events: List<HabitEvent>,
    isGoodHabit: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val startDate = remember { currentMonth.minusMonths(2).atStartOfMonth() }
    val endDate = remember { currentMonth.plusMonths(2).atEndOfMonth() }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Track selected date - start with current date selected
    var selectedDate by remember { mutableStateOf<LocalDate?>(currentDate) }

    // Group events by date
    val eventsByDate =
        remember(events) {
            events.groupBy { event ->
                event.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDate()
            }
        }

    // Filter events based on selected date
    val filteredEvents =
        remember(selectedDate, events) {
            selectedDate?.let { date ->
                eventsByDate[date] ?: emptyList()
            } ?: events
        }

    val state =
        rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
            firstDayOfWeek = firstDayOfWeek,
        )

    Column(modifier = modifier) {
        WeekCalendar(
            state = state,
            dayContent = { day ->
                CalendarDay(
                    day = day,
                    isSelected = selectedDate == day.date,
                    hasEvents = eventsByDate.containsKey(day.date),
                    onClick = {
                        selectedDate = if (selectedDate == day.date) null else day.date
                    },
                )
            },
        )

        Spacer(Modifier.height(16.dp))

        // Show header for selected day or all events
        if (selectedDate != null) {
            Text(
                text = "Events for ${selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Show list of filtered events
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (selectedDate != null) "No events scheduled for this day" else "No events scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(filteredEvents) { ev ->
                    EventRow(ev, isGoodHabit)
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: WeekDay,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            if (hasEvents) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            ),
                )
            } else {
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun EventRow(event: HabitEvent, isGoodHabit: Boolean) {
    val fmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    val local = event.scheduledAt.atZone(ZoneId.systemDefault()).toLocalDateTime()

    val statusText = when {
        event.responseType == "completed" -> "✓ Completed"
        event.responseType == "denied" -> "✗ Denied"
        event.responseType == "snoozed" -> "⏰ Snoozed"
        event.isSnoozed -> "Snoozed notification"
        event.sentAt != null -> "Sent"
        else -> "Scheduled"
    }

    val statusColor = when {
        event.responseType == "completed" -> {
            if (isGoodHabit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        }
        event.responseType == "denied" -> {
            if (isGoodHabit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        }
        event.responseType == "snoozed" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    ListItem(
        headlineContent = { Text(fmt.format(local)) },
        supportingContent = {
            Text(
                text = statusText,
                color = statusColor
            )
        },
    )
}

@Composable
private fun HabitDashboard(
    habit: Habit,
    events: List<HabitEvent>,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
    val todayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

    // Get today's events
    val todayEvents = events.filter { event ->
        event.scheduledAt in todayStart..<todayEnd
    }

    val completedToday = todayEvents.count { it.responseType == "completed" }
    val totalToday = todayEvents.size

    // Calculate current week
    val startDate = habit.startDate
    val currentDay = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val currentWeek = ((currentDay - 1) / 7) + 1
    val totalWeeks = habit.weeks

    // Find next alarm
    val now = java.time.Instant.now()
    val upcomingEvents = events.filter { event ->
        event.scheduledAt > now && event.responseType == null
    }.sortedBy { it.scheduledAt }
    val nextAlarm = upcomingEvents.firstOrNull()

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Habit name and type icon
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
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val habitMessage = "Message: ${habit.message}"
                    // Icon
                    Icon(
                        imageVector = if (habit.isGoodHabit) {
                            Icons.Filled.ArrowUpward
                        } else {
                            Icons.Filled.ArrowDownward
                        },
                        contentDescription = if (habit.isGoodHabit) "Ramp up habit" else "Taper down habit",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    // Habit name and type
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = habitMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Today's progress
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedToday / $totalToday completed",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (totalToday > 0) {
                                "${(completedToday.toFloat() / totalToday.toFloat() * 100).toInt()}%"
                            } else {
                                "0%"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { if (totalToday > 0) completedToday.toFloat() / totalToday.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }

        // Current phase indicator
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current Phase",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Week $currentWeek of $totalWeeks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { currentWeek.toFloat() / totalWeeks.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Text(
                        text = "${totalWeeks - currentWeek} ${if (totalWeeks - currentWeek == 1) "week" else "weeks"} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Next alarm
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (nextAlarm != null) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Next Alarm",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (nextAlarm != null) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (nextAlarm != null) {
                        val nextAlarmTime = nextAlarm.scheduledAt
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mm a")

                        Text(
                            text = nextAlarmTime.format(formatter),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        // Time until next alarm
                        val duration = java.time.Duration.between(now, nextAlarm.scheduledAt)
                        val hours = duration.toHours()
                        val minutes = duration.toMinutes() % 60

                        Text(
                            text = when {
                                hours < 1 -> "in $minutes ${if (minutes == 1L) "minute" else "minutes"}"
                                hours < 24 -> "in $hours ${if (hours == 1L) "hour" else "hours"}"
                                else -> {
                                    val days = hours / 24
                                    "in $days ${if (days == 1L) "day" else "days"}"
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "No upcoming alarms",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
