package com.hp77.linkstash.domain.usecase.sync

import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.domain.repository.LinkRepository
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

class SyncLinksToGitHubUseCase @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository,
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!gitHubSyncRepository.isAuthenticated()) {
            throw IllegalStateException("Not authenticated with GitHub")
        }

        // Get all links
        val links = linkRepository.getAllLinks().toList().firstOrNull() ?: emptyList()
        
        // Sync to GitHub
        gitHubSyncRepository.syncLinks(links).getOrThrow()
    }
}
