package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Tag

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
