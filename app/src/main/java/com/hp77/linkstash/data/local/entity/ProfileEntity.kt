package com.hp77.linkstash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "github_profiles")
data class GitHubProfileEntity(
    @PrimaryKey
    val login: String,
    val name: String?,
    val avatarUrl: String,
    val bio: String?,
    val location: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val lastFetchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hackernews_profiles")
data class HackerNewsProfileEntity(
    @PrimaryKey
    val username: String,
    val karma: Int,
    val about: String?,
    val createdAt: Long,
    val lastFetchedAt: Long = System.currentTimeMillis()
)
