package com.hp77.linkstash.domain.repository

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
    suspend fun getTagById(id: String): Tag?
    fun getAllTags(): Flow<List<Tag>>
    fun searchTags(query: String): Flow<List<Tag>>
    fun getTagsForLink(linkId: String): Flow<List<Tag>>
    suspend fun addTagToLink(tag: Tag, link: Link)
    suspend fun removeTagFromLink(tag: Tag, link: Link)
    suspend fun isTagExists(name: String): Boolean
    suspend fun getOrCreateTag(name: String): Tag
}
