package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.data.preferences.ThemeMode

data class HomeScreenState(
    val links: List<Link> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val showMenu: Boolean = false,
    val showProfile: Boolean = false,
    val showShareSheet: Boolean = false,
    val selectedLink: Link? = null,
    val selectedFilter: LinkFilter = LinkFilter.All,
    val currentTheme: ThemeMode = ThemeMode.SYSTEM
)
