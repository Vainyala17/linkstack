package com.hp77.linkstash.domain.model

data class Link(
    val id: String,
    val url: String,
    val title: String?,
    val description: String?,
    val previewImageUrl: String?,
    val type: LinkType = LinkType.OTHER,
    val createdAt: Long,
    val reminderTime: Long?,
    val isArchived: Boolean,
    val isFavorite: Boolean,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null,
    // HackerNews integration
    val hackerNewsId: String? = null,
    val hackerNewsUrl: String? = null,
    // GitHub sync
    val lastSyncedAt: Long? = null,
    val syncError: String? = null,
    val scrollPosition: Float = 0f,
    val tags: List<Tag>
) {
    companion object {
        fun empty() = Link(
            id = "",
            url = "",
            title = null,
            description = null,
            previewImageUrl = null,
            type = LinkType.OTHER,
            createdAt = System.currentTimeMillis(),
            reminderTime = null,
            isArchived = false,
            isFavorite = false,
            isCompleted = false,
            completedAt = null,
            notes = null,
            hackerNewsId = null,
            hackerNewsUrl = null,
            lastSyncedAt = null,
            syncError = null,
            scrollPosition = 0f,
            tags = emptyList()
        )
    }
}
