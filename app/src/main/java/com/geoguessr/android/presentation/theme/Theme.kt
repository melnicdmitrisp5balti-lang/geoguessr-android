package com.geoguessr.android.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GeoDarkColorScheme = darkColorScheme(
    primary = GeoGreen,
    onPrimary = GeoOnPrimary,
    primaryContainer = GeoGreenDark,
    onPrimaryContainer = GeoGreenLight,
    secondary = GeoBlue,
    onSecondary = GeoOnSurface,
    secondaryContainer = GeoBlueDark,
    onSecondaryContainer = GeoBlueLight,
    background = GeoBackground,
    onBackground = GeoOnBackground,
    surface = GeoSurface,
    onSurface = GeoOnSurface,
    surfaceVariant = GeoSurfaceVariant,
    error = GeoError,
    errorContainer = GeoErrorContainer,
    onError = GeoOnSurface,
    onErrorContainer = GeoOnSurface
)

@Composable
fun GeoGuessrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GeoDarkColorScheme,
        typography = GeoTypography,
        content = content
    )
}
