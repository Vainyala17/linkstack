package com.hp77.linkstash.presentation.settings

sealed class SettingsScreenEvent {
    // GitHub Events
    data class UpdateGitHubToken(val token: String) : SettingsScreenEvent()
    data class UpdateGitHubRepoName(val name: String) : SettingsScreenEvent()
    data class UpdateGitHubRepoOwner(val owner: String) : SettingsScreenEvent()
    object ConnectGitHub : SettingsScreenEvent()
    object DisconnectGitHub : SettingsScreenEvent()
    object SyncToGitHub : SettingsScreenEvent()
    
    // HackerNews Events
    object ShowHackerNewsLogin : SettingsScreenEvent()
    object HideHackerNewsLogin : SettingsScreenEvent()
    data class UpdateHackerNewsUsername(val username: String) : SettingsScreenEvent()
    data class UpdateHackerNewsPassword(val password: String) : SettingsScreenEvent()
    object LoginToHackerNews : SettingsScreenEvent()
    object LogoutFromHackerNews : SettingsScreenEvent()
    
    // Common Events
    object DismissError : SettingsScreenEvent()
}
