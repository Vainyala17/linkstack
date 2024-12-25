package com.hp77.linkstash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String? = null, // Hex color code
    val createdAt: Long = System.currentTimeMillis()
)
