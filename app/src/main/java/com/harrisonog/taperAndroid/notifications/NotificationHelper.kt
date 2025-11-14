package com.harrisonog.taperAndroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
     * @param habitId ID of the habit
     * @param eventId Unique ID for this specific event
     */
    fun showNotification(
        context: Context,
        habitName: String,
        message: String,
        habitId: Long,
        eventId: Long,
    ) {
        val notificationId = (NOTIFICATION_ID_BASE + (eventId % 10000)).toInt()

        // Create action intents
        val completedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_ACTION, NotificationActionReceiver.ACTION_COMPLETED)
        }
        val completedPendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 3 + 0),
            completedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_ACTION, NotificationActionReceiver.ACTION_SNOOZE)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 3 + 1),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val denyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_ACTION, NotificationActionReceiver.ACTION_DENY)
        }
        val denyPendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 3 + 2),
            denyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create delete intent - triggers deny action when notification is dismissed/swiped away
        val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_ACTION, NotificationActionReceiver.ACTION_DENY)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            (notificationId * 3 + 3),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with custom icon
                .setContentTitle(habitName)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setDeleteIntent(deletePendingIntent) // Trigger deny action when notification is dismissed
                .addAction(
                    android.R.drawable.ic_input_add,
                    "Completed",
                    completedPendingIntent
                )
                .addAction(
                    android.R.drawable.ic_popup_reminder,
                    "Snooze",
                    snoozePendingIntent
                )
                .addAction(
                    android.R.drawable.ic_delete,
                    "Deny",
                    denyPendingIntent
                )
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
