package com.hp77.linkstash.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.usecase.link.GetLinksUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkStateUseCase
import com.hp77.linkstash.domain.usecase.link.ToggleLinkStatusUseCase
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
    private val toggleLinkStatusUseCase: ToggleLinkStatusUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _showMenu = MutableStateFlow(false)
    private val _showProfile = MutableStateFlow(false)
    private val _selectedFilter = MutableStateFlow<LinkFilter>(LinkFilter.All)

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
        val showMenu: Boolean,
        val showProfile: Boolean,
        val selectedFilter: LinkFilter
    ) {
        fun toHomeScreenState() = HomeScreenState(
            links = links,
            searchQuery = query,
            error = error,
            isLoading = isLoading,
            showMenu = showMenu,
            showProfile = showProfile,
            selectedFilter = selectedFilter
        )
    }

    val state: StateFlow<HomeScreenState> = combine(
        _links,
        _searchQuery,
        _error,
        _isLoading,
        _showMenu,
        _showProfile,
        _selectedFilter
    ) { args -> 
        ViewModelState(
            links = args[0] as List<Link>,
            query = args[1] as String,
            error = args[2] as String?,
            isLoading = args[3] as Boolean,
            showMenu = args[4] as Boolean,
            showProfile = args[5] as Boolean,
            selectedFilter = args[6] as LinkFilter
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
            HomeScreenEvent.OnErrorDismiss -> {
                _error.value = null
            }
            HomeScreenEvent.OnMenuClick -> {
                _showMenu.value = true
            }
            HomeScreenEvent.OnMenuDismiss -> {
                _showMenu.value = false
            }
            HomeScreenEvent.OnProfileClick -> {
                _showProfile.value = true
            }
            HomeScreenEvent.OnProfileDismiss -> {
                _showProfile.value = false
            }
        }
    }
}
