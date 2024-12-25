package com.hp77.linkstash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hp77.linkstash.domain.model.LinkType
import java.util.UUID

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String?,
    val description: String?,
    val previewImageUrl: String?,
    val type: LinkType = LinkType.OTHER,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
