package com.hp77.linkstash.domain.usecase.profile

import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.domain.model.GitHubProfile
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetGitHubProfileUseCase @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository
) {
    suspend operator fun invoke(): Result<GitHubProfile?> = runCatching {
        // Skip profile fetch during device flow
        if (gitHubSyncRepository.isPollingForToken) {
            return@runCatching null
        }

        // Check token presence
        val token = gitHubSyncRepository.authPreferences.githubToken.first()
        if (token == null) {
            return@runCatching null
        }

        // Get profile from cache or network
        gitHubSyncRepository.getCurrentUser().getOrThrow().let { user ->
            GitHubProfile(
                login = user.login,
                name = user.name,
                avatarUrl = user.avatarUrl,
                bio = user.bio,
                location = user.location,
                publicRepos = user.publicRepos,
                followers = user.followers,
                following = user.following,
                createdAt = user.createdAt
            )
        }
    }
}
