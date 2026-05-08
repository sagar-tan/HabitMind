package com.habitmind.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary - Accent color (Electric Indigo)
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = AccentContainer,
    onPrimaryContainer = TextPrimary,
    
    // Secondary - variant for depth
    secondary = AccentVariant,
    onSecondary = OnAccent,
    secondaryContainer = AccentContainer,
    onSecondaryContainer = TextPrimary,
    
    // Background - Charcoal
    background = DarkBackground,
    onBackground = TextPrimary,
    
    // Surface
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    // Error - Premium Coral
    error = Error,
    onError = DarkBackground,
    
    // Outline
    outline = CardBorder,
    outlineVariant = CardBorder
)

@Composable
fun HabitMindTheme(
    darkTheme: Boolean = true, // Dark mode default per spec
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
