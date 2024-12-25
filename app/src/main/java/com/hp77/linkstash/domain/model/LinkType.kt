package com.hp77.linkstash.domain.model

enum class LinkType {
    JOB, BLOG, VIDEO, BOOKS, OTHER;

    fun getStatusLabel(isCompleted: Boolean): String = when(this) {
        JOB -> if(isCompleted) "Applied" else "Not Applied"
        BLOG -> if(isCompleted) "Read" else "Unread"
        VIDEO -> if(isCompleted) "Watched" else "Unwatched"
        BOOKS -> if(isCompleted) "Read" else "Unread"
        OTHER -> if(isCompleted) "Done" else "Pending"
    }
}
