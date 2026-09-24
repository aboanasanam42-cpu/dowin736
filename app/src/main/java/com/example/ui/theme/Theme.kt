package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClinicColorScheme = darkColorScheme(
    primary = ClinicTealPrimary,
    onPrimary = Color.Black,
    primaryContainer = ClinicHeaderTeal,
    onPrimaryContainer = Color.White,
    secondary = ClinicCyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = ClinicDarkSurfaceVariant,
    onSecondaryContainer = ClinicTextPrimary,
    tertiary = ClinicButtonBlue,
    onTertiary = Color.White,
    background = ClinicDarkBackground,
    onBackground = ClinicTextPrimary,
    surface = ClinicDarkSurface,
    onSurface = ClinicTextPrimary,
    surfaceVariant = ClinicDarkSurfaceVariant,
    onSurfaceVariant = ClinicTextSecondary,
    outline = ClinicDarkCardBorder,
    error = ClinicError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClinicColorScheme,
        typography = Typography,
        content = content
    )
}
