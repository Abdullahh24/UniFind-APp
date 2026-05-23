package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = UniBluePrimaryDark,
    secondary = UniBlueSecondaryDark,
    tertiary = UniGoldTertiaryDark,
    background = UniBackgroundDark,
    surface = UniSurfaceDark,
    onPrimary = UniBackgroundDark,
    onSecondary = UniBackgroundDark,
    onTertiary = UniBackgroundDark,
    onBackground = UniTextDarkPrimary,
    onSurface = UniTextDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = UniBluePrimary,
    secondary = UniBlueSecondary,
    tertiary = UniGoldTertiary,
    background = UniBackgroundLight,
    surface = UniSurfaceLight,
    onPrimary = UniSurfaceLight,
    onSecondary = UniSurfaceLight,
    onTertiary = UniSurfaceLight,
    onBackground = UniTextLightPrimary,
    onSurface = UniTextLightPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
