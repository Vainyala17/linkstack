package com.hp77.linkstash.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.data.repository.HackerNewsRepository
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository,
    private val hackerNewsRepository: HackerNewsRepository,
    private val authPreferences: AuthPreferences,
    private val syncLinksToGitHubUseCase: SyncLinksToGitHubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            Logger.d(TAG, "Initializing ViewModel")
            // Initialize GitHub state
            val isAuthenticated = gitHubSyncRepository.isAuthenticated()
            Logger.d(TAG, "GitHub authenticated: $isAuthenticated")
            _state.update { it.copy(
                isGitHubAuthenticated = isAuthenticated,
                githubRepoName = authPreferences.githubRepoName.first() ?: "",
                githubRepoOwner = authPreferences.githubRepoOwner.first() ?: "",
                isHackerNewsAuthenticated = hackerNewsRepository.isAuthenticated()
            ) }
        }
    }

    fun onEvent(event: SettingsScreenEvent) {
        when (event) {
            is SettingsScreenEvent.UpdateGitHubRepoName -> {
                Logger.d(TAG, "Updating GitHub repo name: ${event.name}")
                _state.update { it.copy(githubRepoName = event.name) }
                viewModelScope.launch {
                    authPreferences.updateGitHubRepoName(event.name)
                }
            }
            is SettingsScreenEvent.UpdateGitHubRepoOwner -> {
                Logger.d(TAG, "Updating GitHub repo owner: ${event.owner}")
                _state.update { it.copy(githubRepoOwner = event.owner) }
                viewModelScope.launch {
                    authPreferences.updateGitHubRepoOwner(event.owner)
                }
            }
            is SettingsScreenEvent.ShowGitHubDeviceFlow -> {
                Logger.d(TAG, "Showing GitHub device flow dialog")
                _state.update { it.copy(showGitHubDeviceFlowDialog = true) }
            }
            is SettingsScreenEvent.HideGitHubDeviceFlow -> {
                Logger.d(TAG, "Hiding GitHub device flow dialog")
                _state.update { it.copy(
                    showGitHubDeviceFlowDialog = false,
                    githubDeviceCode = "",
                    githubUserCode = "",
                    githubVerificationUri = "",
                    isPollingForGitHubToken = false
                ) }
                pollingJob?.cancel()
                pollingJob = null
            }
            is SettingsScreenEvent.InitiateGitHubDeviceFlow -> {
                Logger.d(TAG, "Initiating GitHub device flow")
                viewModelScope.launch {
                    try {
                        val deviceCodeResponse = gitHubSyncRepository.initiateDeviceFlow().getOrThrow()
                        Logger.d(TAG, "Got device code, verification URI: ${deviceCodeResponse.verificationUri}")
                        _state.update { it.copy(
                            githubDeviceCode = deviceCodeResponse.deviceCode,
                            githubUserCode = deviceCodeResponse.userCode,
                            githubVerificationUri = deviceCodeResponse.verificationUri,
                            isPollingForGitHubToken = true,
                            error = null
                        ) }

                        // Start polling for token
                        pollingJob?.cancel()
                        pollingJob = viewModelScope.launch {
                            Logger.d(TAG, "Starting to poll for token")
                            gitHubSyncRepository.pollForToken(
                                deviceCodeResponse.deviceCode,
                                deviceCodeResponse.interval
                            ).onSuccess { token ->
                                Logger.d(TAG, "Successfully got token")
                                _state.update { it.copy(
                                    isGitHubAuthenticated = true,
                                    showGitHubDeviceFlowDialog = false,
                                    isPollingForGitHubToken = false,
                                    error = null
                                ) }
                            }.onFailure { error ->
                                Logger.e(TAG, "Failed to get token", error)
                                _state.update { it.copy(
                                    isPollingForGitHubToken = false,
                                    error = error.message
                                ) }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Device flow initiation failed", e)
                        _state.update { it.copy(
                            isPollingForGitHubToken = false,
                            error = e.message
                        ) }
                    }
                }
            }
            is SettingsScreenEvent.CancelGitHubDeviceFlow -> {
                Logger.d(TAG, "Cancelling GitHub device flow")
                pollingJob?.cancel()
                pollingJob = null
                _state.update { it.copy(
                    showGitHubDeviceFlowDialog = false,
                    githubDeviceCode = "",
                    githubUserCode = "",
                    githubVerificationUri = "",
                    isPollingForGitHubToken = false
                ) }
            }
            is SettingsScreenEvent.DisconnectGitHub -> {
                Logger.d(TAG, "Disconnecting from GitHub")
                viewModelScope.launch {
                    gitHubSyncRepository.logout()
                    authPreferences.updateGitHubRepoName(null)
                    authPreferences.updateGitHubRepoOwner(null)
                    _state.update { it.copy(
                        isGitHubAuthenticated = false,
                        githubRepoName = "",
                        githubRepoOwner = "",
                        syncProgress = SyncProgress.None
                    ) }
                }
            }
            is SettingsScreenEvent.SyncToGitHub -> {
                Logger.d(TAG, "Starting GitHub sync")
                viewModelScope.launch {
                    _state.update { it.copy(
                        isGitHubSyncing = true,
                        syncProgress = SyncProgress.InProgress("Preparing to sync...")
                    ) }
                    try {
                        Logger.d(TAG, "Calling sync use case")
                        syncLinksToGitHubUseCase().onSuccess { result ->
                            Logger.d(TAG, "Sync completed successfully")
                            _state.update { it.copy(
                                isGitHubSyncing = false,
                                error = null,
                                syncProgress = SyncProgress.Success(
                                    totalLinks = result.totalLinks,
                                    newLinks = result.newLinks
                                )
                            ) }
                        }.onFailure { error ->
                            Logger.e(TAG, "Sync failed", error)
                            _state.update { it.copy(
                                isGitHubSyncing = false,
                                error = error.message,
                                syncProgress = SyncProgress.Error(error.message ?: "Unknown error")
                            ) }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Sync failed", e)
                        _state.update { it.copy(
                            isGitHubSyncing = false,
                            error = e.message,
                            syncProgress = SyncProgress.Error(e.message ?: "Unknown error")
                        ) }
                    }
                }
            }
            is SettingsScreenEvent.ShowHackerNewsLogin -> {
                _state.update { it.copy(showHackerNewsLoginDialog = true) }
            }
            is SettingsScreenEvent.HideHackerNewsLogin -> {
                _state.update { it.copy(
                    showHackerNewsLoginDialog = false,
                    hackerNewsUsername = "",
                    hackerNewsPassword = ""
                ) }
            }
            is SettingsScreenEvent.UpdateHackerNewsUsername -> {
                _state.update { it.copy(hackerNewsUsername = event.username) }
            }
            is SettingsScreenEvent.UpdateHackerNewsPassword -> {
                _state.update { it.copy(hackerNewsPassword = event.password) }
            }
            is SettingsScreenEvent.LoginToHackerNews -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    try {
                        hackerNewsRepository.login(
                            _state.value.hackerNewsUsername,
                            _state.value.hackerNewsPassword
                        ).getOrThrow()
                        
                        _state.update { it.copy(
                            isLoading = false,
                            isHackerNewsAuthenticated = true,
                            showHackerNewsLoginDialog = false,
                            hackerNewsUsername = "",
                            hackerNewsPassword = "",
                            error = null
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            isLoading = false,
                            error = e.message
                        ) }
                    }
                }
            }
            is SettingsScreenEvent.LogoutFromHackerNews -> {
                viewModelScope.launch {
                    hackerNewsRepository.logout()
                    _state.update { it.copy(isHackerNewsAuthenticated = false) }
                }
            }
            is SettingsScreenEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ViewModel cleared")
        pollingJob?.cancel()
    }
}
