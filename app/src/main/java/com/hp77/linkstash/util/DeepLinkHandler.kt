package com.hp77.linkstash.util

import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.entity.LinkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DeepLinkHandler @Inject constructor(
    private val linkDao: LinkDao
) {
    suspend fun handleDeepLink(url: String): String = withContext(Dispatchers.IO) {
        // First try to find an existing link with this URL
        val existingLink = linkDao.getLinkByUrl(url)
        if (existingLink != null) {
            return@withContext existingLink.id
        }

        // If no existing link found, create a new one
        val newLink = LinkEntity(
            id = UUID.randomUUID().toString(),
            url = url,
            title = null, // Title will be updated when page loads
            description = null,
            previewImageUrl = null,
            createdAt = System.currentTimeMillis()
        )
        linkDao.insertLink(newLink)
        return@withContext newLink.id
    }
}
