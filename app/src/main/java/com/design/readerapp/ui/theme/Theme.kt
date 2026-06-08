package com.design.readerapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzureBlue,
    onPrimary = Color.White,
    secondary = AzureBlue,
    onSecondary = Color.White,
    tertiary = AzureBlue,
    background = AzureBackground,
    surface = AzureSurface,
    outline = AzureBorder,
    onBackground = AzureText,
    onSurface = AzureText,
    onSurfaceVariant = AzureTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = AzureBlue,
    onPrimary = Color.White,
    secondary = AzureBlue,
    onSecondary = Color.White,
    tertiary = AzureBlue,
    background = AzureLightBackground,
    surface = AzureLightSurface,
    outline = AzureLightBorder,
    onBackground = AzureLightText,
    onSurface = AzureLightText,
    onSurfaceVariant = AzureLightTextSecondary
)

@Composable
fun ReaderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistency with the web brand
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
