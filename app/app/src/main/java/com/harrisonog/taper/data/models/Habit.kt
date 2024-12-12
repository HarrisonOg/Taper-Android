package com.harrisonog.taper.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.harrisonog.taper.utils.Constants.DATABASE_TABLE

/**
 * Class for storing Habit data.
 */
@Entity(tableName = DATABASE_TABLE)
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val notificationMessage: String,
    val habitType: HabitType = HabitType.DECREASE,
    val startTaperAlarmsPerDay: Int,
    val endTaperAlarmsPerDay: Int = 1,
    val taperLength: Int,
    val taperLengthTimeUnit: TaperLengthTimeUnit = TaperLengthTimeUnit.WEEKS,
    val isDone: Boolean = false,
)

enum class TaperLengthTimeUnit {
    DAYS,
    WEEKS,
    MONTHS
}

enum class HabitType {
    INCREASE,
    DECREASE
}