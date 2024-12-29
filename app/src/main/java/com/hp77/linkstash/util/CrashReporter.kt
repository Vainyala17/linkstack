package com.hp77.linkstash.util

import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashReporter : Thread.UncaughtExceptionHandler {
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var contextRef: WeakReference<Context>? = null
    private const val DEVELOPER_EMAIL = "hp77codeforgood@gmail.com"
    private val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.getDefault())
    private const val ANOMALY_THRESHOLD = 5000L // 5 seconds

    fun initialize(context: Context) {
        contextRef = WeakReference(context)
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        
        // Observe UI anomalies
        UIAnomalyDetector.anomalyDetected.value?.let { anomaly ->
            if (anomaly.duration > ANOMALY_THRESHOLD) {
                reportAnomaly(anomaly)
            }
        }
    }

    private fun reportAnomaly(anomaly: UIAnomalyDetector.UIAnomaly) {
        val report = generateAnomalyReport(anomaly)
        sendCrashReport(report)
    }

    private fun generateAnomalyReport(anomaly: UIAnomalyDetector.UIAnomaly): String {
        return """
            |UI ANOMALY REPORT
            |================
            |Time: ${dateFormat.format(Date())}
            |
            |ANOMALY DETAILS
            |==============
            |Component: ${anomaly.component}
            |Description: ${anomaly.description}
            |Duration: ${anomaly.duration}ms
            |Expected Duration: ${anomaly.expectedDuration}ms
            |
            |DEVICE INFORMATION
            |=================
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            |App Version: ${contextRef?.get()?.let { ctx ->
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
            } ?: "Unknown"}
        """.trimMargin()
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val report = generateCrashReport(throwable)
            sendCrashReport(report)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        defaultHandler?.uncaughtException(thread, throwable)
    }

    fun reportIssue(description: String) {
        val report = generateManualReport(description)
        sendCrashReport(report)
    }

    private fun generateCrashReport(throwable: Throwable): String {
        val stackTrace = StringWriter()
        throwable.printStackTrace(PrintWriter(stackTrace))

        return """
            |CRASH REPORT
            |===========
            |Time: ${dateFormat.format(Date())}
            |
            |DEVICE INFORMATION
            |=================
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            |App Version: ${contextRef?.get()?.let { ctx ->
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
            } ?: "Unknown"}
            |
            |STACK TRACE
            |===========
            |$stackTrace
        """.trimMargin()
    }

    private fun generateManualReport(description: String): String {
        return """
            |ISSUE REPORT
            |============
            |Time: ${dateFormat.format(Date())}
            |
            |DEVICE INFORMATION
            |=================
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            |App Version: ${contextRef?.get()?.let { ctx ->
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
            } ?: "Unknown"}
            |
            |DESCRIPTION
            |===========
            |$description
        """.trimMargin()
    }

    private fun sendCrashReport(report: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "LinkStash - Crash Report")
            putExtra(Intent.EXTRA_TEXT, report)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        contextRef?.get()?.startActivity(intent)
    }
}
