package com.example.mkat_nur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppTheme = staticCompositionLocalOf { AppTheme.SUKUNET }
val LocalAppThemeColors = staticCompositionLocalOf { AppTheme.SUKUNET.lightColors }

@Composable
fun MîkatıNurTheme(
    appTheme: AppTheme = AppTheme.SUKUNET,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeColors = appTheme.getActiveColors(isDark)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = themeColors.primary,
            secondary = themeColors.secondary,
            tertiary = themeColors.accent,
            background = themeColors.background,
            surface = themeColors.surface,
            onBackground = themeColors.textPrimary,
            onSurface = themeColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = themeColors.primary,
            secondary = themeColors.secondary,
            tertiary = themeColors.accent,
            background = themeColors.background,
            surface = themeColors.surface,
            onBackground = themeColors.textPrimary,
            onSurface = themeColors.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalAppThemeColors provides themeColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
