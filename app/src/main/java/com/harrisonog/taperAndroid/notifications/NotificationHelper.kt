package com.harrisonog.taperAndroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "taper_habit_reminders"
    const val NOTIFICATION_ID_BASE = 1000

    /**
     * Creates the notification channel for habit reminders.
     * Must be called before showing any notifications.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Notifications for your habit taper schedules"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a notification for a habit event.
     *
     * @param context Application context
     * @param habitName Name of the habit
     * @param message Custom message for the habit
     * @param eventId Unique ID for this specific event
     */
    fun showNotification(
        context: Context,
        habitName: String,
        message: String,
        eventId: Long,
    ) {
        val notificationId = (NOTIFICATION_ID_BASE + (eventId % 10000)).toInt()

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with custom icon
                .setContentTitle(habitName)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

        val notificationManager = NotificationManagerCompat.from(context)

        // Check if we have notification permission (required for Android 13+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            notificationManager.areNotificationsEnabled()
        ) {
            notificationManager.notify(notificationId, notification)
        }
    }

    /**
     * Checks if the app has permission to post notifications.
     * Always returns true for Android versions below 13.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }
}
