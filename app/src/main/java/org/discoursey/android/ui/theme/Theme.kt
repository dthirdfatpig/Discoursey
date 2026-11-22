package org.discoursey.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.discoursey.android.data.preferences.AppPreferences

private val ModernDarkColorScheme = darkColorScheme(
    primary = ModernCyan,
    onPrimary = Color.White,
    secondary = ModernAccent,
    onSecondary = Color.White,
    background = ModernBackground,
    onBackground = ModernTextPrimary,
    surface = ModernSurface,
    onSurface = ModernTextPrimary,
    surfaceVariant = ModernCardBg,
    onSurfaceVariant = ModernTextSecondary,
    outline = ModernDivider
)

private val ClassicDarkColorScheme = darkColorScheme(
    primary = ClassicHighlight,
    onPrimary = Color.White,
    secondary = ClassicAccentPink,
    onSecondary = Color.White,
    background = ClassicBackground,
    onBackground = ClassicTextPrimary,
    surface = ClassicSurface,
    onSurface = ClassicTextPrimary,
    surfaceVariant = ClassicSurface,
    onSurfaceVariant = ClassicTextSecondary,
    outline = ClassicDivider
)

private val AmoledColorScheme = darkColorScheme(
    primary = ModernCyan,
    onPrimary = Color.White,
    secondary = ModernAccent,
    onSecondary = Color.White,
    background = AmoledBackground,
    onBackground = ModernTextPrimary,
    surface = AmoledSurface,
    onSurface = ModernTextPrimary,
    surfaceVariant = AmoledSurface,
    onSurfaceVariant = ModernTextSecondary,
    outline = AmoledDivider
)

private val LightColorScheme = lightColorScheme(
    primary = ModernCyan,
    onPrimary = Color.White,
    secondary = ModernAccent,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider
)

@Composable
fun DiscourseyTheme(
    themeMode: String = AppPreferences.THEME_MODERN_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppPreferences.THEME_CLASSIC_DARK -> ClassicDarkColorScheme
        AppPreferences.THEME_AMOLED -> AmoledColorScheme
        AppPreferences.THEME_LIGHT -> LightColorScheme
        else -> ModernDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
