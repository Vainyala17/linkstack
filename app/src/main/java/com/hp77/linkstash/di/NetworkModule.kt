package com.hp77.linkstash.di

import com.hp77.linkstash.data.remote.GitHubDeviceFlowService
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
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import com.hp77.linkstash.util.Logger
import javax.inject.Named
import javax.inject.Singleton

private const val TAG = "NetworkModule"

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
            .addInterceptor { chain ->
                val request = chain.request()
                val startTime = System.currentTimeMillis()
                val response = chain.proceed(request)
                val endTime = System.currentTimeMillis()
                Logger.d(TAG, "Request to ${request.url} completed in ${endTime - startTime}ms with code ${response.code}")
                response
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
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
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(
        @Named("github") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("github-oauth")
    fun provideGitHubOAuthClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request()
                val startTime = System.currentTimeMillis()
                val response = chain.proceed(request)
                val endTime = System.currentTimeMillis()
                Logger.d(TAG, "OAuth request to ${request.url} completed in ${endTime - startTime}ms with code ${response.code}")
                if (request.url.encodedPath.contains("device/code")) {
                    try {
                        val responseBody = response.peekBody(Long.MAX_VALUE).string()
                        Logger.d(TAG, "Device code response: $responseBody")
                    } catch (e: Exception) {
                        Logger.e(TAG, "Failed to read device code response", e)
                    }
                }
                response
            }
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request()
                try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    // Retry once on failure with increased timeout
                    chain.withConnectTimeout(10, TimeUnit.SECONDS)
                        .withReadTimeout(10, TimeUnit.SECONDS)
                        .withWriteTimeout(10, TimeUnit.SECONDS)
                        .proceed(request)
                }
            }
            .dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                    Logger.d(TAG, "DNS lookup for: $hostname")
                    val startTime = System.currentTimeMillis()
                    val addresses = try {
                        java.net.InetAddress.getAllByName(hostname).toMutableList().also { list ->
                            val endTime = System.currentTimeMillis()
                            Logger.d(TAG, "DNS lookup completed in ${endTime - startTime}ms. Found ${list.size} addresses")
                            list.forEach { addr ->
                                Logger.d(TAG, "Address: ${addr.hostAddress}")
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(TAG, "DNS lookup failed for $hostname", e)
                        throw e
                    }
                    addresses.shuffle()
                    return addresses
                }
            })
            .eventListener(object : okhttp3.EventListener() {
                private val timings = mutableMapOf<String, Long>()

                override fun connectStart(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                    val key = "${call.request().url}"
                    timings[key] = System.currentTimeMillis()
                    Logger.d(TAG, "Connection started to: ${inetSocketAddress.hostString}:${inetSocketAddress.port}")
                }

                override fun connectFailed(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy, protocol: okhttp3.Protocol?, exception: java.io.IOException) {
                    val key = "${call.request().url}"
                    val startTime = timings.remove(key)
                    val duration = if (startTime != null) System.currentTimeMillis() - startTime else -1
                    Logger.e(TAG, "Connection failed to: ${inetSocketAddress.hostString}:${inetSocketAddress.port} after ${duration}ms", exception)
                }

                override fun connectEnd(call: okhttp3.Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy, protocol: okhttp3.Protocol?) {
                    val key = "${call.request().url}"
                    val startTime = timings.remove(key)
                    val duration = if (startTime != null) System.currentTimeMillis() - startTime else -1
                    Logger.d(TAG, "Connection established to: ${inetSocketAddress.hostString}:${inetSocketAddress.port} using ${protocol?.name ?: "unknown protocol"} in ${duration}ms")
                }

                override fun secureConnectStart(call: okhttp3.Call) {
                    val key = "${call.request().url}_ssl"
                    timings[key] = System.currentTimeMillis()
                    Logger.d(TAG, "Starting SSL/TLS handshake with: ${call.request().url}")
                }

                override fun secureConnectEnd(call: okhttp3.Call, handshake: okhttp3.Handshake?) {
                    val key = "${call.request().url}_ssl"
                    val startTime = timings.remove(key)
                    val duration = if (startTime != null) System.currentTimeMillis() - startTime else -1
                    Logger.d(TAG, "SSL/TLS handshake completed with: ${call.request().url} in ${duration}ms using ${handshake?.tlsVersion}")
                }

                override fun callStart(call: okhttp3.Call) {
                    val key = "${call.request().url}"
                    timings[key] = System.currentTimeMillis()
                    Logger.d(TAG, "Starting call to: ${call.request().url}")
                }

                override fun callEnd(call: okhttp3.Call) {
                    val key = "${call.request().url}"
                    val startTime = timings.remove(key)
                    val duration = if (startTime != null) System.currentTimeMillis() - startTime else -1
                    Logger.d(TAG, "Completed call to: ${call.request().url} in ${duration}ms")
                }

                override fun requestHeadersEnd(call: okhttp3.Call, request: okhttp3.Request) {
                    Logger.d(TAG, "Request headers sent to: ${request.url}")
                }

                override fun responseHeadersStart(call: okhttp3.Call) {
                    Logger.d(TAG, "Starting to receive response headers from: ${call.request().url}")
                }

                override fun responseHeadersEnd(call: okhttp3.Call, response: okhttp3.Response) {
                    Logger.d(TAG, "Received response headers from: ${response.request.url} with code: ${response.code}")
                }

                override fun responseBodyStart(call: okhttp3.Call) {
                    Logger.d(TAG, "Starting to receive response body from: ${call.request().url}")
                }

                override fun responseBodyEnd(call: okhttp3.Call, byteCount: Long) {
                    Logger.d(TAG, "Received response body from: ${call.request().url} (${byteCount} bytes)")
                }
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("github-oauth")
    fun provideGitHubOAuthRetrofit(
        @Named("github-oauth") okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://github.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
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
    fun provideGitHubDeviceFlowService(@Named("github-oauth") retrofit: Retrofit): GitHubDeviceFlowService {
        return retrofit.create(GitHubDeviceFlowService::class.java)
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
