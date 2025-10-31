package com.harrisonog.taper_android.logic

import android.content.Context
import com.harrisonog.taper_android.data.db.Habit
import com.harrisonog.taper_android.data.db.HabitEvent
import com.harrisonog.taper_android.data.settings.AppSettings
import com.harrisonog.taper_android.data.settings.dataStore
import com.harrisonog.taper_android.data.settings.prefsToSettings
import java.time.*
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

data class WakeWindow(val start: LocalTime, val end: LocalTime)

object ScheduleGenerator {

    /**
     * Generates HabitEvents for a habit given explicit user [settings]. The
     * resulting schedule is guaranteed to respect the user's waking window and
     * to change monotonically (never increasing for taper-down habits and never
     * decreasing for ramp-up habits) after rounding to whole reminders per day.
     */
    fun generate(
        habitId: Long,
        habit: Habit,
        settings: AppSettings,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<HabitEvent> {
        require(habit.weeks >= 1)
        val totalDays = habit.weeks * 7
        val start = habit.startPerDay
        val end = habit.endPerDay
        val deltaPerDay =
            if (totalDays <= 1) (end - start).toDouble()
            else (end - start).toDouble() / (totalDays - 1)

        val events = mutableListOf<HabitEvent>()
        var previousCount: Int? = null
        val decreasing = end < start
        val increasing = end > start

        for (dayIdx in 0 until totalDays) {
            val ideal = start + deltaPerDay * dayIdx
            var count = ideal.roundToInt()
            if (decreasing && previousCount != null) {
                count = minOf(count, previousCount!!)
            } else if (increasing && previousCount != null) {
                count = maxOf(count, previousCount!!)
            }

            if (decreasing) {
                val floor = end.coerceAtLeast(0)
                if (count < floor) count = floor
            }
            if (increasing) {
                val ceiling = end
                if (count > ceiling) count = ceiling
            }

            if (dayIdx == totalDays - 1) {
                count = end.coerceAtLeast(0)
            }

            count = count.coerceAtLeast(0)
            previousCount = count
            if (count <= 0) continue

            val date = habit.startDate.plusDays(dayIdx.toLong())
            val dayStart = LocalDateTime.of(date, settings.wakeStart)
            val dayEnd   = LocalDateTime.of(date, settings.wakeEnd)
            val secondsSpan = Duration.between(dayStart, dayEnd).seconds.coerceAtLeast(1)

            for (i in 0 until count) {
                val frac = (i + 0.5) / count.toDouble()
                val secondsOffset = (secondsSpan * frac).toLong()
                val ts = dayStart.plusSeconds(secondsOffset)
                events += HabitEvent(
                    habitId = habitId,
                    scheduledAt = ts.atZone(zone).toInstant()
                )
            }
        }
        return events.sortedBy { it.scheduledAt }
    }

    /**
     * Android-friendly helper — pulls settings from Preferences DataStore
     * (“shared preferences” style) and delegates to the pure version.
     */
    suspend fun generateUsingPrefs(
        context: Context,
        habitId: Long,
        habit: Habit,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<HabitEvent> {
        val prefs = context.dataStore.data.first()
        val settings = prefsToSettings(prefs)
        return generate(habitId, habit, settings, zone)
    }
}