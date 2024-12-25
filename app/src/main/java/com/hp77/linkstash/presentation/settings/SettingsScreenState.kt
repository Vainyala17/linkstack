package com.hp77.linkstash.presentation.settings

data class SettingsScreenState(
    val isGitHubAuthenticated: Boolean = false,
    val isHackerNewsAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGitHubSyncing: Boolean = false,
    val hackerNewsUsername: String = "",
    val hackerNewsPassword: String = "",
    val showHackerNewsLoginDialog: Boolean = false,
    val githubToken: String = ""
)
