package com.harrisonog.taperAndroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = ForestGreen80,
        secondary = MossGreen80,
        tertiary = SageGreen80,
        primaryContainer = ForestGreenDark,
        secondaryContainer = MossGreen,
        background = Color(0xFF1A1C1A),
        surface = Color(0xFF1A1C1A),
        onPrimary = Color(0xFF003911),
        onSecondary = Color(0xFF2E3F2E),
        onTertiary = Color(0xFF2B3F2B),
        onBackground = Color(0xFFE1E3E0),
        onSurface = Color(0xFFE1E3E0),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = ForestGreen,
        secondary = MossGreen,
        tertiary = SageGreen,
        primaryContainer = Color(0xFFB8E6B8),
        secondaryContainer = LightMossGreen,
        tertiaryContainer = Color(0xFFDCEDC8),
        background = Color(0xFFFBFDF7),
        surface = Color(0xFFFBFDF7),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onPrimaryContainer = ForestGreenDark,
        onSecondaryContainer = Color(0xFF1B3318),
        onTertiaryContainer = Color(0xFF1F3E1B),
        onBackground = Color(0xFF1A1C1A),
        onSurface = Color(0xFF1A1C1A),
    )

@Composable
fun TaperAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Disabled by default to maintain green theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TaperTypography,
        content = content,
    )
}
