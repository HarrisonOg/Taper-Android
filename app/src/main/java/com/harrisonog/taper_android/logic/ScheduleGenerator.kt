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

/**
 * Immutable representation of the daily wake window used for scheduling.
 */
data class WakeWindow(val start: LocalTime, val end: LocalTime)

object ScheduleGenerator {

    /**
     * Generates the full list of [HabitEvent] instances for the supplied [habit].
     *
     * The counts interpolate between `startPerDay` and `endPerDay` while enforcing
     * monotonic progression (non-decreasing for ramp-ups, non-increasing for tapers).
     * All generated timestamps are evenly distributed within the configured wake
     * window for the provided [zone], and the implementation is careful to respect
     * daylight saving time transitions.
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
            if (totalDays <= 1) (end - start).toDouble() else (end - start).toDouble() / (totalDays - 1)

        val rawCounts = DoubleArray(totalDays) { dayIdx -> start + deltaPerDay * dayIdx }
        val counts = enforceMonotonicCounts(rawCounts, start, end)

        val events = mutableListOf<HabitEvent>()
        for (dayIdx in 0 until totalDays) {
            val count = counts[dayIdx]
            if (count <= 0) continue

            val date = habit.startDate.plusDays(dayIdx.toLong())
            val dayStart = resolve(date, settings.wakeStart, zone)
            val dayEnd = resolve(date, settings.wakeEnd, zone)
            var span = Duration.between(dayStart.toInstant(), dayEnd.toInstant())
            if (span.isNegative || span.isZero) {
                span = Duration.ofSeconds(1)
            }

            val spanMillis = span.toMillis()
            val startInstant = dayStart.toInstant()

            for (i in 0 until count) {
                val fraction = (i + 0.5) / count.toDouble()
                val offsetMillis = ((spanMillis * fraction).toLong()).coerceAtLeast(0)
                val scheduledAt = startInstant.plusMillis(offsetMillis)
                events += HabitEvent(
                    habitId = habitId,
                    scheduledAt = scheduledAt
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

    private fun enforceMonotonicCounts(rawCounts: DoubleArray, start: Int, end: Int): IntArray {
        val totalDays = rawCounts.size
        if (totalDays == 0) return IntArray(0)

        val safeStart = start.coerceAtLeast(0)
        val safeEnd = end.coerceAtLeast(0)
        val counts = IntArray(totalDays) { idx -> rawCounts[idx].roundToInt().coerceAtLeast(0) }

        counts[0] = safeStart
        if (totalDays > 1) {
            counts[totalDays - 1] = safeEnd
        }

        if (safeStart <= safeEnd) {
            for (idx in 1 until totalDays) {
                counts[idx] = counts[idx].coerceAtLeast(counts[idx - 1])
                if (totalDays > 1) {
                    counts[idx] = counts[idx].coerceAtMost(safeEnd)
                }
            }
            if (totalDays > 1) {
                counts[totalDays - 1] = safeEnd
            }
        } else {
            if (totalDays > 1) {
                counts[totalDays - 1] = safeEnd
            }
            for (idx in totalDays - 2 downTo 0) {
                counts[idx] = counts[idx].coerceAtLeast(counts[idx + 1])
                counts[idx] = counts[idx].coerceAtMost(safeStart)
            }
            counts[0] = counts[0].coerceAtLeast(safeStart)
            if (totalDays > 1) {
                counts[totalDays - 1] = safeEnd
            }
        }

        return counts
    }

    private fun resolve(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime {
        val localDateTime = LocalDateTime.of(date, time)
        val rules = zone.rules
        val transition = rules.getTransition(localDateTime)
        if (transition != null && transition.isGap) {
            return ZonedDateTime.ofInstant(transition.instant, zone)
        }

        val zoned = ZonedDateTime.ofLocal(localDateTime, zone, null)
        return if (transition != null && transition.isOverlap) {
            zoned.withLaterOffsetAtOverlap()
        } else {
            zoned
        }
    }
}