package com.hp77.linkstash.data.remote

import retrofit2.Response
import retrofit2.http.*

interface HackerNewsService {
    @GET("user/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<String>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("acct") username: String,
        @Field("pw") password: String,
        @Field("goto") goto: String = "news"
    ): Response<String>

    @GET("submit")
    suspend fun getSubmitPage(
        @Header("Cookie") cookie: String
    ): Response<String>

    @FormUrlEncoded
    @POST("r")
    suspend fun submitStory(
        @Header("Cookie") cookie: String,
        @Field("title") title: String,
        @Field("url") url: String,
        @Field("text") text: String?,
        @Field("fnid") fnid: String,
        @Field("fnop") fnop: String = "submit-page"
    ): Response<String>

    @GET("item")
    suspend fun getItem(
        @Query("id") itemId: String
    ): Response<String>
}

object HNConstants {
    const val API_URL = "https://hacker-news.firebaseio.com/v0/"
    const val WEB_URL = "https://news.ycombinator.com/"
    const val ERROR_LOGIN_FAILED = "Failed to login to HackerNews"
    const val ERROR_FETCH_FAILED = "Failed to fetch from HackerNews"
}

fun String.getHNUrl(): String {
    // Extract the item ID from the response HTML
    val itemIdRegex = """item\?id=(\d+)""".toRegex()
    val match = itemIdRegex.find(this)
    val itemId = match?.groupValues?.get(1)
        ?: throw IllegalStateException("Could not find item ID in response")
    
    return "${HNConstants.WEB_URL}item?id=$itemId"
}
