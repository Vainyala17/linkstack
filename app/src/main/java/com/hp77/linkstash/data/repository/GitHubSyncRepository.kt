package com.hp77.linkstash.data.repository

import android.util.Base64
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.data.remote.*
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.mapper.toLinkEntity
import com.hp77.linkstash.util.Logger
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GitHubSyncRepository"

@Singleton
class GitHubSyncRepository @Inject constructor(
    private val gitHubService: GitHubService,
    private val deviceFlowService: GitHubDeviceFlowService,
    private val authPreferences: AuthPreferences,
    private val linkDao: LinkDao,
    private val moshi: Moshi
) {
    companion object {
        private const val DEFAULT_REPO_NAME = "linkstash-backup"
        private const val REPO_DESCRIPTION = "LinkStash app backup repository"
        private const val COMMIT_MESSAGE = "Update links"
        private const val FILE_PREFIX = "linkstash-"
        private val FILE_PATTERN = Pattern.compile("linkstash-\\d{4}-\\d{2}-\\d{2}\\.md")
    }

    private fun handleErrorResponse(response: Response<*>, prefix: String): String {
        val errorBody = response.errorBody()?.string()
        return try {
            val errorResponse = moshi.adapter(GitHubErrorResponse::class.java).fromJson(errorBody ?: "")
            "$prefix: ${errorResponse?.toUserFriendlyMessage() ?: "Unknown error"}"
        } catch (e: Exception) {
            "$prefix: ${errorBody ?: "Unknown error"}"
        }
    }

    private fun getBackupFilename(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "$FILE_PREFIX${dateFormat.format(Date())}.md"
    }

    suspend fun initiateDeviceFlow(): Result<DeviceCodeResponse> = runCatching {
        Logger.d(TAG, "Initiating GitHub Device Flow")
        val response = deviceFlowService.requestDeviceCode(
            clientId = GitHubConfig.CLIENT_ID,
            scope = "repo"
        )
        
        if (!response.isSuccessful) {
            val error = handleErrorResponse(response, "Failed to get device code")
            Logger.e(TAG, error)
            throw Exception(error)
        }
        
        response.body() ?: throw Exception("Empty response body")
    }

    suspend fun pollForToken(deviceCode: String, interval: Int): Result<String> = runCatching {
        Logger.d(TAG, "Starting to poll for token")
        var token: String? = null
        while (token == null) {
            val response = deviceFlowService.pollForToken(
                clientId = GitHubConfig.CLIENT_ID,
                deviceCode = deviceCode
            )

            when {
                response.isSuccessful -> {
                    val tokenResponse = response.body()
                    when {
                        tokenResponse?.accessToken != null -> {
                            Logger.d(TAG, "Successfully received access token")
                            authPreferences.updateGitHubToken(tokenResponse.accessToken)
                            token = tokenResponse.accessToken
                        }
                        tokenResponse?.error == "authorization_pending" -> {
                            Logger.d(TAG, "Authorization pending, waiting ${interval}s")
                            delay(interval * 1000L)
                        }
                        tokenResponse?.error == "slow_down" -> {
                            Logger.d(TAG, "Polling too fast, increasing interval")
                            delay((interval + 5) * 1000L)
                        }
                        else -> {
                            val error = "Token polling failed: ${tokenResponse?.error}"
                            Logger.e(TAG, error)
                            throw Exception(error)
                        }
                    }
                }
                else -> {
                    val error = handleErrorResponse(response, "Failed to poll for token")
                    Logger.e(TAG, error)
                    throw Exception(error)
                }
            }
        }
        token
    }

    suspend fun syncLinks(links: List<Link>): Result<Unit> = runCatching {
        Logger.d(TAG, "Starting sync for ${links.size} links")
        val token = authPreferences.githubToken.first() ?: throw IllegalStateException("GitHub token not found")
        val repoName = authPreferences.githubRepoName.first() ?: DEFAULT_REPO_NAME
        val authHeader = "token $token"

        // Get user info
        Logger.d(TAG, "Getting user info")
        val userResponse = gitHubService.getCurrentUser(authHeader)
        if (!userResponse.isSuccessful) {
            val error = handleErrorResponse(userResponse, "Failed to get user info")
            Logger.e(TAG, error)
            throw Exception(error)
        }
        val user = userResponse.body()!!
        Logger.d(TAG, "Got user info for: ${user.login}")
        
        // Use configured repo owner or fall back to authenticated user
        val repoOwner = authPreferences.githubRepoOwner.first() ?: user.login

        // Check if repo exists
        Logger.d(TAG, "Checking for repository: $repoName")
        val repoResponse = gitHubService.getRepository(authHeader, repoOwner, repoName)
        val repo = if (repoResponse.isSuccessful) {
            repoResponse.body()!!.also {
                Logger.d(TAG, "Found existing repository: ${it.full_name}")
            }
        } else {
            // Repository doesn't exist, create it
            Logger.d(TAG, "Repository not found, creating new one")
            val createRepoResponse = gitHubService.createRepository(
                authHeader,
                CreateRepoRequest(
                    name = repoName,
                    description = REPO_DESCRIPTION
                )
            )
            if (!createRepoResponse.isSuccessful) {
                val error = handleErrorResponse(createRepoResponse, "Failed to create repository")
                Logger.e(TAG, error)
                throw Exception(error)
            }
            createRepoResponse.body()!!.also {
                Logger.d(TAG, "Created new repository: ${it.full_name}")
            }
        }

        // First, let's get all markdown files from the repo
        Logger.d(TAG, "Listing repository contents")
        val contentsResponse = gitHubService.getFileContent(
            authHeader,
            repoOwner,
            repoName,
            ""  // Empty path to list root directory
        )

        if (contentsResponse.isSuccessful) {
            withContext(Dispatchers.IO) {
                val contents: List<GitHubContent> = contentsResponse.body()?.let { content ->
                    if (content.type == "dir") listOf(content) else emptyList()
                } ?: emptyList()
                
                val linkstashFiles = contents.filter { content -> 
                    content.type == "file" && FILE_PATTERN.matcher(content.name).matches() 
                }
                
                Logger.d(TAG, "Found ${linkstashFiles.size} LinkStash backup files")

                // Get all remote links
                val remoteLinks = mutableListOf<Link>()
                linkstashFiles.forEach { file ->
                    Logger.d(TAG, "Reading backup file: ${file.name}")
                    try {
                        val fileResponse = gitHubService.getFileContent(
                            authHeader,
                            repoOwner,
                            repoName,
                            file.path
                        )
                        
                        if (fileResponse.isSuccessful) {
                            fileResponse.body()?.content?.let { content ->
                                val decodedContent = String(Base64.decode(content, Base64.DEFAULT))
                                val links = MarkdownParser.parseMarkdown(decodedContent)
                                Logger.d(TAG, "Parsed ${links.size} links from ${file.name}")
                                remoteLinks.addAll(links)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Error reading file ${file.name}", e)
                    }
                }

                // Get all local URLs
                val localUrls = links.map { it.url }.toSet()
                
                // Find new links that don't exist locally
                val newLinks = remoteLinks.filter { remoteLink -> 
                    remoteLink.url !in localUrls 
                }
                Logger.d(TAG, "Found ${newLinks.size} new links to import")

                // Import new links
                if (newLinks.isNotEmpty()) {
                    Logger.d(TAG, "Importing new links")
                    newLinks.forEach { link ->
                        val entity = link.toLinkEntity()
                        linkDao.insertLink(entity)
                        Logger.d(TAG, "Imported link: ${link.url}")
                    }
                }
            }
        }

        // Convert links to markdown and encode in base64
        Logger.d(TAG, "Converting ${links.size} links to markdown")
        val markdown = MarkdownUtils.run { links.toMarkdown() }
        val content = Base64.encodeToString(markdown.toByteArray(), Base64.NO_WRAP)

        // Generate filename with date
        val filename = getBackupFilename()
        Logger.d(TAG, "Using filename: $filename")

        // Try to get existing file to get its SHA
        Logger.d(TAG, "Checking for existing file: $filename")
        val fileResponse = gitHubService.getFileContent(
            authHeader,
            repoOwner,
            repoName,
            filename
        )

        val sha = if (fileResponse.isSuccessful) {
            Logger.d(TAG, "Found existing file, using its SHA")
            fileResponse.body()?.sha
        } else {
            Logger.d(TAG, "File doesn't exist yet, will create new one")
            null
        }

        // Update or create file
        Logger.d(TAG, "Updating file with ${if (sha != null) "existing SHA" else "no SHA (new file)"}")
        val updateResponse = gitHubService.updateFile(
            authHeader,
            repoOwner,
            repoName,
            filename,
            UpdateFileRequest(
                message = COMMIT_MESSAGE,
                content = content,
                sha = sha
            )
        )

        if (!updateResponse.isSuccessful) {
            val error = handleErrorResponse(updateResponse, "Failed to update file")
            Logger.e(TAG, error)
            // Update sync status for all links
            withContext(Dispatchers.IO) {
                links.forEach { link ->
                    linkDao.updateSyncError(link.id, error)
                }
            }
            throw Exception(error)
        }

        // Update sync status for all links
        Logger.d(TAG, "Updating sync status for ${links.size} links")
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            links.forEach { link ->
                linkDao.updateSyncSuccess(link.id, timestamp)
            }
        }
        Logger.d(TAG, "Sync completed successfully")
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
            Logger.e(TAG, "Authentication check failed", e)
            false
        }
    }

    suspend fun logout() {
        Logger.d(TAG, "Logging out")
        authPreferences.updateGitHubToken(null)
    }
}
