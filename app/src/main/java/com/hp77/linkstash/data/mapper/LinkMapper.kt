package com.hp77.linkstash.data.mapper

import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.TagEntity
import com.hp77.linkstash.data.local.relation.LinkWithTags
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.util.Logger
import java.util.UUID

fun LinkEntity.toLink() = Link(
    id = id.also { Logger.d("LinkMapper", "Converting LinkEntity to Link with id: $it") },
    url = url,
    title = title,
    description = description,
    previewImageUrl = previewImageUrl,
    type = type,
    createdAt = createdAt,
    reminderTime = reminderTime,
    isArchived = isArchived,
    isFavorite = isFavorite,
    isCompleted = isCompleted,
    completedAt = completedAt,
    notes = notes,
    hackerNewsId = hackerNewsId,
    hackerNewsUrl = hackerNewsUrl,
    lastSyncedAt = lastSyncedAt,
    syncError = syncError,
    scrollPosition = scrollPosition,
    tags = emptyList()
)

fun Link.toLinkEntity(): LinkEntity {
    val entityId = if (id.isBlank()) {
        UUID.randomUUID().toString().also {
            Logger.d("LinkMapper", "Generated new UUID for link: $it")
        }
    } else {
        id.also { Logger.d("LinkMapper", "Using existing id for link: $it") }
    }
    
    return LinkEntity(
        id = entityId,
        url = url,
        title = title,
        description = description,
        previewImageUrl = previewImageUrl,
        type = type,
        createdAt = createdAt,
        reminderTime = reminderTime,
        isArchived = isArchived,
        isFavorite = isFavorite,
        isCompleted = isCompleted,
        completedAt = completedAt,
        notes = notes,
        hackerNewsId = hackerNewsId,
        hackerNewsUrl = hackerNewsUrl,
        lastSyncedAt = lastSyncedAt,
        syncError = syncError,
        scrollPosition = scrollPosition
    )
}

fun TagEntity.toTag() = Tag(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun Tag.toTagEntity() = TagEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun LinkWithTags.toLink() = link.toLink().copy(
    tags = tags.map { it.toTag() }
).also { 
    Logger.d("LinkMapper", "Converted LinkWithTags to Link: id=${it.id}, url=${it.url}, tagCount=${tags.size}")
}
