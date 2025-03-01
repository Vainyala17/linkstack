package com.hp77.linkstash.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddLink : Screen("add_link")
    object EditLink : Screen("edit_link/{linkId}") {
        fun createRoute(linkId: String): String {
            return "edit_link/$linkId"
        }
    }
    object Search : Screen("search")
    object WebView : Screen("webview/{linkId}") {
        fun createRoute(linkId: String): String {
            return "webview/$linkId"
        }
    }
    object Settings : Screen("settings")
    object About : Screen("about")
}
