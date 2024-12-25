package com.hp77.linkstash.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveRecentSearch(query: String) {
        val searches = getRecentSearches().toMutableList()
        // Remove if already exists to avoid duplicates
        searches.remove(query)
        // Add to beginning of list
        searches.add(0, query)
        // Keep only last MAX_RECENT_SEARCHES
        val trimmedSearches = searches.take(MAX_RECENT_SEARCHES)
        
        prefs.edit()
            .putStringSet(KEY_RECENT_SEARCHES, trimmedSearches.toSet())
            .apply()
    }

    fun getRecentSearches(): List<String> {
        return prefs.getStringSet(KEY_RECENT_SEARCHES, emptySet())
            ?.toList()
            ?.sortedByDescending { it } // Most recent first
            ?: emptyList()
    }

    companion object {
        private const val PREFS_NAME = "search_preferences"
        private const val KEY_RECENT_SEARCHES = "recent_searches"
        private const val MAX_RECENT_SEARCHES = 10
    }
}
