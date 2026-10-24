package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VyroElectricPurple,
    onPrimary = Color.White,
    secondary = VyroNeonCyan,
    onSecondary = Color.Black,
    tertiary = VyroAccentPink,
    onTertiary = Color.White,
    background = VyroDeepCharcoal,
    onBackground = VyroTextWhite,
    surface = VyroSurfaceDark,
    onSurface = VyroTextWhite,
    surfaceVariant = VyroSurfaceVariantDark,
    onSurfaceVariant = VyroTextMuted,
    outline = VyroOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = VyroElectricPurple,
    onPrimary = Color.White,
    secondary = VyroNeonCyan,
    onSecondary = Color.Black,
    tertiary = VyroAccentPink,
    onTertiary = Color.White,
    background = VyroLightBackground,
    onBackground = VyroTextDark,
    surface = VyroLightSurface,
    onSurface = VyroTextDark,
    surfaceVariant = VyroLightSurfaceVariant,
    onSurfaceVariant = VyroTextSubtle,
    outline = VyroLightOutline
)

@Composable
fun VYROTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VYROTheme(darkTheme = darkTheme, content = content)
}
