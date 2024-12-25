package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link

sealed class HomeScreenEvent {
    data class OnSearchQueryChange(val query: String) : HomeScreenEvent()
    data class OnToggleFavorite(val link: Link) : HomeScreenEvent()
    data class OnToggleArchive(val link: Link) : HomeScreenEvent()
    object OnErrorDismiss : HomeScreenEvent()
    object OnMenuClick : HomeScreenEvent()
    object OnMenuDismiss : HomeScreenEvent()
    object OnProfileClick : HomeScreenEvent()
    object OnProfileDismiss : HomeScreenEvent()
}
