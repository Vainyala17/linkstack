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

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val githubTokenKey = stringPreferencesKey("github_token")
    private val hackerNewsTokenKey = stringPreferencesKey("hackernews_token")

    val githubToken: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[githubTokenKey]
    }

    val hackerNewsToken: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[hackerNewsTokenKey]
    }

    suspend fun updateGitHubToken(token: String?) {
        context.authDataStore.edit { preferences ->
            if (token == null) {
                preferences.remove(githubTokenKey)
            } else {
                preferences[githubTokenKey] = token
            }
        }
    }

    suspend fun updateHackerNewsToken(token: String?) {
        context.authDataStore.edit { preferences ->
            if (token == null) {
                preferences.remove(hackerNewsTokenKey)
            } else {
                preferences[hackerNewsTokenKey] = token
            }
        }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(githubTokenKey)
            preferences.remove(hackerNewsTokenKey)
        }
    }
}
