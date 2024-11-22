package com.harrisonog.taper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class for storing Habit data.
 */
@Entity
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val notificationMessage: String,
    val habitType: HabitType = HabitType.DECREASE,
    val startTaperAlarmsPerDay: Int,
    val endTaperAlarmsPerDay: Int = 1,
    val taperLength: TaperLength,
    val isDone: Boolean = false,
)

data class TaperLength(
    val number: Int,
    val taperLengthTimeScale: TaperLengthTimeScale
)

enum class HabitType {
    INCREASE,
    DECREASE
}

enum class TaperLengthTimeScale{
    DAYS,
    WEEKS,
    MONTHS
}