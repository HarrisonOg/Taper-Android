package com.harrisonog.taperAndroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.harrisonog.taperAndroid.notifications.NotificationHelper
import com.harrisonog.taperAndroid.ui.navigation.TaperNavHost
import com.harrisonog.taperAndroid.ui.theme.TaperAndroidTheme
import com.harrisonog.taperAndroid.util.PermissionHelper
import kotlinx.coroutines.launch

/**
 * Hosts the Compose navigation graph for the application.
 *
 * The activity obtains the [TaperApp] level repository and provides it to [TaperNavHost],
 * wiring the data layer into the composable screens that make up the app.
 */
class MainActivity : ComponentActivity() {
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private var hasRequestedPermissions = false
    private var permissionsJustGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Register notification permission launcher
        notificationPermissionLauncher = PermissionHelper.createNotificationPermissionLauncher(
            this
        ) { isGranted ->
            if (isGranted) {
                // After notification permission is granted, check exact alarm permission
                permissionsJustGranted = true
                checkExactAlarmPermission()
            }
        }

        enableEdgeToEdge()
        setContent {
            val repository = (application as TaperApp).repository

            TaperAndroidTheme {
                TaperNavHost(
                    repository = repository,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check and request permissions when activity becomes visible
        if (!hasRequestedPermissions) {
            hasRequestedPermissions = true
            checkAndRequestPermissions()
        } else {
            // If permissions were just granted, reschedule all habits
            if (permissionsJustGranted && PermissionHelper.hasAllPermissions(this)) {
                permissionsJustGranted = false
                rescheduleAllHabits()
            } else if (PermissionHelper.hasAllPermissions(this)) {
                // Permissions already granted - only reschedule if needed (>7 days)
                rescheduleIfNeeded()
            } else {
                // Permissions were revoked - re-prompt
                checkAndRequestPermissions()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // Check notification permission first
        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(notificationPermissionLauncher)
        } else {
            // Notification permission already granted, check exact alarm
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (!PermissionHelper.hasExactAlarmPermission(this)) {
            // Open settings to request exact alarm permission
            PermissionHelper.requestExactAlarmPermission(this)
        } else if (permissionsJustGranted) {
            // All permissions granted for the first time, reschedule all habits
            rescheduleAllHabits()
        }
    }

    private fun rescheduleAllHabits() {
        val repository = (application as TaperApp).repository
        lifecycleScope.launch {
            repository.rescheduleAllActiveHabits()
        }
    }

    private fun rescheduleIfNeeded() {
        val repository = (application as TaperApp).repository
        lifecycleScope.launch {
            repository.rescheduleIfNeeded()
        }
    }
}
