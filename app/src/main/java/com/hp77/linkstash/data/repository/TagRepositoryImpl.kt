package com.hp77.linkstash.data.repository

import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.dao.TagDao
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.mapper.toTag
import com.hp77.linkstash.data.mapper.toTagEntity
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val linkDao: LinkDao
) : TagRepository {

    override suspend fun insertTag(tag: Tag) {
        val tagEntity = tag.toTagEntity()
        tagDao.insertTag(tagEntity)
    }

    override suspend fun updateTag(tag: Tag) {
        val tagEntity = tag.toTagEntity()
        tagDao.updateTag(tagEntity)
    }

    override suspend fun deleteTag(tag: Tag) {
        val tagEntity = tag.toTagEntity()
        tagDao.deleteTag(tagEntity)
    }

    override suspend fun getTagById(id: String): Tag? {
        return tagDao.getTagById(id)?.toTag()
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { tags ->
            tags.map { it.toTag() }
        }
    }

    override fun searchTags(query: String): Flow<List<Tag>> {
        return tagDao.searchTags(query).map { tags ->
            tags.map { it.toTag() }
        }
    }

    override fun getTagsForLink(linkId: String): Flow<List<Tag>> {
        return tagDao.getTagsForLink(linkId).map { tags ->
            tags.map { it.toTag() }
        }
    }

    override suspend fun addTagToLink(tag: Tag, link: Link) {
        val crossRef = LinkTagCrossRef(
            linkId = link.id,
            tagId = tag.id
        )
        linkDao.insertLinkTagCrossRef(crossRef)
    }

    override suspend fun removeTagFromLink(tag: Tag, link: Link) {
        val crossRef = LinkTagCrossRef(
            linkId = link.id,
            tagId = tag.id
        )
        linkDao.deleteLinkTagCrossRef(crossRef)
    }

    override suspend fun isTagExists(name: String): Boolean {
        return tagDao.isTagExists(name)
    }

    override suspend fun getLinkedLinksCount(tagId: String): Int {
        return tagDao.getLinkedLinksCount(tagId)
    }

    override suspend fun getOrCreateTag(name: String): Tag {
        val trimmedName = name.trim()
        
        // First try to find existing tag
        val existingTag = tagDao.getAllTags()
            .map { tags -> 
                tags.find { it.name.equals(trimmedName, ignoreCase = true) }?.toTag() 
            }
            .firstOrNull()

        // If found, return it
        if (existingTag != null) {
            return existingTag
        }

        // Otherwise create new tag
        val newTag = Tag(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            color = null,
            createdAt = System.currentTimeMillis()
        )
        insertTag(newTag)
        return newTag
    }
}
