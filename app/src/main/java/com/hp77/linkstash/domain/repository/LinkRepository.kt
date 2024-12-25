package com.hp77.linkstash.domain.repository

import com.hp77.linkstash.domain.model.Link
import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    suspend fun insertLink(link: Link)
    suspend fun updateLink(link: Link)
    suspend fun deleteLink(link: Link)
    suspend fun getLinkById(id: String): Link?
    fun getAllLinks(): Flow<List<Link>>
    fun getActiveLinks(): Flow<List<Link>>
    fun getArchivedLinks(): Flow<List<Link>>
    fun getFavoriteLinks(): Flow<List<Link>>
    fun searchLinks(query: String): Flow<List<Link>>
    suspend fun toggleArchive(link: Link)
    suspend fun toggleFavorite(link: Link)
    suspend fun setReminder(link: Link, reminderTime: Long)
    suspend fun clearReminder(link: Link)
}
