package com.hp77.linkstash.domain.usecase.profile

import com.hp77.linkstash.data.repository.HackerNewsRepository
import com.hp77.linkstash.domain.model.HackerNewsProfile
import javax.inject.Inject

class GetHackerNewsProfileUseCase @Inject constructor(
    private val hackerNewsRepository: HackerNewsRepository
) {
    suspend operator fun invoke(): Result<HackerNewsProfile?> = runCatching {
        if (!hackerNewsRepository.isAuthenticated()) {
            return@runCatching null
        }

        val user = hackerNewsRepository.getCurrentUser().getOrThrow()
        HackerNewsProfile(
            username = user.username,
            karma = user.karma,
            about = user.about,
            createdAt = user.created
        )
    }
}
