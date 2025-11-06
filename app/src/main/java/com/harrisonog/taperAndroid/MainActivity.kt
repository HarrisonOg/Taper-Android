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
                checkExactAlarmPermission()
            } else {
                // User denied notification permission - reschedule anyway with fallback
                rescheduleHabits()
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
            // Reschedule if permissions were granted from settings
            if (PermissionHelper.hasAllPermissions(this)) {
                rescheduleHabits()
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
        } else {
            // All permissions granted, reschedule habits
            rescheduleHabits()
        }
    }

    private fun rescheduleHabits() {
        val repository = (application as TaperApp).repository
        lifecycleScope.launch {
            repository.rescheduleAllActiveHabits()
        }
    }
}
