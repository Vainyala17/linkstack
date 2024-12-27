package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.util.Logger
import javax.inject.Inject

class CleanupInvalidLinksUseCase @Inject constructor(
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(): Result<Int> = runCatching {
        Logger.d("CleanupInvalidLinksUseCase", "Starting cleanup of invalid links")
        val count = linkRepository.cleanupInvalidLinks()
        Logger.d("CleanupInvalidLinksUseCase", "Cleanup completed. Removed $count invalid links")
        count
    }
}
