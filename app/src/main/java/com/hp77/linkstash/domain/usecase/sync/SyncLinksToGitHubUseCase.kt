package com.hp77.linkstash.domain.usecase.sync

import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.util.Logger
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val TAG = "SyncLinksToGitHubUseCase"

data class SyncResult(
    val totalLinks: Int,
    val newLinks: Int
)

class SyncLinksToGitHubUseCase @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository,
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(): Result<SyncResult> = runCatching {
        Logger.d(TAG, "Starting GitHub sync process")
        
        // Check authentication
        Logger.d(TAG, "Checking GitHub authentication")
        if (!gitHubSyncRepository.isAuthenticated()) {
            val error = "Not authenticated with GitHub"
            Logger.e(TAG, error)
            throw IllegalStateException(error)
        }
        Logger.d(TAG, "GitHub authentication verified")

        // Get all links
        Logger.d(TAG, "Fetching all links")
        val links = linkRepository.getAllLinks().first()
        Logger.d(TAG, "Found ${links.size} links to sync")
        
        if (links.isEmpty()) {
            Logger.d(TAG, "No links to sync, skipping")
            return@runCatching SyncResult(totalLinks = 0, newLinks = 0)
        }
        
        // Get initial count of links
        val initialCount = links.size
        
        // Sync to GitHub
        Logger.d(TAG, "Starting sync to GitHub")
        try {
            gitHubSyncRepository.syncLinks(links).getOrThrow()
            Logger.d(TAG, "Successfully synced ${links.size} links to GitHub")
            
            // Get final count of links to determine how many were added
            val finalLinks = linkRepository.getAllLinks().first()
            val newLinks = finalLinks.size - initialCount
            
            SyncResult(
                totalLinks = finalLinks.size,
                newLinks = newLinks
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to sync links to GitHub", e)
            throw e
        }
    }
}
