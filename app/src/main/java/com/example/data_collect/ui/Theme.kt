package com.example.data_collect.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF176C37),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F0DF),
    onPrimaryContainer = Color(0xFF063716),
    secondary = Color(0xFF53643A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EBCF),
    onSecondaryContainer = Color(0xFF141F08),
    tertiary = Color(0xFF2C6472),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD2EEF6),
    onTertiaryContainer = Color(0xFF071F28),
    background = Color(0xFFF7FAF6),
    onBackground = Color(0xFF1A1D19),
    surface = Color.White,
    onSurface = Color(0xFF1A1D19),
    surfaceVariant = Color(0xFFEEF4EC),
    onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF738072),
    outlineVariant = Color(0xFFD2DCCE),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF9ED8A9),
    onPrimary = Color(0xFF00390E),
    primaryContainer = Color(0xFF0B5428),
    onPrimaryContainer = Color(0xFFD9F0DF),
    secondary = Color(0xFFC7D1B2),
    onSecondary = Color(0xFF293516),
    secondaryContainer = Color(0xFF3B4C25),
    onSecondaryContainer = Color(0xFFE3EBCF),
    tertiary = Color(0xFFA6D5E1),
    onTertiary = Color(0xFF003541),
    tertiaryContainer = Color(0xFF124D5B),
    onTertiaryContainer = Color(0xFFD2EEF6),
)

@Composable
fun FarmTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
