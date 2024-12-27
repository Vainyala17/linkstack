package com.hp77.linkstash.domain.model

data class GitHubProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String,
    val bio: String?,
    val location: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String
)

data class HackerNewsProfile(
    val username: String,
    val karma: Int,
    val about: String?,
    val createdAt: Long
)
