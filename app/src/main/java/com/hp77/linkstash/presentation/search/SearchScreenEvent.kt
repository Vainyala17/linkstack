package com.hp77.linkstash.presentation.search

import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag

sealed class SearchScreenEvent {
    data class OnSearchQueryChange(val query: String) : SearchScreenEvent()
    data class OnFilterSelect(val filter: LinkFilter) : SearchScreenEvent()
    data class OnTagSelect(val tag: Tag) : SearchScreenEvent()
    data class OnTagDeselect(val tag: Tag) : SearchScreenEvent()
    object OnErrorDismiss : SearchScreenEvent()
    data class OnRecentSearchClick(val query: String) : SearchScreenEvent()
    data class OnToggleFavorite(val link: Link) : SearchScreenEvent()
    data class OnToggleArchive(val link: Link) : SearchScreenEvent()
}
