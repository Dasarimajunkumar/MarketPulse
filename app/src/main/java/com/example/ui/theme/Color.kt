package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Composition local indicating whether current active theme is dark
val LocalThemeIsDark = compositionLocalOf { true }

// -------------------------------------------------------------
// Terminal Dark Palette Constants
// -------------------------------------------------------------
val DarkNavyBg = Color(0xFF0A0E17)
val DarkSlateNavySurface = Color(0xFF121824)
val DarkCardSurface = Color(0xFF1A2234)
val DarkCardBorder = Color(0xFF26334D)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)
val DarkDividerLine = Color(0xFF1E293B)

// -------------------------------------------------------------
// Crisp Daylight Light Palette Constants
// -------------------------------------------------------------
val LightNavyBg = Color(0xFFF1F5F9) // Slate-100 soft background
val LightSlateNavySurface = Color(0xFFFFFFFF) // Surface white
val LightCardSurface = Color(0xFFFFFFFF) // White cards
val LightCardBorder = Color(0xFFE2E8F0) // Slate-200 subtle border
val LightTextPrimary = Color(0xFF0F172A) // Slate-900 high contrast
val LightTextSecondary = Color(0xFF475569) // Slate-600 readable secondary
val LightTextMuted = Color(0xFF64748B) // Slate-500 muted text
val LightDividerLine = Color(0xFFE2E8F0) // Slate-200 divider

// -------------------------------------------------------------
// Brand & Semantic Accents (Consistent across Light & Dark)
// -------------------------------------------------------------
val CyanTeal = Color(0xFF00D2B4)
val CyanTealLight = Color(0xFF5EEAD4)
val CyanTealDark = Color(0xFF0F766E)

val GainEmerald = Color(0xFF10B981)
val GainEmeraldBg = Color(0xFF064E3B)
val LossCrimson = Color(0xFFEF4444)
val LossCrimsonBg = Color(0xFF7F1D1D)
val RegulatoryAmber = Color(0xFFF59E0B)
val RegulatoryAmberBg = Color(0xFF78350F)
val SecondaryBlue = Color(0xFF38BDF8)

// -------------------------------------------------------------
// Dynamic Theme Adapters
// Seamless drop-in replacements for all composables
// -------------------------------------------------------------
val DeepNavyBg: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkNavyBg else LightNavyBg

val SlateNavySurface: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkSlateNavySurface else LightSlateNavySurface

val CardSurface: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkCardSurface else LightCardSurface

val CardBorder: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkCardBorder else LightCardBorder

val TextPrimary: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkTextPrimary else LightTextPrimary

val TextSecondary: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkTextSecondary else LightTextSecondary

val TextMuted: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkTextMuted else LightTextMuted

val DividerLine: Color
  @Composable
  get() = if (LocalThemeIsDark.current) DarkDividerLine else LightDividerLine
