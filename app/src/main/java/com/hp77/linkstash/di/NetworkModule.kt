package com.hp77.linkstash.di

import com.hp77.linkstash.data.remote.GitHubService
import com.hp77.linkstash.data.remote.HNConstants
import com.hp77.linkstash.data.remote.HackerNewsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("hackernews")
    fun provideHackerNewsOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(@Named("github") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("hackernews-api")
    fun provideHackerNewsApiRetrofit(@Named("hackernews") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HNConstants.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("hackernews-web")
    fun provideHackerNewsWebRetrofit(@Named("hackernews") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HNConstants.WEB_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // For HTML responses
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubService(@Named("github") retrofit: Retrofit): GitHubService {
        return retrofit.create(GitHubService::class.java)
    }

    @Provides
    @Singleton
    fun provideHackerNewsService(
        @Named("hackernews-api") apiRetrofit: Retrofit,
        @Named("hackernews-web") webRetrofit: Retrofit
    ): HackerNewsService {
        // Create a composite service that uses both retrofits
        return object : HackerNewsService {
            private val apiService = apiRetrofit.create(HackerNewsService::class.java)
            private val webService = webRetrofit.create(HackerNewsService::class.java)

            override suspend fun getUser(userId: String) = apiService.getUser(userId)
            override suspend fun login(username: String, password: String, goto: String) = webService.login(username, password, goto)
            override suspend fun submitStory(cookie: String, title: String, url: String, text: String?, fnid: String, fnop: String) = 
                webService.submitStory(cookie, title, url, text, fnid, fnop)
            override suspend fun getItem(itemId: String) = apiService.getItem(itemId)
            override suspend fun getSubmitPage(cookie: String) = webService.getSubmitPage(cookie)
        }
    }
}
