package com.hp77.linkstash.data.repository

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.util.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestLinkRepository @Inject constructor() : LinkRepository {
    private val links = MutableStateFlow(SampleData.sampleLinks)

    override suspend fun insertLink(link: Link) {
        links.value = links.value + link
    }

    override suspend fun updateLink(link: Link) {
        links.value = links.value.map { if (it.id == link.id) link else it }
    }

    override suspend fun deleteLink(link: Link) {
        links.value = links.value.filter { it.id != link.id }
    }

    override suspend fun getLinkById(id: String): Link? {
        return links.value.find { it.id == id }
    }

    override fun getAllLinks(): Flow<List<Link>> = links

    override fun getActiveLinks(): Flow<List<Link>> = links.map { links ->
        links.filter { !it.isArchived }
    }

    override fun getArchivedLinks(): Flow<List<Link>> = links.map { links ->
        links.filter { it.isArchived }
    }

    override fun getFavoriteLinks(): Flow<List<Link>> = links.map { links ->
        links.filter { it.isFavorite }
    }

    override fun searchLinks(query: String): Flow<List<Link>> = links.map { links ->
        links.filter {
            it.url.contains(query, ignoreCase = true) ||
            it.title?.contains(query, ignoreCase = true) == true ||
            it.description?.contains(query, ignoreCase = true) == true ||
            it.tags.any { tag -> tag.name.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun toggleArchive(link: Link) {
        updateLink(link.copy(isArchived = !link.isArchived))
    }

    override suspend fun toggleFavorite(link: Link) {
        updateLink(link.copy(isFavorite = !link.isFavorite))
    }

    override suspend fun setReminder(link: Link, reminderTime: Long) {
        updateLink(link.copy(reminderTime = reminderTime))
    }

    override suspend fun clearReminder(link: Link) {
        updateLink(link.copy(reminderTime = null))
    }
}
