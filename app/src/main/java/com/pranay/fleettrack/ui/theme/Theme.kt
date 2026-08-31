package com.pranay.fleettrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = TextPrimary,
    primaryContainer = Navy600,
    onPrimaryContainer = TextPrimary,
    secondary = Emerald500,
    onSecondary = TextPrimary,
    secondaryContainer = Emerald700,
    onSecondaryContainer = TextPrimary,
    tertiary = Amber500,
    onTertiary = Navy900,
    background = SurfaceDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = Rose500,
    onError = TextPrimary,
    outline = TextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = LightBackground,
    primaryContainer = Blue300,
    onPrimaryContainer = Navy900,
    secondary = Emerald600,
    onSecondary = LightBackground,
    secondaryContainer = Emerald400,
    onSecondaryContainer = Navy900,
    tertiary = Amber600,
    onTertiary = LightBackground,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = Rose600,
    onError = LightBackground,
    outline = LightTextSecondary
)

@Composable
fun FleetTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FleetTrackTypography,
        content = content
    )
}
