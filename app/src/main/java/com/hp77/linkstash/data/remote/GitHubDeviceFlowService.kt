package com.hp77.linkstash.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface GitHubDeviceFlowService {
    @Headers(
        "Accept: application/json",
        "Accept: application/vnd.github.device-flow+json"
    )
    @POST("login/device/code")
    suspend fun requestDeviceCode(
        @Query("client_id") clientId: String,
        @Query("scope") scope: String = "repo" // For private repos access
    ): Response<DeviceCodeResponse>

    @Headers(
        "Accept: application/json"
    )
    @POST("login/oauth/access_token")
    suspend fun pollForToken(
        @Query("client_id") clientId: String,
        @Query("device_code") deviceCode: String,
        @Query("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): Response<AccessTokenResponse>
}

@JsonClass(generateAdapter = true)
data class DeviceCodeResponse(
    @Json(name = "device_code") val deviceCode: String,
    @Json(name = "user_code") val userCode: String,
    @Json(name = "verification_uri") val verificationUri: String,
    @Json(name = "verification_url") val verificationUrl: String = "", // Fallback field
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "interval") val interval: Int
) {
    val verificationLink: String
        get() = verificationUri // Always use verification_uri as it's the primary field
}

@JsonClass(generateAdapter = true)
data class AccessTokenResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    @Json(name = "scope") val scope: String?,
    @Json(name = "error") val error: String?
)
