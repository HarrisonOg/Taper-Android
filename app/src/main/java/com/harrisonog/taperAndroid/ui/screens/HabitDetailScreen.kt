package com.harrisonog.taperAndroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailState,
    onBack: () -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
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
            HabitDetailHeader(habit)

            // Weekly Dashboard
            WeeklyResponseDashboard(
                events = state.events,
                isGoodHabit = habit.isGoodHabit,
                modifier = Modifier.padding(16.dp)
            )

            HabitPlannedNotificationsSection(state, habit)
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
