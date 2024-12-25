package com.hp77.linkstash.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.usecase.link.GetLinksUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkStateUseCase
import com.hp77.linkstash.domain.usecase.tag.ManageTagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLinksUseCase: GetLinksUseCase,
    private val updateLinkStateUseCase: UpdateLinkStateUseCase,
    private val manageTagsUseCase: ManageTagsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow<LinkFilter>(LinkFilter.All)
    private val _selectedTags = MutableStateFlow<List<Tag>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _links = combine(
        _searchQuery,
        _selectedFilter,
        _selectedTags
    ) { query, filter, tags ->
        Triple(query, filter, tags)
    }.flatMapLatest { (query, filter, tags) ->
        val effectiveFilter = if (query.isNotEmpty()) {
            LinkFilter.Search(query)
        } else {
            filter
        }
        getLinksUseCase(effectiveFilter).map { links ->
            links.filter { link ->
                val matchesTags = tags.isEmpty() || tags.all { tag ->
                    link.tags.contains(tag)
                }
                matchesTags
            }
        }
    }

    private val _tags = manageTagsUseCase.getTags(com.hp77.linkstash.domain.usecase.tag.TagFilter.All)

    val state: StateFlow<HomeScreenState> = combine(
        combine(
            combine(
                _links,
                _tags
            ) { links, tags -> Pair(links, tags) },
            combine(
                _searchQuery,
                _selectedFilter
            ) { query, filter -> Pair(query, filter) }
        ) { (links, tags), (query, filter) ->
            Triple(links, tags, Pair(query, filter))
        },
        combine(
            _selectedTags,
            combine(
                _error,
                _isLoading
            ) { error, isLoading -> Pair(error, isLoading) }
        ) { selectedTags, (error, isLoading) ->
            Pair(selectedTags, Pair(error, isLoading))
        }
    ) { (links, tags, queryFilter), (selectedTags, errorLoading) ->
        val (query, filter) = queryFilter
        val (error, isLoading) = errorLoading
        HomeScreenState(
            links = links,
            tags = tags,
            searchQuery = query,
            selectedFilter = filter,
            selectedTags = selectedTags,
            error = error,
            isLoading = isLoading
        )
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
            is HomeScreenEvent.OnTagSelect -> {
                _selectedTags.value = _selectedTags.value + event.tag
            }
            is HomeScreenEvent.OnTagDeselect -> {
                _selectedTags.value = _selectedTags.value - event.tag
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
            HomeScreenEvent.OnErrorDismiss -> {
                _error.value = null
            }
            is HomeScreenEvent.OnLinkClick,
            HomeScreenEvent.OnAddLinkClick -> {
                // Handle navigation events in the UI layer
            }
        }
    }
}
