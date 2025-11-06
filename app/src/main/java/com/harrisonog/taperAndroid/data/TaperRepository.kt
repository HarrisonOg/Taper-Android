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

    suspend fun deleteHabit(habit: Habit)
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

    override suspend fun deleteHabit(habit: Habit) {
        // Cancel all scheduled alarms for this habit
        alarmScheduler.cancelEventsForHabit(habit.id)
        eventDao.deleteForHabit(habit.id)
        habitDao.delete(habit)
    }

    private suspend fun regenerateEvents(habitId: Long) {
        val habit = habitDao.observe(habitId).first() ?: return

        // Only schedule alarms if habit is active and scheduler can schedule exact alarms
        if (!habit.isActive || !alarmScheduler.canScheduleExactAlarms()) {
            return
        }

        val settings = observeSettings().first()

        // Cancel existing alarms for this habit
        alarmScheduler.cancelEventsForHabit(habitId)
        eventDao.deleteForHabit(habitId)

        // Generate new events
        val events =
            ScheduleGenerator.generateUsingPrefs(
                context = context,
                habitId = habitId,
                habit = habit,
            )

        if (events.isEmpty()) return

        // Insert events into database
        eventDao.insertAll(events)

        // Get the inserted events with their IDs
        val insertedEvents = eventDao.observeForHabit(habitId).first()

        // Schedule alarms for each event
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
