package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import javax.inject.Inject

class ToggleLinkStatusUseCase @Inject constructor(
    private val repository: LinkRepository
) {
    suspend operator fun invoke(link: Link) {
        val now = System.currentTimeMillis()
        repository.updateLink(
            link.copy(
                isCompleted = !link.isCompleted,
                completedAt = if (!link.isCompleted) now else null
            )
        )
    }
}
