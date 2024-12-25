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
    private val githubRepoNameKey = stringPreferencesKey("github_repo_name")
    private val githubRepoOwnerKey = stringPreferencesKey("github_repo_owner")
    private val hackerNewsTokenKey = stringPreferencesKey("hackernews_token")

    val githubToken: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[githubTokenKey]
    }

    val githubRepoName: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[githubRepoNameKey]
    }

    val githubRepoOwner: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[githubRepoOwnerKey]
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

    suspend fun updateGitHubRepoName(name: String?) {
        context.authDataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(githubRepoNameKey)
            } else {
                preferences[githubRepoNameKey] = name
            }
        }
    }

    suspend fun updateGitHubRepoOwner(owner: String?) {
        context.authDataStore.edit { preferences ->
            if (owner == null) {
                preferences.remove(githubRepoOwnerKey)
            } else {
                preferences[githubRepoOwnerKey] = owner
            }
        }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(githubTokenKey)
            preferences.remove(githubRepoNameKey)
            preferences.remove(githubRepoOwnerKey)
            preferences.remove(hackerNewsTokenKey)
        }
    }
}
