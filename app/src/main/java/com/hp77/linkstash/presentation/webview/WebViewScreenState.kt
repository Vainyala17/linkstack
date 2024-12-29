package com.hp77.linkstash.presentation.webview

import com.hp77.linkstash.domain.model.Link

data class WebViewScreenState(
    val link: Link? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
