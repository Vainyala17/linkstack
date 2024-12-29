package com.hp77.linkstash.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.GitHubProfile
import com.hp77.linkstash.domain.model.HackerNewsProfile
import com.hp77.linkstash.domain.usecase.profile.GetGitHubProfileUseCase
import com.hp77.linkstash.domain.usecase.profile.GetHackerNewsProfileUseCase
import com.hp77.linkstash.domain.usecase.link.GetLinksUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkStateUseCase
import com.hp77.linkstash.domain.usecase.link.ToggleLinkStatusUseCase
import com.hp77.linkstash.domain.usecase.link.ShareToHackerNewsUseCase
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.data.preferences.ThemeMode
import com.hp77.linkstash.data.preferences.ThemePreferences
import com.hp77.linkstash.util.CrashReporter
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLinksUseCase: GetLinksUseCase,
    private val updateLinkStateUseCase: UpdateLinkStateUseCase,
    private val toggleLinkStatusUseCase: ToggleLinkStatusUseCase,
    private val themePreferences: ThemePreferences,
    private val shareToHackerNewsUseCase: ShareToHackerNewsUseCase,
    private val syncLinksToGitHubUseCase: SyncLinksToGitHubUseCase,
    private val getGitHubProfileUseCase: GetGitHubProfileUseCase,
    private val getHackerNewsProfileUseCase: GetHackerNewsProfileUseCase,
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isDrawerOpen = MutableStateFlow(false)
    private val _showProfile = MutableStateFlow(false)
    private val _showShareSheet = MutableStateFlow(false)
    private val _selectedLink = MutableStateFlow<Link?>(null)
    private val _selectedFilter = MutableStateFlow<LinkFilter>(LinkFilter.All)
    private val _currentTheme = MutableStateFlow(ThemeMode.SYSTEM)
    private val _githubProfile = MutableStateFlow<GitHubProfile?>(null)
    private val _hackerNewsProfile = MutableStateFlow<HackerNewsProfile?>(null)
    private val _showIssueReportDialog = MutableStateFlow(false)
    private val _issueDescription = MutableStateFlow("")

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collect { theme ->
                _currentTheme.value = theme
            }
        }

        // Fetch profiles on init
        viewModelScope.launch {
            try {
                _githubProfile.value = getGitHubProfileUseCase().getOrNull()
                _hackerNewsProfile.value = getHackerNewsProfileUseCase().getOrNull()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _links = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        if (query.isNotEmpty()) {
            LinkFilter.Search(query)
        } else {
            filter
        }
    }.flatMapLatest { filter ->
        getLinksUseCase(filter)
    }

    private data class ViewModelState(
        val links: List<Link>,
        val query: String,
        val error: String?,
        val isLoading: Boolean,
        val isDrawerOpen: Boolean,
        val showProfile: Boolean,
        val showShareSheet: Boolean,
        val selectedLink: Link?,
        val selectedFilter: LinkFilter,
        val currentTheme: ThemeMode,
        val githubProfile: GitHubProfile?,
        val hackerNewsProfile: HackerNewsProfile?,
        val showIssueReportDialog: Boolean,
        val issueDescription: String
    ) {
        fun toHomeScreenState() = HomeScreenState(
            links = links,
            searchQuery = query,
            error = error,
            isLoading = isLoading,
            isDrawerOpen = isDrawerOpen,
            showProfile = showProfile,
            showShareSheet = showShareSheet,
            selectedLink = selectedLink,
            selectedFilter = selectedFilter,
            currentTheme = currentTheme,
            githubProfile = githubProfile,
            hackerNewsProfile = hackerNewsProfile,
            showIssueReportDialog = showIssueReportDialog,
            issueDescription = issueDescription
        )
    }

    val state: StateFlow<HomeScreenState> = combine(
        _links,
        _searchQuery,
        _error,
        _isLoading,
        _isDrawerOpen,
        _showProfile,
        _showShareSheet,
        _selectedLink,
        _selectedFilter,
        _currentTheme,
        _githubProfile,
        _hackerNewsProfile,
        _showIssueReportDialog,
        _issueDescription
    ) { args -> 
        ViewModelState(
            links = args[0] as List<Link>,
            query = args[1] as String,
            error = args[2] as String?,
            isLoading = args[3] as Boolean,
            isDrawerOpen = args[4] as Boolean,
            showProfile = args[5] as Boolean,
            showShareSheet = args[6] as Boolean,
            selectedLink = args[7] as Link?,
            selectedFilter = args[8] as LinkFilter,
            currentTheme = args[9] as ThemeMode,
            githubProfile = args[10] as GitHubProfile?,
            hackerNewsProfile = args[11] as HackerNewsProfile?,
            showIssueReportDialog = args[12] as Boolean,
            issueDescription = args[13] as String
        ).toHomeScreenState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState(isLoading = true)
    )

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            is HomeScreenEvent.OnFilterSelect -> {
                _selectedFilter.value = event.filter
            }
            is HomeScreenEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    try {
                        updateLinkStateUseCase(
                            com.hp77.linkstash.domain.usecase.link.LinkStateUpdate.ToggleFavorite(event.link)
                        )
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is HomeScreenEvent.OnToggleArchive -> {
                viewModelScope.launch {
                    try {
                        updateLinkStateUseCase(
                            com.hp77.linkstash.domain.usecase.link.LinkStateUpdate.ToggleArchive(event.link)
                        )
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is HomeScreenEvent.OnToggleStatus -> {
                viewModelScope.launch {
                    try {
                        toggleLinkStatusUseCase(event.link)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is HomeScreenEvent.OnDeleteLink -> {
                viewModelScope.launch {
                    try {
                        Logger.d("HomeViewModel", "Deleting link: id=${event.link.id}, url=${event.link.url}")
                        linkRepository.deleteLink(event.link)
                        Logger.d("HomeViewModel", "Successfully deleted link")
                    } catch (e: Exception) {
                        Logger.e("HomeViewModel", "Error deleting link: ${e.message}", e)
                        _error.value = e.message
                    }
                }
            }
            is HomeScreenEvent.OnThemeSelect -> {
                viewModelScope.launch {
                    try {
                        themePreferences.updateThemeMode(event.theme)
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            HomeScreenEvent.OnErrorDismiss -> {
                _error.value = null
            }
            HomeScreenEvent.OnDrawerOpen -> {
                _isDrawerOpen.value = true
            }
            HomeScreenEvent.OnDrawerClose -> {
                _isDrawerOpen.value = false
            }
            HomeScreenEvent.OnProfileClick -> {
                _showProfile.value = true
                // Use cached profile data, no need to fetch again
            }
            HomeScreenEvent.OnProfileDismiss -> {
                _showProfile.value = false
                // Keep the profile data cached in ViewModel state
                // Only clear on logout or when cache expires
            }
            is HomeScreenEvent.OnShowShareSheet -> {
                _selectedLink.value = event.link
                _showShareSheet.value = true
            }
            HomeScreenEvent.OnDismissShareSheet -> {
                _showShareSheet.value = false
                _selectedLink.value = null
            }
            is HomeScreenEvent.OnShareToHackerNews -> {
                viewModelScope.launch {
                    try {
                        val result = shareToHackerNewsUseCase(event.link.id.toLong())
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        _showShareSheet.value = false
                        _selectedLink.value = null
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            is HomeScreenEvent.OnSyncToGitHub -> {
                viewModelScope.launch {
                    try {
                        val result = syncLinksToGitHubUseCase()
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        _showShareSheet.value = false
                        _selectedLink.value = null
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
            HomeScreenEvent.NavigateToSettings -> {
                // Navigation is handled by the UI
            }
            HomeScreenEvent.ClearGitHubProfile -> {
                _githubProfile.value = null
            }
            HomeScreenEvent.ClearHackerNewsProfile -> {
                _hackerNewsProfile.value = null
            }
            HomeScreenEvent.ShowIssueReport -> {
                _showIssueReportDialog.value = true
            }
            HomeScreenEvent.HideIssueReport -> {
                _showIssueReportDialog.value = false
                _issueDescription.value = ""
            }
            is HomeScreenEvent.UpdateIssueDescription -> {
                _issueDescription.value = event.description
            }
            is HomeScreenEvent.ReportIssue -> {
                viewModelScope.launch {
                    try {
                        CrashReporter.reportIssue(event.description)
                        _showIssueReportDialog.value = false
                        _issueDescription.value = ""
                    } catch (e: Exception) {
                        _error.value = e.message
                    }
                }
            }
        }
    }
}
