package com.hp77.linkstash.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface GitHubDeviceFlowService {
    @Headers("Accept: application/json")
    @POST("https://github.com/login/device/code")
    suspend fun requestDeviceCode(
        @Query("client_id") clientId: String,
        @Query("scope") scope: String = "repo" // For private repos access
    ): Response<DeviceCodeResponse>

    @Headers("Accept: application/json")
    @POST("https://github.com/login/oauth/access_token")
    suspend fun pollForToken(
        @Query("client_id") clientId: String,
        @Query("device_code") deviceCode: String,
        @Query("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): Response<AccessTokenResponse>
}

data class DeviceCodeResponse(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_uri") val verificationUri: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("interval") val interval: Int
)

data class AccessTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("scope") val scope: String?,
    @SerializedName("error") val error: String?
)
