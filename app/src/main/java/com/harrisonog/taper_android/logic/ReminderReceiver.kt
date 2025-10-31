package com.harrisonog.taper_android.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.harrisonog.taper_android.data.db.HabitEvent

/**
 * BroadcastReceiver placeholder that will eventually surface reminder
 * notifications. For now it simply acts as a marker endpoint for exact alarms.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Actual notification delivery will be implemented separately.
    }

    companion object {
        const val ACTION_REMIND = "com.harrisonog.taper_android.action.REMIND"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_SCHEDULED_AT = "scheduled_at"

        fun createIntent(context: Context, habitId: Long, event: HabitEvent): Intent {
            return Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMIND
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_SCHEDULED_AT, event.scheduledAt.toEpochMilli())
            }
        }
    }
}
