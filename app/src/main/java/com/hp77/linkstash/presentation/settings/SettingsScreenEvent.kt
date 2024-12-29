package com.hp77.linkstash.presentation.settings

sealed class SettingsScreenEvent {
    object CleanupInvalidLinks : SettingsScreenEvent()
    // GitHub Events
    object StartEditingGitHubRepo : SettingsScreenEvent()
    object CancelEditingGitHubRepo : SettingsScreenEvent()
    object SaveGitHubRepo : SettingsScreenEvent()
    data class UpdateTempGitHubRepoName(val name: String) : SettingsScreenEvent()
    data class UpdateTempGitHubRepoOwner(val owner: String) : SettingsScreenEvent()
    object ShowGitHubDeviceFlow : SettingsScreenEvent()
    object HideGitHubDeviceFlow : SettingsScreenEvent()
    object InitiateGitHubDeviceFlow : SettingsScreenEvent()
    object CancelGitHubDeviceFlow : SettingsScreenEvent()
    object DisconnectGitHub : SettingsScreenEvent()
    object SyncToGitHub : SettingsScreenEvent()
    
    // HackerNews Events
    object ShowHackerNewsLogin : SettingsScreenEvent()
    object HideHackerNewsLogin : SettingsScreenEvent()
    data class UpdateHackerNewsUsername(val username: String) : SettingsScreenEvent()
    data class UpdateHackerNewsPassword(val password: String) : SettingsScreenEvent()
    object LoginToHackerNews : SettingsScreenEvent()
    object LogoutFromHackerNews : SettingsScreenEvent()
    
    // Issue Reporting Events
    data class ReportIssue(val description: String) : SettingsScreenEvent()
    object ShowIssueReportDialog : SettingsScreenEvent()
    object HideIssueReportDialog : SettingsScreenEvent()
    data class UpdateIssueDescription(val description: String) : SettingsScreenEvent()

    // Common Events
    object DismissError : SettingsScreenEvent()
}
