package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import javax.inject.Inject

class UpdateLinkUseCase @Inject constructor(
    private val linkRepository: LinkRepository
) {
    suspend operator fun invoke(link: Link) {
        linkRepository.updateLink(link)
    }
}
