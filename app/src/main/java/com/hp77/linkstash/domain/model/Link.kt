package com.hp77.linkstash.domain.model

data class Link(
    val id: String,
    val url: String,
    val title: String?,
    val description: String?,
    val previewImageUrl: String?,
    val createdAt: Long,
    val reminderTime: Long?,
    val isArchived: Boolean,
    val isFavorite: Boolean,
    val tags: List<Tag>
) {
    companion object {
        fun empty() = Link(
            id = "",
            url = "",
            title = null,
            description = null,
            previewImageUrl = null,
            createdAt = System.currentTimeMillis(),
            reminderTime = null,
            isArchived = false,
            isFavorite = false,
            tags = emptyList()
        )
    }
}

data class Tag(
    val id: String,
    val name: String,
    val color: String?,
    val createdAt: Long
) {
    companion object {
        fun empty() = Tag(
            id = "",
            name = "",
            color = null,
            createdAt = System.currentTimeMillis()
        )
    }
}
