package com.hp77.linkstash.data.mapper

import com.hp77.linkstash.data.local.entity.GitHubProfileEntity
import com.hp77.linkstash.data.local.entity.HackerNewsProfileEntity
import com.hp77.linkstash.data.remote.GitHubUser
import com.hp77.linkstash.data.remote.HackerNewsUser
import com.hp77.linkstash.domain.model.GitHubProfile
import com.hp77.linkstash.domain.model.HackerNewsProfile

fun GitHubUser.toEntity() = GitHubProfileEntity(
    login = login,
    name = name,
    avatarUrl = avatarUrl,
    bio = bio,
    location = location,
    publicRepos = publicRepos,
    followers = followers,
    following = following,
    createdAt = createdAt,
    lastFetchedAt = System.currentTimeMillis()
)

fun GitHubProfileEntity.toProfile() = GitHubProfile(
    login = login,
    name = name,
    avatarUrl = avatarUrl,
    bio = bio,
    location = location,
    publicRepos = publicRepos,
    followers = followers,
    following = following,
    createdAt = createdAt
)

fun HackerNewsUser.toEntity() = HackerNewsProfileEntity(
    username = username,
    karma = karma,
    about = about,
    createdAt = created,
    lastFetchedAt = System.currentTimeMillis()
)

fun HackerNewsProfileEntity.toProfile() = HackerNewsProfile(
    username = username,
    karma = karma,
    about = about,
    createdAt = createdAt
)
