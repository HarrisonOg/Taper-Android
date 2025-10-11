package com.harrisonog.taper_android.data.settings

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private const val DS_NAME = "taper_prefs"

// Top-level extension to access the DataStore from any Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DS_NAME)

// Keys
private val KEY_WAKE_START = stringPreferencesKey("wake_start") // "HH:MM"
private val KEY_WAKE_END   = stringPreferencesKey("wake_end")

// Typed settings model
data class AppSettings(
    val wakeStart: LocalTime = LocalTime.of(8, 0),
    val wakeEnd: LocalTime = LocalTime.of(22, 0)
)

// Map Preferences -> AppSettings
fun prefsToSettings(p: Preferences): AppSettings {
    val start = p[KEY_WAKE_START]?.let(LocalTime::parse) ?: LocalTime.of(8, 0)
    val end   = p[KEY_WAKE_END]?.let(LocalTime::parse)   ?: LocalTime.of(22, 0)
    return AppSettings(start, end)
}

// Observe settings as a Flow
fun Context.observeSettings(): Flow<AppSettings> =
    dataStore.data.map { prefsToSettings(it) }

// Save/update settings
suspend fun Context.saveSettings(settings: AppSettings) {
    dataStore.edit { prefs ->
        prefs[KEY_WAKE_START] = settings.wakeStart.toString() // e.g., "08:00"
        prefs[KEY_WAKE_END]   = settings.wakeEnd.toString()   // e.g., "22:00"
    }
}