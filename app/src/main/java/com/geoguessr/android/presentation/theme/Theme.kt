package com.geoguessr.android.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GeoDarkColorScheme = darkColorScheme(
    primary = GeoCyan,
    onPrimary = GeoBgPrimary,
    primaryContainer = GeoBgTertiary,
    onPrimaryContainer = GeoCyan,
    secondary = GeoGreen,
    onSecondary = GeoBgPrimary,
    secondaryContainer = GeoBgSecondary,
    onSecondaryContainer = GeoGreen,
    background = GeoBgPrimary,
    onBackground = GeoTextPrimary,
    surface = GeoBgSecondary,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoBgTertiary,
    onSurfaceVariant = GeoTextSecondary,
    error = GeoError,
    errorContainer = GeoErrorContainer,
    onError = GeoTextPrimary,
    onErrorContainer = GeoTextPrimary
)

@Composable
fun GeoGuessrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GeoDarkColorScheme,
        typography = GeoTypography,
        content = content
    )
}
