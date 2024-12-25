package com.hp77.linkstash.data.remote

import com.hp77.linkstash.domain.model.Link
import retrofit2.Response
import retrofit2.http.*

interface GitHubService {
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<GitHubUser>

    @GET("user/repos")
    suspend fun getRepositories(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 100
    ): Response<List<GitHubRepo>>

    @POST("user/repos")
    suspend fun createRepository(
        @Header("Authorization") token: String,
        @Body repo: CreateRepoRequest
    ): Response<GitHubRepo>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String
    ): Response<GitHubContent>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun updateFile(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: UpdateFileRequest
    ): Response<GitHubUpdateResponse>
}

data class GitHubUser(
    val login: String,
    val id: Long,
    val name: String?
)

data class GitHubRepo(
    val id: Long,
    val name: String,
    val full_name: String,
    val private: Boolean,
    val description: String?
)

data class GitHubContent(
    val type: String,
    val encoding: String?,
    val size: Long,
    val name: String,
    val path: String,
    val content: String?,
    val sha: String
)

data class CreateRepoRequest(
    val name: String,
    val description: String?,
    val private: Boolean = true,
    val auto_init: Boolean = true
)

data class UpdateFileRequest(
    val message: String,
    val content: String, // Base64 encoded content
    val sha: String? = null // Required for updating existing files
)

data class GitHubUpdateResponse(
    val content: GitHubContent,
    val commit: GitHubCommit
)

data class GitHubCommit(
    val sha: String,
    val message: String
)

// Extension function to convert Link to markdown format
fun List<Link>.toMarkdown(): String {
    return buildString {
        appendLine("# LinkStash Backup")
        appendLine()
        appendLine("## Links")
        appendLine()
        
        this@toMarkdown.forEach { link ->
            appendLine("### ${link.title}")
            appendLine("- URL: ${link.url}")
            appendLine("- Type: ${link.type}")
            appendLine("- Added: ${link.createdAt}")
            appendLine("- Status: ${if (link.isCompleted) "READ" else "UNREAD"}")
            if (link.tags.isNotEmpty()) {
                appendLine("- Tags: ${link.tags.joinToString(", ") { it.name }}")
            }
            if (!link.notes.isNullOrBlank()) {
                appendLine("- Notes: ${link.notes}")
            }
            appendLine()
        }
    }
}
