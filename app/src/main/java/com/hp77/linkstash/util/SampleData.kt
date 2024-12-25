package com.hp77.linkstash.util

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import java.util.UUID

object SampleData {
    val sampleTags = listOf(
        Tag(
            id = UUID.randomUUID().toString(),
            name = "Android",
            color = "#4CAF50",
            createdAt = System.currentTimeMillis()
        ),
        Tag(
            id = UUID.randomUUID().toString(),
            name = "Kotlin",
            color = "#7C4DFF",
            createdAt = System.currentTimeMillis()
        ),
        Tag(
            id = UUID.randomUUID().toString(),
            name = "Tutorial",
            color = "#FF9800",
            createdAt = System.currentTimeMillis()
        ),
        Tag(
            id = UUID.randomUUID().toString(),
            name = "Blog",
            color = "#2196F3",
            createdAt = System.currentTimeMillis()
        )
    )

    val sampleLinks = listOf(
        Link(
            id = UUID.randomUUID().toString(),
            url = "https://developer.android.com/jetpack/compose",
            title = "Jetpack Compose Documentation",
            description = "Build better apps faster with Jetpack Compose",
            previewImageUrl = null,
            createdAt = System.currentTimeMillis(),
            reminderTime = null,
            isArchived = false,
            isFavorite = true,
            tags = listOf(sampleTags[0], sampleTags[1])
        ),
        Link(
            id = UUID.randomUUID().toString(),
            url = "https://kotlinlang.org/docs/coroutines-overview.html",
            title = "Kotlin Coroutines Overview",
            description = "Learn about coroutines and how they help manage background threads",
            previewImageUrl = null,
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            reminderTime = null,
            isArchived = false,
            isFavorite = false,
            tags = listOf(sampleTags[1], sampleTags[2])
        ),
        Link(
            id = UUID.randomUUID().toString(),
            url = "https://blog.example.com/android-development",
            title = "Android Development Best Practices",
            description = "A comprehensive guide to Android development best practices and tips",
            previewImageUrl = null,
            createdAt = System.currentTimeMillis() - 172800000, // 2 days ago
            reminderTime = System.currentTimeMillis() + 86400000, // Tomorrow
            isArchived = false,
            isFavorite = true,
            tags = listOf(sampleTags[0], sampleTags[3])
        ),
        Link(
            id = UUID.randomUUID().toString(),
            url = "https://example.com/archived-link",
            title = "Old Android Tutorial",
            description = "An archived tutorial about Android development",
            previewImageUrl = null,
            createdAt = System.currentTimeMillis() - 2592000000, // 30 days ago
            reminderTime = null,
            isArchived = true,
            isFavorite = false,
            tags = listOf(sampleTags[0], sampleTags[2])
        )
    )
}
