package com.hp77.linkstash.data.repository

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.repository.TagRepository
import com.hp77.linkstash.util.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestTagRepository @Inject constructor() : TagRepository {
    private val tags = MutableStateFlow(SampleData.sampleTags)
    private val tagLinks = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    override suspend fun insertTag(tag: Tag) {
        tags.value = tags.value + tag
    }

    override suspend fun updateTag(tag: Tag) {
        tags.value = tags.value.map { if (it.id == tag.id) tag else it }
    }

    override suspend fun deleteTag(tag: Tag) {
        tags.value = tags.value.filter { it.id != tag.id }
        // Remove tag from all links
        tagLinks.value = tagLinks.value.mapValues { entry ->
            entry.value.filter { it != tag.id }
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        return tags.value.find { it.id == id }
    }

    override fun getAllTags(): Flow<List<Tag>> = tags

    override fun searchTags(query: String): Flow<List<Tag>> = tags.map { tags ->
        tags.filter { it.name.contains(query, ignoreCase = true) }
    }

    override fun getTagsForLink(linkId: String): Flow<List<Tag>> = tags.map { allTags ->
        val linkTagIds = tagLinks.value[linkId] ?: emptyList()
        allTags.filter { it.id in linkTagIds }
    }

    override suspend fun addTagToLink(tag: Tag, link: Link) {
        val currentLinks = tagLinks.value[link.id] ?: emptyList()
        tagLinks.value = tagLinks.value + (link.id to (currentLinks + tag.id))
    }

    override suspend fun removeTagFromLink(tag: Tag, link: Link) {
        val currentLinks = tagLinks.value[link.id] ?: emptyList()
        tagLinks.value = tagLinks.value + (link.id to (currentLinks - tag.id))
    }

    override suspend fun isTagExists(name: String): Boolean {
        return tags.value.any { it.name.equals(name, ignoreCase = true) }
    }

    override suspend fun getOrCreateTag(name: String): Tag {
        val existingTag = tags.value.find { it.name.equals(name, ignoreCase = true) }
        if (existingTag != null) {
            return existingTag
        }

        val newTag = Tag(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            color = null,
            createdAt = System.currentTimeMillis()
        )
        insertTag(newTag)
        return newTag
    }
}
