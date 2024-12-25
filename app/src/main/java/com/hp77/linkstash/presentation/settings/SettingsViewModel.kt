package com.hp77.linkstash.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.data.repository.HackerNewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository,
    private val hackerNewsRepository: HackerNewsRepository,
    private val authPreferences: AuthPreferences,
    private val syncLinksToGitHubUseCase: SyncLinksToGitHubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state

    init {
        viewModelScope.launch {
            _state.update { it.copy(
                isGitHubAuthenticated = gitHubSyncRepository.isAuthenticated(),
                isHackerNewsAuthenticated = hackerNewsRepository.isAuthenticated()
            ) }
        }
    }

    fun onEvent(event: SettingsScreenEvent) {
        when (event) {
            is SettingsScreenEvent.UpdateGitHubToken -> {
                _state.update { it.copy(githubToken = event.token) }
            }
            is SettingsScreenEvent.ConnectGitHub -> {
                viewModelScope.launch {
                    try {
                        if (state.value.githubToken.isBlank()) {
                            _state.update { it.copy(error = "GitHub token cannot be empty") }
                            return@launch
                        }
                        gitHubSyncRepository.authenticate(state.value.githubToken)
                        _state.update { it.copy(
                            isGitHubAuthenticated = true,
                            error = null
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
            is SettingsScreenEvent.DisconnectGitHub -> {
                viewModelScope.launch {
                    gitHubSyncRepository.logout()
                    _state.update { it.copy(isGitHubAuthenticated = false) }
                }
            }
            is SettingsScreenEvent.SyncToGitHub -> {
                viewModelScope.launch {
                    _state.update { it.copy(isGitHubSyncing = true) }
                    try {
                        syncLinksToGitHubUseCase().getOrThrow()
                        _state.update { it.copy(
                            isGitHubSyncing = false,
                            error = null
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            isGitHubSyncing = false,
                            error = e.message
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
}
