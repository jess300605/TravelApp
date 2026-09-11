package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandTextIcons,
    primaryContainer = BrandDarkPrimary,
    onPrimaryContainer = BrandTextIcons,
    secondary = BrandAccent,
    onSecondary = Color(0xFF1E2F00),
    secondaryContainer = Color(0xFF33691E),
    onSecondaryContainer = Color(0xFFDCEDC8),
    tertiary = BrandLightPrimary,
    onTertiary = BrandPrimaryText,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = BrandDivider,
    error = BrandError,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandTextIcons,
    primaryContainer = BrandLightPrimary,
    onPrimaryContainer = BrandDarkPrimary,
    secondary = BrandAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF2E5100),
    tertiary = BrandDarkPrimary,
    onTertiary = BrandTextIcons,
    background = BrandBackground,
    onBackground = BrandPrimaryText,
    surface = BrandSurface,
    onSurface = BrandPrimaryText,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandSecondaryText,
    outline = BrandDivider,
    outlineVariant = Color(0xFFE0E0E0),
    error = BrandError,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set false to prioritize requested assignment palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

