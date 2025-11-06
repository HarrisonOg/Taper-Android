package com.harrisonog.taperAndroid.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Helper class for managing notification and alarm permissions.
 */
object PermissionHelper {
    /**
     * Check if POST_NOTIFICATIONS permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Not required on older versions
            true
        }
    }

    /**
     * Check if SCHEDULE_EXACT_ALARM permission is granted (Android 12+).
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // Not required on older versions
            true
        }
    }

    /**
     * Check if all required permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasNotificationPermission(context) && hasExactAlarmPermission(context)
    }

    /**
     * Request notification permission (Android 13+).
     * Returns an ActivityResultLauncher that should be registered in onCreate.
     */
    fun createNotificationPermissionLauncher(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }

    /**
     * Request notification permission.
     */
    fun requestNotificationPermission(
        launcher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Open system settings for exact alarm permission (Android 12+).
     * This permission requires the user to grant it from system settings.
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
