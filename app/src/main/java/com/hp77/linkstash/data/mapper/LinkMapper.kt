package com.hp77.linkstash.data.mapper

import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.TagEntity
import com.hp77.linkstash.data.local.relation.LinkWithTags
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag

fun LinkEntity.toLink(tags: List<Tag> = emptyList()) = Link(
    id = id,
    url = url,
    title = title,
    description = description,
    previewImageUrl = previewImageUrl,
    createdAt = createdAt,
    reminderTime = reminderTime,
    isArchived = isArchived,
    isFavorite = isFavorite,
    tags = tags
)

fun Link.toLinkEntity() = LinkEntity(
    id = id,
    url = url,
    title = title,
    description = description,
    previewImageUrl = previewImageUrl,
    createdAt = createdAt,
    reminderTime = reminderTime,
    isArchived = isArchived,
    isFavorite = isFavorite,
    tags = tags.map { it.name }
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

fun LinkWithTags.toLink() = Link(
    id = link.id,
    url = link.url,
    title = link.title,
    description = link.description,
    previewImageUrl = link.previewImageUrl,
    createdAt = link.createdAt,
    reminderTime = link.reminderTime,
    isArchived = link.isArchived,
    isFavorite = link.isFavorite,
    tags = tags.map { it.toTag() }
)
