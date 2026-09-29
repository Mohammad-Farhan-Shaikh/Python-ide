package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PythonCyan,
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF004D67),
    onPrimaryContainer = Color(0xFFBEE9FF),
    secondary = PythonYellow,
    onSecondary = Color(0xFF3B2F00),
    secondaryContainer = Color(0xFF554400),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = RunGreen,
    onTertiary = Color(0xFF003914),
    tertiaryContainer = Color(0xFF005320),
    onTertiaryContainer = Color(0xFF8CF8A2),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = StopRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
