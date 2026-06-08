package com.design.readerapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── Paleta cálida — coincide con la web (main.css) ──────────────────────────

// Light mode
val WarmBackground     = Color(0xFFF5F0E8)   // --bg
val WarmSurface        = Color(0xFFFDFAF5)   // --surface
val WarmSurfaceSoft    = Color(0xFFEDE8DC)   // --surface-soft
val WarmSurfaceWarm    = Color(0xFFE8E0D0)   // --surface-warm
val WarmText           = Color(0xFF1C1714)   // --text
val WarmTextSoft       = Color(0xFF8C7B6B)   // --text-soft / --muted
val WarmBorder         = Color(0xFFD9CFC0)   // --border
val WarmPrimary        = Color(0xFF7C6A52)   // --primary / --accent
val WarmPrimaryHover   = Color(0xFF5E5040)   // --primary-hover
val WarmAccentDark     = Color(0xFF3D2B1F)   // --accent-dark (button fill)
val WarmSuccess        = Color(0xFF5C8C6B)   // --success
val WarmDanger         = Color(0xFFC0624E)   // --danger
val WarmDangerHover    = Color(0xFFA0503E)   // --danger-hover

// Dark mode
val WarmDarkBackground = Color(0xFF1A1512)   // --bg dark
val WarmDarkSurface    = Color(0xFF231E1A)   // --surface dark
val WarmDarkSurfaceSoft= Color(0xFF2C2520)   // --surface-soft dark
val WarmDarkSurfaceWarm= Color(0xFF332C27)   // --surface-warm dark
val WarmDarkText       = Color(0xFFEDE8DC)   // --text dark
val WarmDarkTextSoft   = Color(0xFF9C8C7C)   // --text-soft dark
val WarmDarkBorder     = Color(0xFF3D3530)   // --border dark
val WarmDarkPrimary    = Color(0xFFC4A882)   // lighter warm for dark mode actions
val WarmDarkAccent     = Color(0xFF3D2B1F)   // accent-dark (same in both modes)

// ── Aliases para compatibilidad con pantallas que usan nombres Azure ─────────
// Apuntan a la nueva paleta cálida para no tener que tocar todas las pantallas
val AzureBlue          = WarmPrimary         // antes azul, ahora marrón cálido
val AzureBackground    = WarmDarkBackground
val AzureSurface       = WarmDarkSurface
val AzureBorder        = WarmDarkBorder
val AzureText          = WarmDarkText
val AzureTextSecondary = WarmDarkTextSoft
val AzureDark          = WarmDarkBackground
