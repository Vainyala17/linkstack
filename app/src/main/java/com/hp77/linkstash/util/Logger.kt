package com.hp77.linkstash.util

import android.util.Log
import com.hp77.linkstash.BuildConfig

/**
 * Utility class for logging that automatically disables logs in release builds.
 */
object Logger {
    private const val TAG_PREFIX = "LinkStash_"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG_PREFIX + tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG_PREFIX + tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG_PREFIX + tag, message, throwable)
        }
    }
}
