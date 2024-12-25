package com.hp77.linkstash.presentation.settings

data class SettingsScreenState(
    val isGitHubAuthenticated: Boolean = false,
    val isGitHubSyncing: Boolean = false,
    val githubToken: String = "",
    val githubRepoName: String = "",
    val githubRepoOwner: String = "",
    val isHackerNewsAuthenticated: Boolean = false,
    val hackerNewsUsername: String = "",
    val hackerNewsPassword: String = "",
    val showHackerNewsLoginDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
