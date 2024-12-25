package com.hp77.linkstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hp77.linkstash.presentation.home.HomeScreen
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
                    HomeScreen(
                        onNavigateToAddLink = {
                            // TODO: Implement navigation to add link screen
                        },
                        onNavigateToLink = { linkId ->
                            // TODO: Implement navigation to link detail screen
                        }
                    )
                }
            }
        }
    }
}
