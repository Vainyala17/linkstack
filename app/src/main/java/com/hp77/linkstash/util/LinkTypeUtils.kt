package com.hp77.linkstash.util

import com.hp77.linkstash.domain.model.LinkType

object LinkTypeUtils {
    // Default tags for each link type
    private val JOB_TAGS = setOf("job", "career", "interview", "position", "hiring", "work", "employment")
    private val BLOG_TAGS = setOf("blog", "article", "post", "read", "reading")
    private val VIDEO_TAGS = setOf("video", "youtube", "tutorial", "course", "watch", "watching")
    private val BOOK_TAGS = setOf("book", "ebook", "reading", "literature", "textbook")

    // Get default tags for a link type
    fun getDefaultTags(type: LinkType): Set<String> = when(type) {
        LinkType.JOB -> JOB_TAGS
        LinkType.BLOG -> BLOG_TAGS
        LinkType.VIDEO -> VIDEO_TAGS
        LinkType.BOOKS -> BOOK_TAGS
        LinkType.OTHER -> emptySet()
    }

    // Infer link type from tags
    fun inferLinkType(tags: List<String>): LinkType {
        val lowerTags = tags.map { it.lowercase() }.toSet()
        return when {
            lowerTags.any { it in JOB_TAGS } -> LinkType.JOB
            lowerTags.any { it in BLOG_TAGS } -> LinkType.BLOG
            lowerTags.any { it in VIDEO_TAGS } -> LinkType.VIDEO
            lowerTags.any { it in BOOK_TAGS } -> LinkType.BOOKS
            else -> LinkType.OTHER
        }
    }

    // Get suggested tags based on URL pattern
    fun getSuggestedTags(url: String): Set<String> {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains("job") || lowerUrl.contains("career") || 
            lowerUrl.contains("linkedin.com/jobs") || lowerUrl.contains("indeed.com") -> JOB_TAGS
            lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be") -> VIDEO_TAGS
            lowerUrl.contains("medium.com") || lowerUrl.contains("dev.to") || 
            lowerUrl.contains("blog") || lowerUrl.contains("article") -> BLOG_TAGS
            lowerUrl.contains("book") || lowerUrl.contains("read") -> BOOK_TAGS
            else -> emptySet()
        }
    }
}
