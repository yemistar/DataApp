// package com.example.data_collect.ui
//
// import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.compose.material3.ColorScheme
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.darkColorScheme
// import androidx.compose.material3.lightColorScheme
// import androidx.compose.runtime.Composable
// import androidx.compose.ui.graphics.Color
//
// private val LightColors: ColorScheme = lightColorScheme(
//     primary = Color(0xFF1B5E20),
//     onPrimary = Color.White,
//     primaryContainer = Color(0xFFA5D6A7),
//     onPrimaryContainer = Color(0xFF00210B),
//     secondary = Color(0xFFFFC107),
//     onSecondary = Color(0xFF1F1300),
//     secondaryContainer = Color(0xFFFFECB3),
//     onSecondaryContainer = Color(0xFF2E2000)
// )
//
// private val DarkColors: ColorScheme = darkColorScheme(
//     primary = Color(0xFF81C784),
//     onPrimary = Color(0xFF00390E),
//     primaryContainer = Color(0xFF2E7D32),
//     onPrimaryContainer = Color(0xFFA5D6A7),
//     secondary = Color(0xFFFFD54F),
//     onSecondary = Color(0xFF2E2000),
//     secondaryContainer = Color(0xFF5D4300),
//     onSecondaryContainer = Color(0xFFFFECB3)
// )
//
// @Composable
// fun FarmTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//     val colors = if (useDarkTheme) DarkColors else LightColors
//     MaterialTheme(
//         colorScheme = colors,
//         typography = MaterialTheme.typography,
//         shapes = MaterialTheme.shapes,
//         content = content
//     )
// }
