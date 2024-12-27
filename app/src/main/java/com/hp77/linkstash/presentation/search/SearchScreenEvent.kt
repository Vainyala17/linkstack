package com.hp77.linkstash.presentation.search

import com.hp77.linkstash.domain.model.LinkFilter
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag

sealed class SearchScreenEvent {
    data class OnSearchQueryChange(val query: String) : SearchScreenEvent()
    data class OnTagSelect(val tag: Tag) : SearchScreenEvent()
    data class OnTagDeselect(val tag: Tag) : SearchScreenEvent()
    data class OnDeleteLink(val link: Link) : SearchScreenEvent()
    object OnErrorDismiss : SearchScreenEvent()
    data class OnRecentSearchClick(val query: String) : SearchScreenEvent()
    data class OnShowShareSheet(val link: Link) : SearchScreenEvent()
    object OnDismissShareSheet : SearchScreenEvent()
    data class OnShareToHackerNews(val link: Link) : SearchScreenEvent()
    data class OnSyncToGitHub(val link: Link) : SearchScreenEvent()
}
