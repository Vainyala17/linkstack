package com.hp77.linkstash.presentation.components

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.hp77.linkstash.util.CrashReporter
import com.hp77.linkstash.util.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ErrorBoundary(
    content: @Composable () -> Unit
) {
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val errorHandler = remember {
        CoroutineExceptionHandler { _, throwable ->
            Logger.e("ErrorBoundary", "Caught UI error", throwable)
            error = throwable.message
            handleError(throwable, coroutineScope)
        }
    }

    CompositionLocalProvider(
        LocalCoroutineExceptionHandler provides errorHandler
    ) {
        content()
    }

    if (error != null) {
        // You could show a fallback UI here if needed
        LaunchedEffect(error) {
            error = null // Reset after handling
        }
    }

    content()
}

private fun handleError(throwable: Throwable, scope: CoroutineScope) {
    scope.launch {
        val errorDescription = """
            UI Error Detected
            Type: ${throwable.javaClass.simpleName}
            Message: ${throwable.message}
            
            This might be related to the drawer rendering issues.
            Please check the system logs for:
            - PQ Session errors
            - AAL Engine errors
            - Graphics pipeline issues
            
            Stack trace:
            ${throwable.stackTraceToString()}
        """.trimIndent()

        CrashReporter.reportIssue(errorDescription)
    }
}
