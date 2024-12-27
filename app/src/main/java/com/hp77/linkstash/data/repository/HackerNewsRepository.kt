package com.hp77.linkstash.data.repository

import com.hp77.linkstash.data.preferences.AuthPreferences
import com.hp77.linkstash.data.remote.HNConstants
import com.hp77.linkstash.data.remote.HackerNewsService
import com.hp77.linkstash.data.remote.HackerNewsUser
import com.hp77.linkstash.data.remote.getHNUrl
import com.hp77.linkstash.data.mapper.toEntity
import com.hp77.linkstash.data.mapper.toProfile
import com.hp77.linkstash.data.remote.parseHNUserProfile
import com.hp77.linkstash.util.Logger
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.data.local.dao.HackerNewsProfileDao
import kotlinx.coroutines.flow.first
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HackerNewsRepository @Inject constructor(
    private val hackerNewsService: HackerNewsService,
    private val authPreferences: AuthPreferences,
    private val profileDao: HackerNewsProfileDao
) {
    companion object {
        private const val PROFILE_CACHE_DURATION = 30 * 60 * 1000L // 30 minutes
    }
    suspend fun submitStory(link: Link): Result<String> = runCatching {
        val cookie = authPreferences.hackerNewsToken.first() 
            ?: throw IllegalStateException("Not logged in to HackerNews")

        // First get the submit page to extract the fnid token
        val submitPageResponse = hackerNewsService.getSubmitPage(cookie)
        if (!submitPageResponse.isSuccessful) {
            throw Exception("Failed to get submit page: ${submitPageResponse.errorBody()?.string()}")
        }

        // Parse the HTML to get the fnid
        val submitPageHtml = submitPageResponse.body() ?: throw Exception("Empty submit page response")
        val doc = Jsoup.parse(submitPageHtml)
        val fnidInput = doc.select("input[name=fnid]").first()
        val fnid = fnidInput?.attr("value") ?: throw Exception("Failed to get fnid token")

        // Now submit the story
        val response = hackerNewsService.submitStory(
            cookie = cookie,
            title = link.title ?: "Untitled",
            url = link.url,
            text = link.notes,
            fnid = fnid
        )

        if (!response.isSuccessful) {
            throw Exception("Failed to submit story: ${response.errorBody()?.string()}")
        }

        response.body()?.getHNUrl() ?: throw Exception("No response from HackerNews")
    }

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        val response = hackerNewsService.login(username, password)
        
        if (!response.isSuccessful) {
            throw Exception(HNConstants.ERROR_LOGIN_FAILED)
        }

        // Extract the user cookie from response headers
        val cookies = response.headers()["set-cookie"]
        if (cookies.isNullOrEmpty()) {
            throw Exception(HNConstants.ERROR_LOGIN_FAILED)
        }

        // The cookie format is "user=<token>; Path=/; HttpOnly"
        val userCookie = cookies.split(";").firstOrNull { it.trim().startsWith("user=") }
            ?: throw Exception(HNConstants.ERROR_LOGIN_FAILED)

        authPreferences.updateHackerNewsToken(userCookie)
    }

    suspend fun isAuthenticated(): Boolean {
        return try {
            val cookie = authPreferences.hackerNewsToken.first()
            if (cookie == null) return false

            // Try to get the submit page to verify the cookie is still valid
            val response = hackerNewsService.getSubmitPage(cookie)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUser(): Result<HackerNewsUser> = runCatching {
        val cookie = authPreferences.hackerNewsToken.first()
            ?: throw IllegalStateException("Not logged in to HackerNews")

        // Extract username from cookie
        // Cookie format is "user=<username>; Path=/; HttpOnly"
        val username = cookie.split(";").firstOrNull { it.trim().startsWith("user=") }
            ?.substringAfter("user=")
            ?: throw IllegalStateException("Invalid HackerNews cookie")

        // Check if we have a fresh cached profile
        val cachedProfile = profileDao.getProfileIfFresh(
            username = username,
            maxAge = PROFILE_CACHE_DURATION
        )

        if (cachedProfile != null) {
            Logger.d("HackerNewsRepository", "Using cached HackerNews profile")
            return@runCatching HackerNewsUser(
                username = cachedProfile.username,
                karma = cachedProfile.karma,
                about = cachedProfile.about,
                created = cachedProfile.createdAt
            )
        }

        // Fetch from network
        Logger.d("HackerNewsRepository", "Fetching HackerNews profile from network")
        val response = hackerNewsService.getUser(username)
        if (!response.isSuccessful) {
            throw Exception("Failed to get user profile: ${response.errorBody()?.string()}")
        }

        val user = response.body()?.parseHNUserProfile()
            ?: throw Exception("Empty response from HackerNews")

        // Update cache
        profileDao.insertProfile(user.toEntity())
        Logger.d("HackerNewsRepository", "Updated HackerNews profile cache")

        user
    }

    suspend fun logout() {
        authPreferences.updateHackerNewsToken(null)
        // Clear cached profile data
        val username = authPreferences.hackerNewsToken.first()
            ?.split(";")?.firstOrNull { it.trim().startsWith("user=") }
            ?.substringAfter("user=")
        if (username != null) {
            profileDao.deleteProfile(username)
        }
    }

    suspend fun getStory(id: String): Result<String> = runCatching {
        val response = hackerNewsService.getItem(id)
        
        if (!response.isSuccessful) {
            throw Exception(HNConstants.ERROR_FETCH_FAILED)
        }

        response.body()?.getHNUrl() ?: throw Exception("Story not found")
    }
}
