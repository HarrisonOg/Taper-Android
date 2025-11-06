package com.harrisonog.taperAndroid.data

import android.content.Context
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitDao
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.data.db.HabitEventDao
import com.harrisonog.taperAndroid.data.settings.AppSettings
import com.harrisonog.taperAndroid.data.settings.observeSettings
import com.harrisonog.taperAndroid.data.settings.saveSettings
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

    suspend fun createHabitAndPlan(habit: Habit): Long

    suspend fun updateHabitAndPlan(habit: Habit)

    suspend fun updateHabitDetails(habitId: Long, name: String, description: String?, message: String)

    suspend fun deleteHabit(habit: Habit)

    suspend fun rescheduleAllActiveHabits()
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
        alarmScheduler.cancelEventsForHabit(habit.id)
        eventDao.deleteForHabit(habit.id)
        habitDao.delete(habit)
    }

    override suspend fun rescheduleAllActiveHabits() {
        // Get all active habits
        val habits = habitDao.observeAll().first()
        val activeHabits = habits.filter { it.isActive }

        // Reschedule each active habit
        activeHabits.forEach { habit ->
            regenerateEvents(habit.id, forceReschedule = true)
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
        alarmScheduler.cancelEventsForHabit(habitId)
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
