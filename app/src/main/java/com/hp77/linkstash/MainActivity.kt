package com.hp77.linkstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hp77.linkstash.presentation.addlink.AddLinkScreen
import com.hp77.linkstash.presentation.home.HomeScreen
import com.hp77.linkstash.presentation.navigation.Screen
import com.hp77.linkstash.ui.theme.LinkStashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinkStashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToAddLink = {
                                    navController.navigate(Screen.AddLink.route)
                                },
                                onNavigateToLink = { linkId ->
                                    navController.navigate(Screen.LinkDetail.createRoute(linkId))
                                }
                            )
                        }
                        composable(Screen.AddLink.route) {
                            AddLinkScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            route = Screen.LinkDetail.route,
                            arguments = listOf(
                                navArgument("linkId") {
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            // TODO: Implement LinkDetailScreen
                        }
                    }
                }
            }
        }
    }
}
