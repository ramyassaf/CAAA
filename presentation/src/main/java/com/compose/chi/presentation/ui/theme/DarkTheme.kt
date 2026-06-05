package com.compose.chi.presentation.ui.theme

import androidx.compose.runtime.compositionLocalOf

data class DarkThemeController(
    val isDark: Boolean,
    val toggle: () -> Unit,
)

val LocalDarkTheme = compositionLocalOf<DarkThemeController> {
    error("LocalDarkTheme not provided")
}
