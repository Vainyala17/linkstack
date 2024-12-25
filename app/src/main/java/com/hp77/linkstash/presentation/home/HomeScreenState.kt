package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Tag

data class HomeScreenState(
    val links: List<Link> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedFilter: LinkFilter = LinkFilter.All,
    val selectedTags: List<Tag> = emptyList(),
    val error: String? = null
)
