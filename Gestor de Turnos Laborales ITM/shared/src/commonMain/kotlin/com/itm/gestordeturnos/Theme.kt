package com.itm.gestordeturnos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary brand colors
val CafeDarkBrown = Color(0xFF2C1E16)
val CafeBackgroundLight = Color(0xFFF7F4EF)
val CafeAccentYellow = Color(0xFFD4A373)
val CafeTextPrimary = Color(0xFF1E1E1E)
val CafeTextSecondary = Color(0xFF757575)
val CafeBorderColor = Color(0xFFE0E0E0)
val CafeBorderLight = Color(0xFFEAEAEA)
val CafeSurfaceWarm = Color(0xFFFAF9F6)

// Status & Badge semantic colors
val CafeStatusSuccessBg = Color(0xFFE6F4EA)
val CafeStatusSuccessText = Color(0xFF1E8E3E)
val CafeStatusErrorBg = Color(0xFFFCE8E6)
val CafeStatusErrorText = Color(0xFFD93025)
val CafeStatusWarningBg = Color(0xFFF3E7C9)
val CafeStatusWarningText = Color(0xFF2C1E16)
val CafeStatusInfoBg = Color(0xFFD3E3FD)
val CafeStatusInfoText = Color(0xFF0B57D0)

// Shift color categories
val ColorApertura = Color(0xFFC48E38)
val ColorIntermedio = Color(0xFF6B7E4B)
val ColorCierre = Color(0xFF4C7B8B)

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
