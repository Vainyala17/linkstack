package com.hp77.linkstash.presentation.settings

data class SettingsScreenState(
    val isGitHubAuthenticated: Boolean = false,
    val isGitHubSyncing: Boolean = false,
    val githubRepoName: String = "",
    val githubRepoOwner: String = "",
    val githubProfile: GitHubProfile? = null,
    val isHackerNewsAuthenticated: Boolean = false,
    val hackerNewsUsername: String = "",
    val hackerNewsPassword: String = "",
    val hackerNewsProfile: HackerNewsProfile? = null,
    val showHackerNewsLoginDialog: Boolean = false,
    val showGitHubDeviceFlowDialog: Boolean = false,
    val githubDeviceCode: String = "",
    val githubUserCode: String = "",
    val githubVerificationUri: String = "",
    val isPollingForGitHubToken: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncProgress: SyncProgress = SyncProgress.None
)

data class GitHubProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String,
    val bio: String?,
    val location: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String
)

data class HackerNewsProfile(
    val username: String,
    val karma: Int,
    val about: String?,
    val createdAt: Long
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
