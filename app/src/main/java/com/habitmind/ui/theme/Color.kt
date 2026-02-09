package com.habitmind.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Monochrome theme - sophisticated grayscale palette
 * With subtle blue-tinted grays for depth
 */

// Near-black background (not pure black per spec)
val DarkBackground = Color(0xFF0A0A0C)
val DarkSurface = Color(0xFF141416)
val DarkSurfaceVariant = Color(0xFF1E1E22)

// Elevated surfaces for glassmorphism
val GlassSurface = Color(0xFF1C1C20)
val GlassOverlay = Color(0x20FFFFFF)
val GlassBorder = Color(0x15FFFFFF)

// Text colors - monochrome hierarchy
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB8B8BC)
val TextMuted = Color(0xFF707078)
val TextSubtle = Color(0xFF505058)

// Legacy compatibility
val DarkOnBackground = TextPrimary
val DarkOnSurface = TextPrimary
val DarkOnSurfaceVariant = TextSecondary

// Single accent - subtle warm white/silver for focus states
val Accent = Color(0xFFE8E8EC)
val AccentVariant = Color(0xFFD0D0D8)
val AccentContainer = Color(0xFF28282E)
val OnAccent = Color(0xFF0A0A0C)

// Semantic colors - muted, not aggressive
val Success = Color(0xFF9CA3AF)  // Soft gray-green
val Warning = Color(0xFFA8A0A0)  // Warm gray
val Error = Color(0xFFA8A8A8)    // Neutral gray

// Card and container colors
val CardBackground = Color(0xFF121214)
val CardBorder = Color(0xFF252528)

// Bottom navigation - glassmorphism
val NavBarBackground = Color(0xCC141416)  // 80% opacity
val NavBarBorder = Color(0x20FFFFFF)

// Muted text
val MutedForeground = Color(0xFF6B6B70)
val Placeholder = Color(0xFF454550)

// Progress indicators
val ProgressTrack = Color(0xFF252528)
val ProgressActive = Color(0xFFD8D8DC)
