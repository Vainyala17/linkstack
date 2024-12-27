package com.hp77.linkstash.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubErrorResponse(
    val message: String,
    val errors: List<GitHubError>? = null,
    @Json(name = "documentation_url")
    val documentationUrl: String? = null
) {
    @JsonClass(generateAdapter = true)
    data class GitHubError(
        val resource: String,
        val code: String,
        val field: String,
        val message: String
    )

    fun toUserFriendlyMessage(): String {
        return errors?.firstOrNull()?.message ?: message
    }
}
