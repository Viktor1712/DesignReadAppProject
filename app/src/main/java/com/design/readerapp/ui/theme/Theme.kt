package com.design.readerapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzureBlue,
    onPrimary = AzureText,
    secondary = AzureBlue,
    onSecondary = AzureText,
    tertiary = AzureBlue,
    background = AzureBackground,
    surface = AzureSurface,
    onBackground = AzureText,
    onSurface = AzureText
)

private val LightColorScheme = lightColorScheme(
    primary = AzureBlue,
    onPrimary = AzureText,
    secondary = AzureBlue,
    onSecondary = AzureText,
    tertiary = AzureBlue,
    background = AzureText,
    surface = AzureText,
    onBackground = AzureDark,
    onSurface = AzureDark
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
