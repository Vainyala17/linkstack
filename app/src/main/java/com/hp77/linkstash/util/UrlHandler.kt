package com.hp77.linkstash.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.URLUtil

object UrlHandler {
    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val TWITTER_PACKAGE = "com.twitter.android"
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    
    private const val YOUTUBE_URL_PATTERN = "(?:https?://)?(?:www\\.)?youtu(?:\\.be|be\\.com)/(?:.*v(?:/|=)|(?:.*/)?)([\\w'-]+)"
    private const val TWITTER_URL_PATTERN = "(?:https?://)?(?:www\\.)?(?:twitter|x)\\.com/.*"
    private const val INSTAGRAM_URL_PATTERN = "(?:https?://)?(?:www\\.)?instagram\\.com/.*"

    sealed class UrlType {
        data class YouTube(val videoId: String) : UrlType()
        object Twitter : UrlType()
        object Instagram : UrlType()
        object Web : UrlType()
    }

    fun parseUrl(url: String): UrlType {
        if (!URLUtil.isValidUrl(url)) return UrlType.Web

        // Check if it's a YouTube URL
        YOUTUBE_URL_PATTERN.toRegex().find(url)?.let { matchResult ->
            return UrlType.YouTube(matchResult.groupValues[1])
        }

        // Check if it's a Twitter URL
        if (TWITTER_URL_PATTERN.toRegex().matches(url)) {
            return UrlType.Twitter
        }

        // Check if it's an Instagram URL
        if (INSTAGRAM_URL_PATTERN.toRegex().matches(url)) {
            return UrlType.Instagram
        }

        return UrlType.Web
    }

    fun createIntent(context: Context, url: String): Intent {
        return when (val urlType = parseUrl(url)) {
            is UrlType.YouTube -> createYouTubeIntent(context, urlType.videoId, url)
            is UrlType.Twitter -> createAppIntent(context, TWITTER_PACKAGE, url)
            is UrlType.Instagram -> createAppIntent(context, INSTAGRAM_PACKAGE, url)
            is UrlType.Web -> createBrowserIntent(url)
        }
    }

    private fun createYouTubeIntent(context: Context, videoId: String, fallbackUrl: String): Intent {
        // Try to open in YouTube app if installed
        val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))

        return if (isAppInstalled(context, YOUTUBE_PACKAGE)) {
            youtubeIntent.setPackage(YOUTUBE_PACKAGE)
            youtubeIntent
        } else {
            // Fallback to browser if YouTube app is not installed
            createBrowserIntent(fallbackUrl)
        }
    }

    private fun createAppIntent(context: Context, packageName: String, fallbackUrl: String): Intent {
        return if (isAppInstalled(context, packageName)) {
            Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                setPackage(packageName)
            }
        } else {
            createBrowserIntent(fallbackUrl)
        }
    }

    fun createBrowserIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
