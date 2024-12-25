package com.hp77.linkstash.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddLink : Screen("add_link")
    object LinkDetail : Screen("link_detail/{linkId}") {
        fun createRoute(linkId: String) = "link_detail/$linkId"
    }
    object WebView : Screen("webview/{url}") {
        fun createRoute(url: String) = "webview/${android.net.Uri.encode(url)}"
    }
}
