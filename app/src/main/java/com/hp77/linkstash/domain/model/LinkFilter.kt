package com.hp77.linkstash.domain.model

sealed class LinkFilter {
    abstract val title: String

    data object All : LinkFilter() {
        override val title: String = "All"
    }
    data object Active : LinkFilter() {
        override val title: String = "Active"
    }
    data object Archived : LinkFilter() {
        override val title: String = "Archive"
    }
    data object Favorites : LinkFilter() {
        override val title: String = "Favorites"
    }
    data class Search(val query: String) : LinkFilter() {
        override val title: String = "Search"
    }
}
