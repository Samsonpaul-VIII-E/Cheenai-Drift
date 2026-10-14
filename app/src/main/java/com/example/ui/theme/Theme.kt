package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LeadForgeColorScheme = darkColorScheme(
    primary = CyberPrimaryCyan,
    onPrimary = CyberBackground,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberPrimaryCyan,
    secondary = CyberEmerald,
    onSecondary = CyberBackground,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberEmerald,
    tertiary = CyberViolet,
    onTertiary = CyberBackground,
    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorder,
    error = CyberCrimson,
    onError = CyberTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LeadForgeColorScheme,
        typography = Typography,
        content = content
    )
}
