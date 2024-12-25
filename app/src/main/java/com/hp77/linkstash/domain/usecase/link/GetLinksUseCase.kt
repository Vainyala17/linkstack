package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed class LinkFilter {
    object All : LinkFilter()
    object Active : LinkFilter()
    object Archived : LinkFilter()
    object Favorites : LinkFilter()
    data class Search(val query: String) : LinkFilter()
}

class GetLinksUseCase @Inject constructor(
    private val repository: LinkRepository
) {
    operator fun invoke(filter: LinkFilter = LinkFilter.All): Flow<List<Link>> {
        return when (filter) {
            is LinkFilter.All -> repository.getAllLinks()
            is LinkFilter.Active -> repository.getActiveLinks()
            is LinkFilter.Archived -> repository.getArchivedLinks()
            is LinkFilter.Favorites -> repository.getFavoriteLinks()
            is LinkFilter.Search -> repository.searchLinks(filter.query)
        }
    }
}
