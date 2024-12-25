package com.hp77.linkstash.data.repository

import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.mapper.toLink
import com.hp77.linkstash.data.mapper.toLinkEntity
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkDao: LinkDao
) : LinkRepository {

    override suspend fun insertLink(link: Link) {
        val linkEntity = link.toLinkEntity()
        linkDao.insertLink(linkEntity)
    }

    override suspend fun updateLink(link: Link) {
        val linkEntity = link.toLinkEntity()
        linkDao.updateLink(linkEntity)
    }

    override suspend fun deleteLink(link: Link) {
        val linkEntity = link.toLinkEntity()
        linkDao.deleteLink(linkEntity)
    }

    override suspend fun getLinkById(id: String): Link? {
        return linkDao.getLinkWithTags(id)?.toLink()
    }

    override fun getAllLinks(): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.map { it.toLink() }
        }
    }

    override fun getActiveLinks(): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.filter { !it.link.isArchived }
                .map { it.toLink() }
        }
    }

    override fun getArchivedLinks(): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.filter { it.link.isArchived }
                .map { it.toLink() }
        }
    }

    override fun getFavoriteLinks(): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.filter { it.link.isFavorite }
                .map { it.toLink() }
        }
    }

    override fun searchLinks(query: String): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.filter {
                it.link.title?.contains(query, ignoreCase = true) == true ||
                it.link.description?.contains(query, ignoreCase = true) == true ||
                it.link.url.contains(query, ignoreCase = true) ||
                it.tags.any { tag -> tag.name.contains(query, ignoreCase = true) }
            }.map { it.toLink() }
        }
    }

    override suspend fun toggleArchive(link: Link) {
        val updatedLink = link.copy(isArchived = !link.isArchived)
        updateLink(updatedLink)
    }

    override suspend fun toggleFavorite(link: Link) {
        val updatedLink = link.copy(isFavorite = !link.isFavorite)
        updateLink(updatedLink)
    }

    override suspend fun setReminder(link: Link, reminderTime: Long) {
        val updatedLink = link.copy(reminderTime = reminderTime)
        updateLink(updatedLink)
    }

    override suspend fun clearReminder(link: Link) {
        val updatedLink = link.copy(reminderTime = null)
        updateLink(updatedLink)
    }
}
