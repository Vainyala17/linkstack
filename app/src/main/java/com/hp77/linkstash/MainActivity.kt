package com.hp77.linkstash

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hp77.linkstash.presentation.addlink.AddEditLinkScreen
import com.hp77.linkstash.presentation.home.HomeScreen
import com.hp77.linkstash.presentation.navigation.Screen
import com.hp77.linkstash.presentation.webview.WebViewScreen
import java.net.URLDecoder
import com.hp77.linkstash.ui.theme.LinkStashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            LinkStashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Handle deep link from notification
                    LaunchedEffect(intent?.data) {
                        intent?.data?.toString()?.let { url ->
                            // Navigate to WebView with the URL
                            navController.navigate(Screen.WebView.createRoute(url))
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToAddLink = {
                                    navController.navigate(Screen.AddLink.route)
                                },
                                onNavigateToEdit = { link ->
                                    navController.navigate(Screen.EditLink.createRoute(link.id))
                                }
                            )
                        }
                        composable(Screen.AddLink.route) {
                            AddEditLinkScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = Screen.EditLink.route,
                            arguments = listOf(
                                navArgument("linkId") {
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            AddEditLinkScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = Screen.WebView.route,
                            arguments = listOf(
                                navArgument("url") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val url = URLDecoder.decode(
                                backStackEntry.arguments?.getString("url") ?: "",
                                "UTF-8"
                            )
                            WebViewScreen(
                                url = url,
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
