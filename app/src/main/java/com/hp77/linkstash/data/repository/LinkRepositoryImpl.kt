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
                // Log error but continue with other tags
                e.printStackTrace()
            }
        }
    }

    override suspend fun updateLink(link: Link) {
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
            // Log error but don't fail the update
            e.printStackTrace()
        }
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
