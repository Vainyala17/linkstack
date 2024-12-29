package com.hp77.linkstash.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineExceptionHandler

val LocalCoroutineExceptionHandler = staticCompositionLocalOf<CoroutineExceptionHandler> {
    error("No CoroutineExceptionHandler provided")
}
