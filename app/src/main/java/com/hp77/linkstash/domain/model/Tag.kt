package com.hp77.linkstash.domain.model

data class Tag(
    val id: String,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
