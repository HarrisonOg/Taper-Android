package com.harrisonog.taperAndroid.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harrisonog.taperAndroid.TaperApp

/**
 * Periodic worker that reschedules all active habits to ensure the next 14 days
 * of events are always scheduled. This prevents running out of scheduled events
 * and avoids hitting Android's 500 concurrent alarm limit.
 */
class RescheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as TaperApp
            app.repository.rescheduleAllActiveHabits()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
