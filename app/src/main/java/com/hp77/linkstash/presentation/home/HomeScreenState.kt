package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link
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

sealed class LinkFilter(val title: String) {
    object All : LinkFilter("All")
    object Active : LinkFilter("Active")
    object Archived : LinkFilter("Archive")
    object Favorites : LinkFilter("Favorites")
}

sealed class HomeScreenEvent {
    data class OnSearchQueryChange(val query: String) : HomeScreenEvent()
    data class OnFilterSelect(val filter: LinkFilter) : HomeScreenEvent()
    data class OnTagSelect(val tag: Tag) : HomeScreenEvent()
    data class OnTagDeselect(val tag: Tag) : HomeScreenEvent()
    data class OnLinkClick(val link: Link) : HomeScreenEvent()
    data class OnToggleFavorite(val link: Link) : HomeScreenEvent()
    data class OnToggleArchive(val link: Link) : HomeScreenEvent()
    object OnAddLinkClick : HomeScreenEvent()
    object OnErrorDismiss : HomeScreenEvent()
}
