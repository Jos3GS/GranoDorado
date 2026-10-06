package com.itm.gestordeturnos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CafeDarkBrown = Color(0xFF2C1E16)
val CafeBackgroundLight = Color(0xFFF7F4EF)
val CafeAccentYellow = Color(0xFFD4A373)
val CafeTextPrimary = Color(0xFF1E1E1E)
val CafeTextSecondary = Color(0xFF757575)
val CafeBorderColor = Color(0xFFE0E0E0)

private val LightColorScheme = lightColorScheme(
    primary = CafeDarkBrown,
    secondary = CafeAccentYellow,
    background = CafeBackgroundLight,
    surface = CafeBackgroundLight,
    onPrimary = Color.White,
    onBackground = CafeTextPrimary,
)

@Composable
fun CafeAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}