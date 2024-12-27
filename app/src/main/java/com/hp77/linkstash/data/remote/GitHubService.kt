package com.hp77.linkstash.data.remote

import retrofit2.Response
import retrofit2.http.*

interface GitHubService {
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<GitHubUser>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepo>

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
