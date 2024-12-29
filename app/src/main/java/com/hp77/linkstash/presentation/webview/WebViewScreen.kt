package com.hp77.linkstash.presentation.webview

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    onBackPressed: () -> Unit,
    viewModel: WebViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var scrollProgress by remember { mutableStateOf(state.link?.scrollPosition ?: 0f) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(WebViewScreenEvent.OnErrorDismiss)
        }
    }
    
    // Effect to save scroll position when leaving
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(WebViewScreenEvent.OnScrollPositionChanged(scrollProgress))
        }
    }

    if (state.link == null && !state.isLoading) {
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = state.link?.title ?: "LinkStash") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // WebView
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        // Restore scroll position after page loads
                                        if (state.link?.scrollPosition ?: 0f > 0) {
                                            val contentHeight = view?.contentHeight ?: 0
                                            val scrollY = (contentHeight * (state.link?.scrollPosition ?: 0f)).toInt()
                                            view?.scrollTo(0, scrollY)
                                        }
                                    }
                                }
                                
                                // Set up scroll change listener
                                setOnScrollChangeListener { v, _, scrollY, _, _ ->
                                    val webView = v as WebView
                                    val contentHeight = webView.contentHeight * webView.scale
                                    val maxScroll = contentHeight - webView.height
                                    if (maxScroll > 0) {
                                        val newProgress = (scrollY.toFloat() / maxScroll).coerceIn(0f, 1f)
                                        scrollProgress = newProgress
                                        Logger.d("WebViewScreen", "Scroll progress: $newProgress (scrollY=$scrollY, maxScroll=$maxScroll)")
                                    }
                                }
                                
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    setSupportZoom(true)
                                    userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }
                                loadUrl(state.link?.url ?: return@apply)
                            }
                        }
                    )

                    // Scroll progress indicator
                    LinearProgressIndicator(
                        progress = scrollProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
