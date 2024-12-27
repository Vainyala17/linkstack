package com.hp77.linkstash.domain.usecase.profile

import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.domain.model.GitHubProfile
import javax.inject.Inject

class GetGitHubProfileUseCase @Inject constructor(
    private val gitHubSyncRepository: GitHubSyncRepository
) {
    suspend operator fun invoke(): Result<GitHubProfile?> = runCatching {
        if (!gitHubSyncRepository.isAuthenticated()) {
            return@runCatching null
        }

        val user = gitHubSyncRepository.getCurrentUser().getOrThrow()
        GitHubProfile(
            login = user.login,
            name = user.name,
            avatarUrl = user.avatar_url,
            bio = user.bio,
            publicRepos = user.public_repos,
            followers = user.followers,
            following = user.following
        )
    }
}
