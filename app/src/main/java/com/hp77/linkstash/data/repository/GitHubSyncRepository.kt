package com.hp77.linkstash.data.repository

import android.util.Base64
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.data.remote.CreateRepoRequest
import com.hp77.linkstash.data.remote.GitHubService
import com.hp77.linkstash.data.remote.UpdateFileRequest
import com.hp77.linkstash.data.remote.toMarkdown
import com.hp77.linkstash.domain.model.Link
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubSyncRepository @Inject constructor(
    private val gitHubService: GitHubService,
    private val authPreferences: AuthPreferences
) {
    companion object {
        private const val DEFAULT_REPO_NAME = "linkstash-backup"
        private const val REPO_DESCRIPTION = "LinkStash app backup repository"
        private const val LINKS_FILE = "links.md"
        private const val COMMIT_MESSAGE = "Update links"
    }

    suspend fun syncLinks(links: List<Link>): Result<Unit> = runCatching {
        val token = authPreferences.githubToken.first() ?: throw IllegalStateException("GitHub token not found")
        val repoName = authPreferences.githubRepoName.first() ?: DEFAULT_REPO_NAME
        val authHeader = "token $token"

        // Get user info
        val userResponse = gitHubService.getCurrentUser(authHeader)
        if (!userResponse.isSuccessful) {
            throw Exception("Failed to get user info: ${userResponse.errorBody()?.string()}")
        }
        val user = userResponse.body()!!
        
        // Use configured repo owner or fall back to authenticated user
        val repoOwner = authPreferences.githubRepoOwner.first() ?: user.login

        // Check if repo exists or create it
        val reposResponse = gitHubService.getRepositories(authHeader)
        if (!reposResponse.isSuccessful) {
            throw Exception("Failed to get repositories: ${reposResponse.errorBody()?.string()}")
        }

        val repo = reposResponse.body()!!.find { it.name == repoName } ?: run {
            val createRepoResponse = gitHubService.createRepository(
                authHeader,
                CreateRepoRequest(
                    name = repoName,
                    description = REPO_DESCRIPTION
                )
            )
            if (!createRepoResponse.isSuccessful) {
                throw Exception("Failed to create repository: ${createRepoResponse.errorBody()?.string()}")
            }
            createRepoResponse.body()!!
        }

        // Convert links to markdown and encode in base64
        val markdown = links.toMarkdown()
        val content = Base64.encodeToString(markdown.toByteArray(), Base64.NO_WRAP)

        // Try to get existing file to get its SHA
        val fileResponse = gitHubService.getFileContent(
            authHeader,
            repoOwner,
            repoName,
            LINKS_FILE
        )

        // Update or create file
        val updateResponse = gitHubService.updateFile(
            authHeader,
            repoOwner,
            repoName,
            LINKS_FILE,
            UpdateFileRequest(
                message = COMMIT_MESSAGE,
                content = content,
                sha = if (fileResponse.isSuccessful) fileResponse.body()?.sha else null
            )
        )

        if (!updateResponse.isSuccessful) {
            throw Exception("Failed to update file: ${updateResponse.errorBody()?.string()}")
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            val token = authPreferences.githubToken.first()
            if (token == null) {
                false
            } else {
                val response = gitHubService.getCurrentUser("token $token")
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        authPreferences.updateGitHubToken(null)
    }

    suspend fun authenticate(token: String): Result<Unit> = runCatching {
        // Test the token by trying to get user info
        val response = gitHubService.getCurrentUser("token $token")
        if (!response.isSuccessful) {
            throw Exception("Invalid token")
        }
        // If successful, save the token
        authPreferences.updateGitHubToken(token)
    }
}
