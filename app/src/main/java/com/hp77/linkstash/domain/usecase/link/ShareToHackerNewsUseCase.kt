package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.data.repository.HackerNewsRepository
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import javax.inject.Inject

class ShareToHackerNewsUseCase @Inject constructor(
    private val hackerNewsRepository: HackerNewsRepository,
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(linkId: Long): Result<String> = runCatching {
        if (!hackerNewsRepository.isAuthenticated()) {
            throw IllegalStateException("Not authenticated with HackerNews")
        }

        // Get the link
        val link = linkRepository.getLinkById(linkId.toString()) ?: throw IllegalArgumentException("Link not found")
        
        // Submit to HackerNews
        val hnUrl = hackerNewsRepository.submitStory(link).getOrThrow()
        
        // Update link with HN URL and ID
        val hnId = hnUrl.substringAfterLast("id=")
        linkRepository.updateLink(link.copy(
            hackerNewsId = hnId,
            hackerNewsUrl = hnUrl
        ))
        
        hnUrl
    }
}
