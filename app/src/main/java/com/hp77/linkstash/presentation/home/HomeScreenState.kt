package com.hp77.linkstash.presentation.home

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.GitHubProfile
import com.hp77.linkstash.domain.model.HackerNewsProfile
import com.hp77.linkstash.data.preferences.ThemeMode

data class HomeScreenState(
    val links: List<Link> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null,
    val isDrawerOpen: Boolean = false,
    val showProfile: Boolean = false,
    val showShareSheet: Boolean = false,
    val selectedLink: Link? = null,
    val selectedFilter: LinkFilter = LinkFilter.All,
    val currentTheme: ThemeMode = ThemeMode.SYSTEM,
    val githubProfile: GitHubProfile? = null,
    val hackerNewsProfile: HackerNewsProfile? = null,
    val showIssueReportDialog: Boolean = false,
    val issueDescription: String = ""
)
