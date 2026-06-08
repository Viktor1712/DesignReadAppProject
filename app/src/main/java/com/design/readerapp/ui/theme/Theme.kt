package com.design.readerapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light scheme — paleta cálida de papel, igual que la web ─────────────────
private val LightColorScheme = lightColorScheme(
    primary           = WarmAccentDark,        // --accent-dark: botones primary (#3D2B1F)
    onPrimary         = WarmSurface,           // texto sobre botones primary
    primaryContainer  = WarmSurfaceSoft,       // --primary-light: fondos suaves
    onPrimaryContainer= WarmAccentDark,

    secondary         = WarmPrimary,           // --primary: elementos secundarios (#7C6A52)
    onSecondary       = WarmSurface,
    secondaryContainer= WarmSurfaceWarm,       // --surface-warm
    onSecondaryContainer = WarmText,

    tertiary          = WarmPrimaryHover,      // --primary-hover
    onTertiary        = WarmSurface,

    background        = WarmBackground,        // --bg: #F5F0E8 beige cálido
    onBackground      = WarmText,              // --text: #1C1714

    surface           = WarmSurface,           // --surface: #FDFAF5
    onSurface         = WarmText,
    surfaceVariant    = WarmSurfaceSoft,       // --surface-soft
    onSurfaceVariant  = WarmTextSoft,          // --text-soft

    outline           = WarmBorder,            // --border: #D9CFC0
    outlineVariant    = WarmSurfaceWarm,

    error             = WarmDanger,            // --danger
    onError           = Color.White,
    errorContainer    = Color(0xFFFEE8E4),
    onErrorContainer  = WarmDangerHover,

    inverseSurface    = WarmDarkSurface,
    inverseOnSurface  = WarmDarkText,
    inversePrimary    = WarmDarkPrimary,
    scrim             = Color(0x521C1714)
)

// ── Dark scheme — paleta oscura cálida, igual que darkMode del web ───────────
private val DarkColorScheme = darkColorScheme(
    primary           = WarmDarkPrimary,       // #C4A882 — warm tan claro para dark
    onPrimary         = WarmDarkBackground,
    primaryContainer  = WarmDarkSurfaceSoft,
    onPrimaryContainer= WarmDarkPrimary,

    secondary         = WarmDarkPrimary,
    onSecondary       = WarmDarkBackground,
    secondaryContainer= WarmDarkSurfaceWarm,
    onSecondaryContainer = WarmDarkText,

    tertiary          = WarmPrimary,
    onTertiary        = WarmDarkBackground,

    background        = WarmDarkBackground,    // #1A1512 — marrón muy oscuro
    onBackground      = WarmDarkText,          // #EDE8DC — crema cálido

    surface           = WarmDarkSurface,       // #231E1A
    onSurface         = WarmDarkText,
    surfaceVariant    = WarmDarkSurfaceSoft,   // #2C2520
    onSurfaceVariant  = WarmDarkTextSoft,      // #9C8C7C

    outline           = WarmDarkBorder,        // #3D3530
    outlineVariant    = WarmDarkSurfaceWarm,

    error             = WarmDanger,
    onError           = Color.White,
    errorContainer    = Color(0xFF4A1A14),
    onErrorContainer  = Color(0xFFFFB4A8),

    inverseSurface    = WarmSurfaceSoft,
    inverseOnSurface  = WarmText,
    inversePrimary    = WarmAccentDark,
    scrim             = Color(0x521A1512)
)

@Composable
fun ReaderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}
