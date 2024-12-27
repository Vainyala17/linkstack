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
        try {
            Logger.d(TAG, "Making device code request...")
            val startTime = System.currentTimeMillis()
            val response = deviceFlowService.requestDeviceCode(
                clientId = GitHubConfig.CLIENT_ID,
                scope = "repo"
            )
            val endTime = System.currentTimeMillis()
            Logger.d(TAG, "Device code request took ${endTime - startTime}ms")
            
            if (!response.isSuccessful) {
                val error = handleErrorResponse(response, "Failed to get device code")
                Logger.e(TAG, error)
                throw Exception(error)
            }

            val body = response.body()
            if (body != null) {
                // Log parsed response
                Logger.d(TAG, """Device code response:
                    |Device code: ${body.deviceCode}
                    |User code: ${body.userCode}
                    |Verification URL: ${body.verificationUrl}
                    |Expires in: ${body.expiresIn} seconds
                    |Polling interval: ${body.interval} seconds""".trimMargin())
                body
            } else {
                // If body is null, try to parse from error body
                val errorBody = response.errorBody()?.string()
                Logger.d(TAG, "Raw device code response: $errorBody")
                
                if (errorBody != null) {
                    try {
                        val adapter = moshi.adapter(DeviceCodeResponse::class.java)
                        val fallbackBody = adapter.fromJson(errorBody)
                        if (fallbackBody != null) {
                            Logger.d(TAG, "Successfully parsed response from error body")
                            fallbackBody
                        } else {
                            throw Exception("Failed to parse error body")
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "Failed to parse error body", e)
                        throw e
                    }
                } else {
                    throw Exception("Failed to get device code response")
                }
            }
        } catch (e: Exception) {
            when (e) {
                is java.net.UnknownHostException -> throw Exception("No internet connection. Please check your network and try again.")
                is java.net.SocketTimeoutException -> throw Exception("Request timed out. GitHub servers might be slow, please try again.")
                else -> throw e
            }
        }
    }

    suspend fun pollForToken(deviceCode: String, interval: Int): Result<String> = runCatching {
        Logger.d(TAG, "Starting to poll for token")
        var token: String? = null
        val startTime = System.currentTimeMillis()
        val timeout = 10 * 60 * 1000L // 10 minutes timeout
        
        while (token == null) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw Exception("Device flow authentication timed out after 10 minutes. Please try again.")
            }
            
            try {
                val requestStartTime = System.currentTimeMillis()
                val response = deviceFlowService.pollForToken(
                    clientId = GitHubConfig.CLIENT_ID,
                    deviceCode = deviceCode
                )
                val requestEndTime = System.currentTimeMillis()
                Logger.d(TAG, "Token polling request took ${requestEndTime - requestStartTime}ms")

                // Log progress towards timeout
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = (timeout - elapsedTime) / 1000 // Convert to seconds
                Logger.d(TAG, "Device flow time remaining: ${remainingTime}s")

                when {
                    response.isSuccessful -> {
                        val tokenResponse = response.body()
                        if (tokenResponse == null) {
                            Logger.e(TAG, "Received successful response but body is null")
                            val errorBody = response.errorBody()?.string()
                            Logger.d(TAG, "Error body: $errorBody")
                            throw Exception("Failed to parse token response")
                        }
                        Logger.d(TAG, """Token response:
                            |Access token: ${if (tokenResponse.accessToken != null) "Present" else "Null"}
                            |Token type: ${tokenResponse.tokenType}
                            |Scope: ${tokenResponse.scope}
                            |Error: ${tokenResponse.error}""".trimMargin())

                        if (tokenResponse.accessToken != null) {
                            Logger.d(TAG, "Successfully received access token")
                            authPreferences.updateGitHubToken(tokenResponse.accessToken)
                            token = tokenResponse.accessToken
                            break
                        }

                        // No access token, check error
                        when (tokenResponse.error) {
                            "authorization_pending" -> {
                                Logger.d(TAG, "Authorization pending, waiting ${interval}s")
                                delay(interval * 1000L)
                            }
                            "slow_down" -> {
                                Logger.d(TAG, "Polling too fast, increasing interval")
                                delay((interval + 5) * 1000L)
                            }
                            "expired_token" -> {
                                val error = "Device code has expired. Please try again."
                                Logger.e(TAG, error)
                                throw Exception(error)
                            }
                            null -> {
                                val error = "Unexpected response: no access token or error"
                                Logger.e(TAG, error)
                                throw Exception(error)
                            }
                            else -> {
                                val error = "Token polling failed: ${tokenResponse.error}"
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
            } catch (e: Exception) {
                when (e) {
                    is java.net.UnknownHostException -> throw Exception("No internet connection. Please check your network and try again.")
                    is java.net.SocketTimeoutException -> throw Exception("Request timed out. GitHub servers might be slow, please try again.")
                    else -> throw e
                }
            }
        }
        token ?: throw Exception("Token should not be null at this point")
    }

    suspend fun syncLinks(links: List<Link>): Result<Unit> = runCatching {
        Logger.d(TAG, "Starting sync for ${links.size} links")
        try {
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
                    Logger.d(TAG, "Found existing repository: ${it.fullName}")
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
                    Logger.d(TAG, "Created new repository: ${it.fullName}")
                }
            }

            // First, let's get all markdown files from the repo
            Logger.d(TAG, "Listing repository contents")
            val contentsResponse = gitHubService.listDirectoryContents(
                authHeader,
                repoOwner,
                repoName,
                ""  // Empty path to list root directory
            )

            if (contentsResponse.isSuccessful) {
                withContext(Dispatchers.IO) {
                    val contents = contentsResponse.body() ?: emptyList()
                    
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

            // Generate filename with date
            val filename = getBackupFilename()
            Logger.d(TAG, "Using filename: $filename")

            // Try to get existing file to get its SHA and content
            Logger.d(TAG, "Checking for existing file: $filename")
            val fileResponse = gitHubService.getFileContent(
                authHeader,
                repoOwner,
                repoName,
                filename
            )

            // Get existing links if file exists
            val existingLinks = if (fileResponse.isSuccessful) {
                Logger.d(TAG, "Found existing file, reading content")
                fileResponse.body()?.content?.let { base64Content ->
                    val decodedContent = String(Base64.decode(base64Content, Base64.DEFAULT))
                    MarkdownParser.parseMarkdown(decodedContent)
                } ?: emptyList()
            } else {
                Logger.d(TAG, "No existing file for today, will create new one")
                emptyList()
            }

            // Merge existing and new links, removing duplicates by URL
            val mergedLinks = (existingLinks + links).distinctBy { it.url }
            Logger.d(TAG, "Merged links: ${mergedLinks.size} (${existingLinks.size} existing + ${links.size} new)")

            // Convert merged links to markdown and encode in base64
            Logger.d(TAG, "Converting ${mergedLinks.size} links to markdown")
            val markdown = MarkdownUtils.run { mergedLinks.toMarkdown() }
            val content = Base64.encodeToString(markdown.toByteArray(), Base64.NO_WRAP)

            // Update or create file
            val sha = if (fileResponse.isSuccessful) fileResponse.body()?.sha else null
            Logger.d(TAG, "Updating file with ${if (sha != null) "existing SHA" else "no SHA (new file)"}")
            val updateResponse = gitHubService.updateFile(
                authHeader,
                repoOwner,
                repoName,
                filename,
                UpdateFileRequest(
                    message = if (sha != null) "Update links" else "Create backup file for ${filename.substringAfter(FILE_PREFIX).substringBefore('.')}",
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
        } catch (e: Exception) {
            Logger.e(TAG, "Sync failed", e)
            throw e
        }
    }

    suspend fun getCurrentUser(): Result<GitHubUser> = runCatching {
        val token = authPreferences.githubToken.first() ?: throw IllegalStateException("GitHub token not found")
        val response = gitHubService.getCurrentUser("token $token")
        if (!response.isSuccessful) {
            throw Exception(handleErrorResponse(response, "Failed to get user info"))
        }
        response.body() ?: throw Exception("Empty response body")
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
