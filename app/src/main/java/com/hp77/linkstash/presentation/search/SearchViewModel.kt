package com.hp77.linkstash.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.data.preferences.SearchPreferences
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.domain.repository.TagRepository
import com.hp77.linkstash.domain.usecase.link.ShareToHackerNewsUseCase
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val tagRepository: TagRepository,
    private val searchPreferences: SearchPreferences,
    private val shareToHackerNewsUseCase: ShareToHackerNewsUseCase,
    private val syncLinksToGitHubUseCase: SyncLinksToGitHubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    init {
        loadTags()
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        _state.update { it.copy(recentSearches = searchPreferences.getRecentSearches()) }
    }

    private fun loadTags() {
        viewModelScope.launch {
            try {
                tagRepository.getAllTags().collect { tags ->
                    _state.update { it.copy(tags = tags) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun toggleFavorite(link: Link) {
        viewModelScope.launch {
            try {
                linkRepository.toggleFavorite(link)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun toggleArchive(link: Link) {
        viewModelScope.launch {
            try {
                linkRepository.toggleArchive(link)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                linkRepository.searchLinks(
                    query = query,
                    tags = state.value.selectedTags.map { it.id }
                ).collect { links ->
                    _state.update { it.copy(
                        searchResults = links,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message,
                    isLoading = false,
                    searchResults = emptyList()
                ) }
            }
        }
    }

    fun onEvent(event: SearchScreenEvent) {
        when (event) {
            is SearchScreenEvent.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = event.query) }
                if (event.query.isNotEmpty()) {
                    searchPreferences.saveRecentSearch(event.query)
                    loadRecentSearches()
                    performSearch(event.query)
                } else {
                    _state.update { it.copy(searchResults = emptyList()) }
                }
            }
            is SearchScreenEvent.OnTagSelect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags + event.tag
                )}
                if (state.value.searchQuery.isNotEmpty()) {
                    performSearch(state.value.searchQuery)
                }
            }
            is SearchScreenEvent.OnTagDeselect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags - event.tag
                )}
                if (state.value.searchQuery.isNotEmpty()) {
                    performSearch(state.value.searchQuery)
                }
            }
            is SearchScreenEvent.OnDeleteLink -> {
                viewModelScope.launch {
                    try {
                        Logger.d("SearchViewModel", "Deleting link: id=${event.link.id}, url=${event.link.url}")
                        linkRepository.deleteLink(event.link)
                        Logger.d("SearchViewModel", "Successfully deleted link")
                        // Re-run search to update results
                        if (state.value.searchQuery.isNotEmpty()) {
                            performSearch(state.value.searchQuery)
                        }
                    } catch (e: Exception) {
                        Logger.e("SearchViewModel", "Error deleting link: ${e.message}", e)
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
            is SearchScreenEvent.OnRecentSearchClick -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchPreferences.saveRecentSearch(event.query)
                loadRecentSearches()
                performSearch(event.query)
            }
            SearchScreenEvent.OnErrorDismiss -> {
                _state.update { it.copy(error = null) }
            }
            is SearchScreenEvent.OnShowShareSheet -> {
                _state.update { it.copy(
                    showShareSheet = true,
                    selectedLink = event.link
                ) }
            }
            SearchScreenEvent.OnDismissShareSheet -> {
                _state.update { it.copy(
                    showShareSheet = false,
                    selectedLink = null
                ) }
            }
            is SearchScreenEvent.OnShareToHackerNews -> {
                viewModelScope.launch {
                    try {
                        val result = shareToHackerNewsUseCase(event.link.id.toLong())
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        _state.update { it.copy(
                            showShareSheet = false,
                            selectedLink = null
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
            is SearchScreenEvent.OnSyncToGitHub -> {
                viewModelScope.launch {
                    try {
                        val result = syncLinksToGitHubUseCase()
                        if (result.isFailure) {
                            throw result.exceptionOrNull() ?: Exception("Unknown error")
                        }
                        _state.update { it.copy(
                            showShareSheet = false,
                            selectedLink = null
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
        }
    }
}
