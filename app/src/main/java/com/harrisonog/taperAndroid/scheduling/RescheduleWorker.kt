package com.harrisonog.taperAndroid.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harrisonog.taperAndroid.TaperApp

/**
 * Periodic worker that checks if rescheduling is needed (>7 days since last reschedule)
 * and reschedules all active habits if necessary. This ensures the next 7 days
 * of events are always scheduled while avoiding unnecessary work.
 */
class RescheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as TaperApp
            // Only reschedules if it's been >7 days since last reschedule
            app.repository.rescheduleIfNeeded()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
