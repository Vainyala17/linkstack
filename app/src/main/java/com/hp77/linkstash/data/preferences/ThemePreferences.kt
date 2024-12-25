package com.hp77.linkstash.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeModeString = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(themeModeString)
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.name
        }
    }

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
