package com.hp77.linkstash.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.data.repository.HackerNewsRepository
import com.hp77.linkstash.domain.usecase.link.CleanupInvalidLinksUseCase
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository,
    private val hackerNewsRepository: HackerNewsRepository,
    private val authPreferences: AuthPreferences,
    private val syncLinksToGitHubUseCase: SyncLinksToGitHubUseCase,
    private val cleanupInvalidLinksUseCase: CleanupInvalidLinksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            Logger.d(TAG, "Initializing ViewModel")
            // Initialize GitHub state
            val isAuthenticated = gitHubSyncRepository.isAuthenticated()
            val repoName = authPreferences.githubRepoName.first() ?: ""
            val repoOwner = authPreferences.githubRepoOwner.first() ?: ""
            Logger.d(TAG, "GitHub authenticated: $isAuthenticated")
            _state.update { it.copy(
                isGitHubAuthenticated = isAuthenticated,
                githubRepoName = repoName,
                githubRepoOwner = repoOwner,
                tempGithubRepoName = repoName,
                tempGithubRepoOwner = repoOwner,
                isHackerNewsAuthenticated = hackerNewsRepository.isAuthenticated()
            ) }
        }
    }

    fun onEvent(event: SettingsScreenEvent) {
        when (event) {
            is SettingsScreenEvent.StartEditingGitHubRepo -> {
                _state.update { it.copy(
                    isEditingGitHubRepo = true,
                    tempGithubRepoName = it.githubRepoName,
                    tempGithubRepoOwner = it.githubRepoOwner
                ) }
            }
            is SettingsScreenEvent.CancelEditingGitHubRepo -> {
                _state.update { it.copy(
                    isEditingGitHubRepo = false,
                    tempGithubRepoName = it.githubRepoName,
                    tempGithubRepoOwner = it.githubRepoOwner
                ) }
            }
            is SettingsScreenEvent.SaveGitHubRepo -> {
                viewModelScope.launch {
                    val newName = _state.value.tempGithubRepoName
                    val newOwner = _state.value.tempGithubRepoOwner
                    authPreferences.updateGitHubRepoName(newName)
                    authPreferences.updateGitHubRepoOwner(newOwner)
                    _state.update { it.copy(
                        isEditingGitHubRepo = false,
                        githubRepoName = newName,
                        githubRepoOwner = newOwner
                    ) }
                }
            }
            is SettingsScreenEvent.UpdateTempGitHubRepoName -> {
                _state.update { it.copy(tempGithubRepoName = event.name) }
            }
            is SettingsScreenEvent.UpdateTempGitHubRepoOwner -> {
                _state.update { it.copy(tempGithubRepoOwner = event.owner) }
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
                    _state.update { it.copy(
                        isInitiatingDeviceFlow = true,
                        deviceFlowStatus = "Connecting to GitHub...",
                        error = null
                    ) }
                    delay(500) // Give UI time to update
                    _state.update { it.copy(
                        deviceFlowStatus = "Requesting device code..."
                    ) }
                    try {
                        val deviceCodeResponse = gitHubSyncRepository.initiateDeviceFlow().getOrThrow()
                        Logger.d(TAG, "Got device code, verification link: ${deviceCodeResponse.verificationLink}")
                        _state.update { it.copy(
                            githubDeviceCode = deviceCodeResponse.deviceCode,
                            githubUserCode = deviceCodeResponse.userCode,
                            githubVerificationUri = deviceCodeResponse.verificationLink,
                            isPollingForGitHubToken = true,
                            isInitiatingDeviceFlow = false,
                            deviceFlowStatus = "",
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
                                when {
                                    error.message?.contains("authorization_pending") == true -> {
                                        Logger.d(TAG, "Waiting for user authorization")
                                        _state.update { it.copy(
                                            deviceFlowStatus = "Waiting for authorization... Please enter the code on GitHub"
                                        ) }
                                    }
                                    error.message?.contains("slow_down") == true -> {
                                        Logger.d(TAG, "Polling too fast")
                                        _state.update { it.copy(
                                            deviceFlowStatus = "Please wait a moment..."
                                        ) }
                                    }
                                    error.message?.contains("expired") == true -> {
                                        Logger.e(TAG, "Device code expired", error)
                                        _state.update { it.copy(
                                            isPollingForGitHubToken = false,
                                            error = "Code expired. Please try again.",
                                            deviceFlowStatus = ""
                                        ) }
                                    }
                                    error is CancellationException -> {
                                        Logger.d(TAG, "GitHub authorization was cancelled by user")
                                        _state.update { it.copy(
                                            isPollingForGitHubToken = false,
                                            deviceFlowStatus = "",
                                            showGitHubDeviceFlowDialog = false
                                        ) }
                                    }
                                    else -> {
                                        Logger.e(TAG, "Failed to get token", error)
                                        _state.update { it.copy(
                                            isPollingForGitHubToken = false,
                                            error = "Unable to connect to GitHub. Please try again.",
                                            deviceFlowStatus = ""
                                        ) }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Device flow initiation failed", e)
                        val errorMessage = when {
                            e is java.net.SocketTimeoutException -> "Connection timed out. Please check your internet connection and try again."
                            e.message?.contains("timeout", ignoreCase = true) == true -> "Request timed out. Please try again."
                            else -> e.message ?: "Failed to connect to GitHub. Please try again."
                        }
                        _state.update { it.copy(
                            isPollingForGitHubToken = false,
                            isInitiatingDeviceFlow = false,
                            error = errorMessage
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
                        val currentState = _state.value
                        when {
                            currentState.hackerNewsUsername.isBlank() -> throw IllegalArgumentException("Username cannot be empty")
                            currentState.hackerNewsPassword.isBlank() -> throw IllegalArgumentException("Password cannot be empty")
                            else -> hackerNewsRepository.login(currentState.hackerNewsUsername, currentState.hackerNewsPassword).getOrThrow()
                        }
                        
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
            is SettingsScreenEvent.CleanupInvalidLinks -> {
                viewModelScope.launch {
                    Logger.d(TAG, "Starting cleanup of invalid links")
                    _state.update { it.copy(
                        isCleaningUp = true,
                        cleanupResult = null,
                        error = null
                    ) }
                    try {
                        val result = cleanupInvalidLinksUseCase().getOrNull()
                        Logger.d(TAG, "Cleanup completed. Removed $result invalid links")
                        _state.update { it.copy(
                            isCleaningUp = false,
                            cleanupResult = result,
                            error = null
                        ) }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Cleanup failed", e)
                        _state.update { it.copy(
                            isCleaningUp = false,
                            error = "Failed to cleanup invalid links: ${e.message}"
                        ) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ViewModel cleared")
        pollingJob?.cancel()
    }
}
