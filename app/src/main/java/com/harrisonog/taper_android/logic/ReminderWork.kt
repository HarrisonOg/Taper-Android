package com.harrisonog.taper_android.logic

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.harrisonog.taper_android.data.db.HabitEvent
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Placeholder worker that will eventually deliver the reminder notification.
 * The worker currently succeeds immediately but captures the habit and event
 * metadata so downstream implementations can build on it.
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = Result.success()

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_SCHEDULED_AT = "scheduled_at"

        fun inputData(habitId: Long, event: HabitEvent): Data = workDataOf(
            KEY_HABIT_ID to habitId,
            KEY_SCHEDULED_AT to event.scheduledAt.toEpochMilli()
        )
    }
}

/** WorkRequestFactory implementation that tags work for cancellation. */
class ReminderWorkRequestFactory : WorkRequestFactory {
    override fun create(
        habitId: Long,
        event: HabitEvent,
        delay: Duration
    ): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(AlarmScheduler.habitTag(habitId))
            .setInputData(ReminderWorker.inputData(habitId, event))
            .build()
    }
}
