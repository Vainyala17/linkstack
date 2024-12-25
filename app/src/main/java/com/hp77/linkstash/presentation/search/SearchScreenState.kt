package com.hp77.linkstash.presentation.search

import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag

data class SearchScreenState(
    val searchQuery: String = "",
    val tags: List<Tag> = emptyList(),
    val selectedTags: List<Tag> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val searchResults: List<Link> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
