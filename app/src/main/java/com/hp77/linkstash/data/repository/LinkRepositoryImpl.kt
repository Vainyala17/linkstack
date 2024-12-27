package com.hp77.linkstash.data.repository

import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.mapper.toLink
import com.hp77.linkstash.data.mapper.toLinkEntity
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkDao: LinkDao
) : LinkRepository {

    override suspend fun insertLink(link: Link) {
        Logger.d("LinkRepository", "Inserting new link: id=${link.id}, url=${link.url}")
        // First insert the link
        val linkEntity = link.toLinkEntity()
        linkDao.insertLink(linkEntity)
        
        // Then create cross references for each tag
        link.tags.forEach { tag ->
            try {
                val crossRef = LinkTagCrossRef(
                    linkId = link.id,
                    tagId = tag.id
                )
                linkDao.insertLinkTagCrossRef(crossRef)
            } catch (e: Exception) {
                Logger.e("LinkRepository", "Error inserting tag crossRef for link ${link.id}", e)
                e.printStackTrace()
            }
        }
    }

    override suspend fun updateLink(link: Link) {
        Logger.d("LinkRepository", "Updating link: id=${link.id}, url=${link.url}")
        // First update the link
        val linkEntity = link.toLinkEntity()
        linkDao.updateLink(linkEntity)
        
        try {
            // Then update tag relationships
            linkDao.deleteAllTagsForLink(link.id)
            link.tags.forEach { tag ->
                val crossRef = LinkTagCrossRef(
                    linkId = link.id,
                    tagId = tag.id
                )
                linkDao.insertLinkTagCrossRef(crossRef)
            }
        } catch (e: Exception) {
            Logger.e("LinkRepository", "Error updating tags for link ${link.id}", e)
            e.printStackTrace()
        }
    }

    override suspend fun deleteLink(link: Link) {
        Logger.d("LinkRepository", "Deleting link: id=${link.id}")
        val linkEntity = link.toLinkEntity()
        linkDao.deleteLink(linkEntity)
    }

    override suspend fun getLinkById(id: String): Link? {
        Logger.d("LinkRepository", "Fetching link with id: $id")
        val linkWithTags = linkDao.getLinkWithTags(id)
        if (linkWithTags == null) {
            Logger.e("LinkRepository", "Link not found with id: $id")
            return null
        }
        Logger.d("LinkRepository", "Found link: ${linkWithTags.link.url}")
        return linkWithTags.toLink()
    }

    override fun getAllLinks(): Flow<List<Link>> {
        return linkDao.getAllLinksWithTags().map { links ->
            links.map { it.toLink() }
        }
    }

    override fun getActiveLinks(): Flow<List<Link>> {
        return linkDao.getActiveLinks().map { links ->
            links.map { it.toLink() }
        }
    }

    override fun getArchivedLinks(): Flow<List<Link>> {
        return linkDao.getArchivedLinks().map { links ->
            links.map { it.toLink() }
        }
    }

    override fun getFavoriteLinks(): Flow<List<Link>> {
        return linkDao.getFavoriteLinks().map { links ->
            links.map { it.toLink() }
        }
    }

    override fun searchLinks(query: String, tags: List<String>): Flow<List<Link>> {
        return if (tags.isEmpty()) {
            linkDao.searchLinks(query).map { links ->
                links.map { it.toLink() }
            }
        } else {
            linkDao.searchLinksWithTags(query, tags).map { links ->
                links.map { it.toLink() }
            }
        }
    }

    override suspend fun toggleArchive(link: Link) {
        Logger.d("LinkRepository", "Toggling archive for link: id=${link.id}")
        val updatedLink = link.copy(isArchived = !link.isArchived)
        updateLink(updatedLink)
    }

    override suspend fun toggleFavorite(link: Link) {
        Logger.d("LinkRepository", "Toggling favorite for link: id=${link.id}")
        val updatedLink = link.copy(isFavorite = !link.isFavorite)
        updateLink(updatedLink)
    }

    override suspend fun setReminder(link: Link, reminderTime: Long) {
        Logger.d("LinkRepository", "Setting reminder for link: id=${link.id}")
        val updatedLink = link.copy(reminderTime = reminderTime)
        updateLink(updatedLink)
    }

    override suspend fun clearReminder(link: Link) {
        Logger.d("LinkRepository", "Clearing reminder for link: id=${link.id}")
        val updatedLink = link.copy(reminderTime = null)
        updateLink(updatedLink)
    }

    override suspend fun toggleStatus(link: Link) {
        Logger.d("LinkRepository", "Toggling status for link: id=${link.id}")
        val now = System.currentTimeMillis()
        val updatedLink = link.copy(
            isCompleted = !link.isCompleted,
            completedAt = if (!link.isCompleted) now else null
        )
        updateLink(updatedLink)
    }

    override suspend fun cleanupInvalidLinks(): Int {
        Logger.d("LinkRepository", "Cleaning up invalid links")
        return linkDao.deleteInvalidLinks()
    }
}
