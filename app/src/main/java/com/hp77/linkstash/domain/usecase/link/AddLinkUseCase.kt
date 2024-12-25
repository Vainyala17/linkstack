package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import javax.inject.Inject

class AddLinkUseCase @Inject constructor(
    private val repository: LinkRepository
) {
    suspend operator fun invoke(link: Link) {
        require(link.url.isNotBlank()) { "URL cannot be empty" }
        require(link.tags.isNotEmpty()) { "At least one tag is required" }
        repository.insertLink(link)
    }
}
