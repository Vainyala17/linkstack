package com.hp77.linkstash.data.mapper

import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.TagEntity
import com.hp77.linkstash.data.local.relation.LinkWithTags
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag

fun LinkEntity.toLink() = Link(
    id = id,
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
    tags = emptyList()
)

fun Link.toLinkEntity() = LinkEntity(
    id = id,
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
    syncError = syncError
)

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
)
