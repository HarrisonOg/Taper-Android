package com.harrisonog.taperAndroid.logic

import android.content.Context
import com.harrisonog.taperAndroid.data.db.Habit
import com.harrisonog.taperAndroid.data.db.HabitEvent
import com.harrisonog.taperAndroid.data.settings.AppSettings
import com.harrisonog.taperAndroid.data.settings.dataStore
import com.harrisonog.taperAndroid.data.settings.prefsToSettings
import kotlinx.coroutines.flow.first
import java.time.*
import kotlin.math.roundToInt

data class WakeWindow(val start: LocalTime, val end: LocalTime)

object ScheduleGenerator {
    /**
     * PURE version — pass settings explicitly (recommended for tests).
     */
    fun generate(
        habitId: Long,
        habit: Habit,
        settings: AppSettings,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<HabitEvent> {
        require(habit.weeks >= 1)
        val totalDays = habit.weeks * 7
        val start = habit.startPerDay
        val end = habit.endPerDay
        val deltaPerDay =
            if (totalDays <= 1) {
                (end - start).toDouble()
            } else {
                (end - start).toDouble() / (totalDays - 1)
            }

        val events = mutableListOf<HabitEvent>()
        for (dayIdx in 0 until totalDays) {
            val count = (start + deltaPerDay * dayIdx).roundToInt().coerceAtLeast(0)
            if (count <= 0) continue

            val date = habit.startDate.plusDays(dayIdx.toLong())
            val dayStart = LocalDateTime.of(date, settings.wakeStart)
            val dayEnd = LocalDateTime.of(date, settings.wakeEnd)
            val secondsSpan = Duration.between(dayStart, dayEnd).seconds.coerceAtLeast(1)

            for (i in 0 until count) {
                val frac = (i + 0.5) / count.toDouble()
                val secondsOffset = (secondsSpan * frac).toLong()
                val ts = dayStart.plusSeconds(secondsOffset)
                events +=
                    HabitEvent(
                        habitId = habitId,
                        scheduledAt = ts.atZone(zone).toInstant(),
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
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<HabitEvent> {
        val prefs = context.dataStore.data.first()
        val settings = prefsToSettings(prefs)
        return generate(habitId, habit, settings, zone)
    }
}
