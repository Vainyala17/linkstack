package com.hp77.linkstash.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = OnPrimaryDark,
    primaryContainer = BlueDark,
    onPrimaryContainer = BlueContainer,
    
    secondary = Purple80,
    onSecondary = OnSecondaryDark,
    secondaryContainer = PurpleDark,
    onSecondaryContainer = PurpleContainer,
    
    tertiary = Green80,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = GreenDark,
    onTertiaryContainer = GreenContainer,
    
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    error = Error
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = OnPrimaryLight,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueContainerDark,
    
    secondary = Purple40,
    onSecondary = OnSecondaryLight,
    secondaryContainer = PurpleContainer,
    onSecondaryContainer = PurpleContainerDark,
    
    tertiary = Green40,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = GreenContainer,
    onTertiaryContainer = GreenContainerDark,
    
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    error = Error
)

@Composable
fun LinkStashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to use our custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
