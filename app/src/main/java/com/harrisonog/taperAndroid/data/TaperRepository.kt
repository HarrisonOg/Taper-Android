package com.harrisonog.taperAndroid.data

import android.content.Context
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitDao
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.data.db.HabitEventDao
import com.harrisonog.taperAndroid.data.settings.AppSettings
import com.harrisonog.taperAndroid.data.settings.observeSettings
import com.harrisonog.taperAndroid.data.settings.saveSettings
import com.harrisonog.taperAndroid.data.settings.updateLastRescheduleTimestamp
import com.harrisonog.taperAndroid.logic.ScheduleGenerator
import com.harrisonog.taperAndroid.scheduling.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalTime

interface TaperRepository {
    fun observeHabits(): Flow<List<Habit>>

    fun observeHabit(id: Long): Flow<Habit?>

    fun observeEvents(habitId: Long): Flow<List<HabitEvent>>

    fun observeSettings(): Flow<AppSettings>

    suspend fun upsertSettings(
        wakeStart: LocalTime,
        wakeEnd: LocalTime,
    )

    suspend fun updateSettingsAndReschedule(
        wakeStart: LocalTime,
        wakeEnd: LocalTime,
    )

    suspend fun createHabitAndPlan(habit: Habit): Long

    suspend fun updateHabitAndPlan(habit: Habit)

    suspend fun updateHabitDetails(habitId: Long, name: String, description: String?, message: String)

    suspend fun deleteHabit(habit: Habit)

    suspend fun rescheduleAllActiveHabits()

    suspend fun shouldRescheduleAll(): Boolean

    suspend fun rescheduleIfNeeded()
}

class DefaultTaperRepository(
    private val context: Context,
    private val habitDao: HabitDao,
    private val eventDao: HabitEventDao,
) : TaperRepository {
    private val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler.create(context)
    }

    // Room-backed domain data
    override fun observeHabits() = habitDao.observeAll()

    override fun observeHabit(id: Long) = habitDao.observe(id)

    override fun observeEvents(habitId: Long) = eventDao.observeForHabit(habitId)

    // DataStore-backed settings
    override fun observeSettings(): Flow<AppSettings> = context.observeSettings()

    override suspend fun upsertSettings(
        wakeStart: LocalTime,
        wakeEnd: LocalTime,
    ) {
        context.saveSettings(AppSettings(wakeStart, wakeEnd))
    }

    override suspend fun updateSettingsAndReschedule(
        wakeStart: LocalTime,
        wakeEnd: LocalTime,
    ) {
        // Update the settings
        context.saveSettings(AppSettings(wakeStart, wakeEnd))

        // Reschedule all active habits with the new wake window
        rescheduleAllActiveHabits()
    }

    override suspend fun createHabitAndPlan(habit: Habit): Long {
        val id = habitDao.insert(habit)
        regenerateEvents(id)
        return id
    }

    override suspend fun updateHabitAndPlan(habit: Habit) {
        habitDao.update(habit)
        regenerateEvents(habit.id)
    }

    override suspend fun updateHabitDetails(habitId: Long, name: String, description: String?, message: String) {
        val habit = habitDao.observe(habitId).first() ?: return
        val updatedHabit = habit.copy(
            name = name.trim(),
            description = description?.trim(),
            message = message.trim()
        )
        habitDao.update(updatedHabit)
    }

    override suspend fun deleteHabit(habit: Habit) {
        // Cancel all scheduled alarms for this habit
        // For AlarmManager, we need to cancel each event individually before deleting
        val existingEvents = eventDao.observeForHabit(habit.id).first()
        val scheduler = alarmScheduler
        if (scheduler is com.harrisonog.taperAndroid.scheduling.AlarmManagerScheduler) {
            existingEvents.forEach { event ->
                scheduler.cancelEvent(event.id)
            }
        } else {
            // For WorkManager, we can cancel by habit tag
            scheduler.cancelEventsForHabit(habit.id)
        }

        // Delete events and habit from database
        eventDao.deleteForHabit(habit.id)
        habitDao.delete(habit)
    }

    override suspend fun rescheduleAllActiveHabits() {
        // First, cancel ALL existing alarms to prevent duplicates
        // This is important when rescheduling after app restart or permission changes

        // Cancel all WorkManager-based notifications
        val scheduler = alarmScheduler
        scheduler.cancelAllEvents()

        // For AlarmManager-based notifications, we need to cancel each event individually
        // Get all events and cancel each alarm
        val allEvents = eventDao.getAll()
        if (scheduler is com.harrisonog.taperAndroid.scheduling.AlarmManagerScheduler) {
            allEvents.forEach { event ->
                scheduler.cancelEvent(event.id)
            }
        }

        // Delete all existing events from database
        habitDao.observeAll().first().forEach { habit ->
            eventDao.deleteForHabit(habit.id)
        }

        // Get all active habits
        val habits = habitDao.observeAll().first()
        val activeHabits = habits.filter { it.isActive }

        // Reschedule each active habit with fresh events
        activeHabits.forEach { habit ->
            regenerateEvents(habit.id, forceReschedule = true)
        }

        // Update the last reschedule timestamp
        context.updateLastRescheduleTimestamp(Instant.now())
    }

    override suspend fun shouldRescheduleAll(): Boolean {
        val settings = observeSettings().first()
        val lastReschedule = settings.lastRescheduleTimestamp ?: return true

        // Check if it's been more than 7 days
        val now = Instant.now()
        val sevenDaysAgo = now.minusSeconds(7 * 24 * 60 * 60)

        return lastReschedule.isBefore(sevenDaysAgo)
    }

    override suspend fun rescheduleIfNeeded() {
        if (shouldRescheduleAll()) {
            rescheduleAllActiveHabits()
        }
    }

    private suspend fun regenerateEvents(
        habitId: Long,
        forceReschedule: Boolean = false
    ) {
        val habit = habitDao.observe(habitId).first() ?: return

        // Skip if habit is not active
        if (!habit.isActive) {
            return
        }

        val settings = observeSettings().first()

        // Cancel existing alarms for this habit
        // For AlarmManager, we need to cancel each event individually before deleting
        val existingEvents = eventDao.observeForHabit(habitId).first()
        val scheduler = alarmScheduler
        if (scheduler is com.harrisonog.taperAndroid.scheduling.AlarmManagerScheduler) {
            existingEvents.forEach { event ->
                scheduler.cancelEvent(event.id)
            }
        } else {
            // For WorkManager, we can cancel by habit tag
            scheduler.cancelEventsForHabit(habitId)
        }

        // Now delete the events from database
        eventDao.deleteForHabit(habitId)

        // Generate new events
        val allEvents =
            ScheduleGenerator.generateUsingPrefs(
                context = context,
                habitId = habitId,
                habit = habit,
            )

        // Filter to only schedule events within the next 7 days to avoid hitting
        // Android's 500 concurrent alarm limit
        val now = Instant.now()
        val fourteenDaysFromNow = now.plusSeconds(7 * 24 * 60 * 60)

        val upcomingEvents = allEvents.filter { event ->
            event.scheduledAt.isAfter(now) && event.scheduledAt.isBefore(fourteenDaysFromNow)
        }

        if (upcomingEvents.isEmpty()) return

        // Insert events into database
        eventDao.insertAll(upcomingEvents)

        // Get the inserted events with their IDs
        val insertedEvents = eventDao.observeForHabit(habitId).first()

        // Schedule alarms for each event
        // Now we always attempt to schedule, even without exact alarm permission
        // The AlarmScheduler will use a fallback mechanism
        insertedEvents.forEach { event ->
            alarmScheduler.scheduleEvent(
                habitId = habitId,
                habitName = habit.name,
                message = habit.message,
                event = event,
            )
        }
    }
}
