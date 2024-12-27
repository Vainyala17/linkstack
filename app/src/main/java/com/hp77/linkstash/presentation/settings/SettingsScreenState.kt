package com.hp77.linkstash.presentation.settings

data class SettingsScreenState(
    val isGitHubAuthenticated: Boolean = false,
    val isGitHubSyncing: Boolean = false,
    val githubRepoName: String = "",
    val githubRepoOwner: String = "",
    val isHackerNewsAuthenticated: Boolean = false,
    val hackerNewsUsername: String = "",
    val hackerNewsPassword: String = "",
    val showHackerNewsLoginDialog: Boolean = false,
    val showGitHubDeviceFlowDialog: Boolean = false,
    val githubDeviceCode: String = "",
    val githubUserCode: String = "",
    val githubVerificationUri: String = "",
    val isPollingForGitHubToken: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // New sync status fields
    val syncProgress: SyncProgress = SyncProgress.None
)

sealed class SyncProgress {
    object None : SyncProgress()
    data class InProgress(val message: String) : SyncProgress()
    data class Success(
        val totalLinks: Int,
        val newLinks: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncProgress()
    data class Error(val message: String) : SyncProgress()
}
