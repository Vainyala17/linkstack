package com.hp77.linkstash.util

import android.util.Log

object SystemLogMonitor {
    private const val TAG = "SystemLogMonitor"
    private val graphicsErrors = listOf(
        "PQSessionManager",
        "PQ Session",
        "AAL Engine",
        "DpEngine_AAL"
    )

    private var lastCheckTime = 0L
    private var lastErrorTime = 0L
    private val ERROR_THRESHOLD = 2000L // 2 seconds

    fun checkForGraphicsErrors(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Don't check too frequently
        if (currentTime - lastCheckTime < 1000) {
            return false
        }
        lastCheckTime = currentTime

        return try {
            // Clear old logs first
            Runtime.getRuntime().exec("logcat -c")
            
            // Get fresh logs
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = process.inputStream.bufferedReader()
            val logs = bufferedReader.readText()
            
            // Check for graphics errors in recent logs
            val hasGraphicsErrors = graphicsErrors.any { pattern ->
                logs.contains(pattern, ignoreCase = true)
            }
            
            if (hasGraphicsErrors) {
                // Only report error if we haven't reported one recently
                if (currentTime - lastErrorTime > ERROR_THRESHOLD) {
                    Log.e(TAG, "Graphics pipeline errors detected in system logs")
                    lastErrorTime = currentTime
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            // If we can't access logs, don't report an error
            Log.w(TAG, "Unable to check system logs", e)
            false
        } finally {
            try {
                // Clean up logs to prevent memory issues
                Runtime.getRuntime().exec("logcat -c")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear logs", e)
            }
        }
    }
}
