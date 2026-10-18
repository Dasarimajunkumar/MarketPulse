package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
  DARK,
  LIGHT,
  SYSTEM
}

private val TerminalDarkColorScheme = darkColorScheme(
  primary = CyanTeal,
  onPrimary = DarkNavyBg,
  primaryContainer = CyanTealDark,
  onPrimaryContainer = CyanTealLight,
  secondary = SecondaryBlue,
  onSecondary = Color.White,
  background = DarkNavyBg,
  onBackground = DarkTextPrimary,
  surface = DarkSlateNavySurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkCardSurface,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkCardBorder,
  error = LossCrimson,
  onError = Color.White
)

private val TerminalLightColorScheme = lightColorScheme(
  primary = CyanTealDark,
  onPrimary = Color.White,
  primaryContainer = CyanTealLight,
  onPrimaryContainer = DarkNavyBg,
  secondary = SecondaryBlue,
  onSecondary = Color.White,
  background = LightNavyBg,
  onBackground = LightTextPrimary,
  surface = LightSlateNavySurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightCardSurface,
  onSurfaceVariant = LightTextSecondary,
  outline = LightCardBorder,
  error = LossCrimson,
  onError = Color.White
)

@Composable
fun MarketPulseTheme(
  themeMode: ThemeMode = ThemeMode.DARK,
  darkTheme: Boolean = when (themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  },
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) TerminalDarkColorScheme else TerminalLightColorScheme
  CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MarketPulseTheme(darkTheme = darkTheme, content = content)
}
