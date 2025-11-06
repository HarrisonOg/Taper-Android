package com.harrisonog.taper_android.ui.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

/**
 * Composable that checks for exact alarm and notification permissions
 * and displays a UI to request them if needed.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionChecker(
    onAllPermissionsGranted: () -> Unit = {}
) {
    val context = LocalContext.current

    var hasExactAlarmPermission by remember {
        mutableStateOf(canScheduleExactAlarms(context))
    }

    // Use Accompanist for notification permission
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    val hasNotificationPermission = notificationPermissionState?.status?.isGranted ?: true

    // Launcher for exact alarm settings
    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasExactAlarmPermission = canScheduleExactAlarms(context)
        if (hasExactAlarmPermission && hasNotificationPermission) {
            onAllPermissionsGranted()
        }
    }

    LaunchedEffect(hasExactAlarmPermission, hasNotificationPermission) {
        if (hasExactAlarmPermission && hasNotificationPermission) {
            onAllPermissionsGranted()
        }
    }

    if (!hasNotificationPermission || !hasExactAlarmPermission) {
        PermissionRequestCard(
            notificationPermissionState = notificationPermissionState,
            hasNotificationPermission = hasNotificationPermission,
            hasExactAlarmPermission = hasExactAlarmPermission,
            onRequestExactAlarms = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    exactAlarmSettingsLauncher.launch(intent)
                }
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRequestCard(
    notificationPermissionState: PermissionState?,
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onRequestExactAlarms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (!hasNotificationPermission && notificationPermissionState != null) {
                Text(
                    text = if (notificationPermissionState.status.shouldShowRationale) {
                        "Taper needs notification permission to remind you about your habits. " +
                        "Please grant this permission to receive timely reminders."
                    } else {
                        "Taper needs notification permission to remind you about your habits."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = { notificationPermissionState.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Notification Permission")
                }
            }

            if (!hasExactAlarmPermission) {
                Text(
                    text = "Taper needs exact alarm permission to deliver reminders at the right time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = onRequestExactAlarms,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Exact Alarm Permission")
                }
            }
        }
    }
}

fun canScheduleExactAlarms(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}
