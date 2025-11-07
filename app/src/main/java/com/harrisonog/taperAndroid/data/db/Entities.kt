package com.harrisonog.taperAndroid.data.db

import androidx.room.*
import java.time.*

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val message: String,
    val startPerDay: Int,
    val endPerDay: Int,
    val weeks: Int, // >= 1
    val startDate: LocalDate = LocalDate.now(),
    val isGoodHabit: Boolean, // true=ramp up, false=taper down
    val isActive: Boolean = true,
)

@Entity(
    tableName = "habit_events",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habitId"), Index("scheduledAt")],
)
data class HabitEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val scheduledAt: Instant, // exact time we plan to fire
    val sentAt: Instant? = null, // null until delivered (alarm scheduling later)
    val responseType: String? = null, // "completed", "denied", or null
    val respondedAt: Instant? = null, // when the user responded
    val isSnoozed: Boolean = false, // true if this event was created from a snooze
)
