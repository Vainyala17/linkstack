package com.hp77.linkstash

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.hp77.linkstash.presentation.search.SearchScreen
import com.hp77.linkstash.presentation.webview.WebViewScreen
import com.hp77.linkstash.presentation.about.AboutScreen
import com.hp77.linkstash.presentation.settings.SettingsScreen
import java.net.URLDecoder
import com.hp77.linkstash.data.preferences.ThemePreferences
import com.hp77.linkstash.data.preferences.ThemeMode
import com.hp77.linkstash.presentation.components.ErrorBoundary
import com.hp77.linkstash.ui.theme.LinkStashTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.hp77.linkstash.util.Logger
import com.hp77.linkstash.util.DeepLinkHandler
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val contentReady = mutableStateOf(false)
    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep the splash screen visible until content is ready
        splashScreen.setKeepOnScreenCondition { !contentReady.value }
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            // Mark content as ready once the theme is loaded
            LaunchedEffect(Unit) {
                contentReady.value = true
            }
            
            LinkStashTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                ErrorBoundary {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                    // Handle deep link from notification
                    LaunchedEffect(intent?.data) {
                        intent?.data?.toString()?.let { url ->
                            // Get or create link and navigate to WebView
                            val linkId = deepLinkHandler.handleDeepLink(url)
                            navController.navigate(Screen.WebView.createRoute(linkId))
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
                                    Logger.d("MainActivity", "Creating edit route for link: id=${link.id}, url=${link.url}")
                                    navController.navigate(Screen.EditLink.createRoute(link.id))
                                },
                                onNavigateToSearch = {
                                    navController.navigate(Screen.Search.route)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onNavigateToWebView = { link ->
                                    navController.navigate(Screen.WebView.createRoute(link.id))
                                },
                                onNavigateToAbout = {
                                    navController.navigate(Screen.About.route)
                                }
                            )
                        }
                        
                        composable(Screen.Search.route) {
                            SearchScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToLink = { link ->
                                    navController.navigate(Screen.WebView.createRoute(link.id))
                                },
                                onEditLink = { link ->
                                    navController.navigate(Screen.EditLink.createRoute(link.id))
                                },
                                onToggleFavorite = { link ->
                                    // Handle in SearchScreen
                                },
                                onToggleArchive = { link ->
                                    // Handle in SearchScreen
                                },
                                onToggleStatus = {
                                    link ->
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
                        ) { backStackEntry ->
                            val linkId = backStackEntry.arguments?.getString("linkId")
                            Logger.d("MainActivity", "EditLink screen received linkId: $linkId")
                            AddEditLinkScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                linkId = linkId
                            )
                        }

                        composable(
                            route = Screen.WebView.route,
                            arguments = listOf(
                                navArgument("linkId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val linkId = backStackEntry.arguments?.getString("linkId")
                                ?: return@composable
                            WebViewScreen(
                                onBackPressed = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToAbout = {
                                    navController.navigate(Screen.About.route)
                                }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen(
                                onNavigateUp = {
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
}
