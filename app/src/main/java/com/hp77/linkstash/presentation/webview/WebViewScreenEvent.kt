package com.hp77.linkstash.presentation.webview

sealed class WebViewScreenEvent {
    data class OnScrollPositionChanged(val position: Float) : WebViewScreenEvent()
    object OnErrorDismiss : WebViewScreenEvent()
}
