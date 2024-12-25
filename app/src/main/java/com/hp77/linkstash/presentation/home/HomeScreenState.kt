package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link

data class HomeScreenState(
    val links: List<Link> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val showMenu: Boolean = false,
    val showProfile: Boolean = false
)
