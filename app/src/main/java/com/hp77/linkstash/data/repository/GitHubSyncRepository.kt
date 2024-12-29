package com.hp77.linkstash.data.repository

import android.util.Base64
import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.data.remote.*
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.dao.GitHubProfileDao
import com.hp77.linkstash.data.mapper.toLinkEntity
import com.hp77.linkstash.data.mapper.toEntity
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
    internal val authPreferences: AuthPreferences,
    private val linkDao: LinkDao,
    private val profileDao: GitHubProfileDao,
    private val moshi: Moshi
) {
    companion object {
        private const val DEFAULT_REPO_NAME = "linkstash-backup"
        private const val REPO_DESCRIPTION = "LinkStash app backup repository"
        private const val COMMIT_MESSAGE = "Update links"
        private const val FILE_PREFIX = "linkstash-"
        private val FILE_PATTERN = Pattern.compile("linkstash-\\d{4}-\\d{2}-\\d{2}\\.md")
        private const val PROFILE_CACHE_DURATION = 30 * 60 * 1000L // 30 minutes
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


    suspend fun syncLinks(links: List<Link>): Result<Unit> = runCatching {
        Logger.d(TAG, "Starting sync for ${links.size} links")
        try {
            val token = authPreferences.githubToken.first() ?: throw IllegalStateException("GitHub token not found")
            val repoName = authPreferences.githubRepoName.first() ?: DEFAULT_REPO_NAME
            val authHeader = "token $token"

            // Get user info to get the correct owner
            Logger.d(TAG, "Getting user info")
            val userResponse = gitHubService.getCurrentUser(authHeader)
            if (!userResponse.isSuccessful) {
                val error = handleErrorResponse(userResponse, "Failed to get user info")
                Logger.e(TAG, error)
                throw Exception(error)
            }
            val user = userResponse.body()!!
            
            // First try to get repo from configured owner
            var currentRepoOwner = authPreferences.githubRepoOwner.first()
            if (currentRepoOwner != null) {
                Logger.d(TAG, "Checking for repository under configured owner: $currentRepoOwner")
                val repoResponse = gitHubService.getRepository(authHeader, currentRepoOwner, repoName)
                if (repoResponse.isSuccessful) {
                    repoResponse.body()!!.also {
                        Logger.d(TAG, "Found existing repository: ${it.fullName}")
                    }
                } else {
                    // If not found under configured owner, try user's account
                    currentRepoOwner = user.login
                }
            } else {
                currentRepoOwner = user.login
            }

            // If we're using user's account, check/create repo there
            if (currentRepoOwner == user.login) {
                Logger.d(TAG, "Checking for repository under user account: ${user.login}")
                val repoResponse = gitHubService.getRepository(authHeader, user.login, repoName)
                if (!repoResponse.isSuccessful) {
                    // Repository doesn't exist in user's account, create it
                    Logger.d(TAG, "Repository not found, creating new one in ${user.login}'s account")
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
                } else {
                    repoResponse.body()!!.also {
                        Logger.d(TAG, "Found existing repository: ${it.fullName}")
                    }
                }
            }

        // First, let's get all markdown files from the repo and parse all existing links
        Logger.d(TAG, "Listing repository contents")
        val contentsResponse = gitHubService.listDirectoryContents(
            authHeader,
            currentRepoOwner,
            repoName,
            ""  // Empty path to list root directory
        )

        if (!contentsResponse.isSuccessful) {
            val error = handleErrorResponse(contentsResponse, "Failed to list repository contents")
            Logger.e(TAG, error)
            throw Exception(error)
        }

        // Get all existing remote links first
        val remoteLinks = mutableListOf<Link>()
        val contents = contentsResponse.body() ?: emptyList()
        val linkstashFiles = contents.filter { content -> 
            content.type == "file" && FILE_PATTERN.matcher(content.name).matches() 
        }
        
        Logger.d(TAG, "Found ${linkstashFiles.size} LinkStash backup files")

        linkstashFiles.forEach { file ->
            Logger.d(TAG, "Reading backup file: ${file.name}")
            try {
                val fileResponse = gitHubService.getFileContent(
                    authHeader,
                    currentRepoOwner,
                    repoName,
                    file.path
                )
                
                if (fileResponse.isSuccessful) {
                    fileResponse.body()?.content?.let { content ->
                        val decodedContent = String(Base64.decode(content, Base64.DEFAULT))
                        val parsedLinks = MarkdownParser.parseMarkdown(decodedContent)
                        Logger.d(TAG, "Parsed ${parsedLinks.size} links from ${file.name}")
                        remoteLinks.addAll(parsedLinks)
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error reading file ${file.name}", e)
            }
        }

        // Filter out links that already exist in remote
        val (linksToSync, skippedLinks) = links.partition { localLink ->
            val remoteLink = remoteLinks.find { it.url == localLink.url }
            remoteLink == null || !areLinksEffectivelyEqual(localLink, remoteLink)
        }

        Logger.d(TAG, "Found ${linksToSync.size} links to sync and ${skippedLinks.size} duplicate links")

        // Update sync status for skipped links
        withContext(Dispatchers.IO) {
            skippedLinks.forEach { link ->
                Logger.d(TAG, "Skipping duplicate link: ${link.url}")
                linkDao.updateSyncError(link.id, "Link already exists in GitHub")
            }
        }

        if (linksToSync.isEmpty()) {
            Logger.d(TAG, "No new links to sync")
            return@runCatching
        }

        // Generate filename with date
        val filename = getBackupFilename()
        Logger.d(TAG, "Using filename: $filename")

        // Convert links to sync to markdown and encode in base64
        Logger.d(TAG, "Converting ${linksToSync.size} links to markdown")
        val markdown = MarkdownUtils.run { linksToSync.toMarkdown() }
        val content = Base64.encodeToString(markdown.toByteArray(), Base64.NO_WRAP)

        // Check if file exists and get its SHA
        Logger.d(TAG, "Checking if file exists: $filename")
        val fileResponse = gitHubService.getFileContent(
            authHeader,
            currentRepoOwner,
            repoName,
            filename
        )

        // Get SHA if file exists
        val sha = if (fileResponse.isSuccessful) {
            fileResponse.body()?.sha
        } else {
            null
        }

        // Create or update file
        Logger.d(TAG, if (sha != null) "Updating existing file" else "Creating new file")
        val updateResponse = gitHubService.updateFile(
            authHeader,
            currentRepoOwner,
            repoName,
            filename,
            UpdateFileRequest(
                message = if (sha != null) "Update links" else "Add new links",
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

        // For device flow, we don't need to fetch profile
        if (isPollingForToken) {
            throw IllegalStateException("GitHub token not found")
        }

        // Try to get profile from cache first
        val cachedProfile = profileDao.getProfileIfFresh(
            login = "github", // Use a constant key since we only store one profile at a time
            maxAge = PROFILE_CACHE_DURATION
        )

        if (cachedProfile != null) {
            Logger.d(TAG, "Using cached GitHub profile")
            return@runCatching GitHubUser(
                login = cachedProfile.login,
                id = 0, // Using 0 as a placeholder since we don't store id in cache
                name = cachedProfile.name,
                avatarUrl = cachedProfile.avatarUrl,
                bio = cachedProfile.bio,
                location = cachedProfile.location,
                publicRepos = cachedProfile.publicRepos,
                followers = cachedProfile.followers,
                following = cachedProfile.following,
                createdAt = cachedProfile.createdAt
            )
        }

        // If no valid cache, fetch from network
        Logger.d(TAG, "No valid cache found, fetching from network")
        val response = gitHubService.getCurrentUser("token $token")
        if (!response.isSuccessful) {
            throw Exception(handleErrorResponse(response, "Failed to get user info"))
        }
        
        val user = response.body() ?: throw Exception("Empty response body")
        
        // Update cache with the constant key and timestamp
        profileDao.insertProfile(user.toEntity().copy(
            login = "github",
            lastFetchedAt = System.currentTimeMillis()
        ))
        Logger.d(TAG, "Updated GitHub profile cache")
        
        user
    }

    var isPollingForToken = false
        private set

    suspend fun pollForToken(deviceCode: String, interval: Int): Result<String> = runCatching {
        isPollingForToken = true
        try {
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
        } finally {
            isPollingForToken = false
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            // During device flow, just check token presence
            if (isPollingForToken) {
                return authPreferences.githubToken.first() != null
            }
            
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
        // Clear cached profile
        profileDao.deleteProfile("github")
    }

    /**
     * Compares two links to determine if they are effectively equal by comparing relevant fields.
     * This helps prevent duplicate imports of links that only differ in non-essential ways.
     *
     * @param link1 First link to compare
     * @param link2 Second link to compare
     * @return true if the links are effectively equal, false if they have meaningful differences
     */
    private fun areLinksEffectivelyEqual(link1: Link, link2: Link): Boolean {
        // URL is already checked before this function is called
        return link1.title == link2.title &&
               link1.description == link2.description &&
               link1.type == link2.type &&
               link1.notes == link2.notes &&
               link1.isArchived == link2.isArchived &&
               link1.isFavorite == link2.isFavorite &&
               link1.isCompleted == link2.isCompleted
    }
}
