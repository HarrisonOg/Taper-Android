package com.harrisonog.taperAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.harrisonog.taperAndroid.notifications.NotificationHelper
import com.harrisonog.taperAndroid.ui.navigation.TaperNavHost
import com.harrisonog.taperAndroid.ui.theme.TaperAndroidTheme

/**
 * Hosts the Compose navigation graph for the application.
 *
 * The activity obtains the [TaperApp] level repository and provides it to [TaperNavHost],
 * wiring the data layer into the composable screens that make up the app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

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
}
