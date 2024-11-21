package com.harrisonog.taper.data

import java.time.LocalDateTime
import java.util.Date

/**
 * Class for storing Habit data.
 */
class Habit(
    val name: String,
    val description: String,
    val notificationMessage: String,
    val habitType: HabitType = HabitType.DECREASE,
    val startTaperAlarmsPerDay: Int,
    val endTaperAlarmsPerDay: Int = 1,
    val taperLength: TaperLength
) {

    //list of alarms


}

data class TaperLength(val number: Int, val taperLengthTimeScale: TaperLengthTimeScale)

enum class HabitType {
    INCREASE,
    DECREASE
}

enum class TaperLengthTimeScale{
    DAYS,
    WEEKS,
    MONTHS
}