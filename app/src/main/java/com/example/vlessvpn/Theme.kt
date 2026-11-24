package com.example.vlessvpn

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Oxyvo {
    val Green = Color(0xFF2EE67A)
    val GreenDeep = Color(0xFF00843B)
    val Warn = Color(0xFFFFC857)
    val Bad = Color(0xFFFF6B6B)
    val Muted = Color(0xFF7FA38D)
}

private val Scheme = darkColorScheme(
    primary = Color(0xFF2EE67A),
    onPrimary = Color(0xFF00210E),
    primaryContainer = Color(0xFF14432A),
    onPrimaryContainer = Color(0xFFB6F5CE),
    secondary = Color(0xFF7ED9A4),
    onSecondary = Color(0xFF00210E),
    background = Color(0xFF07120C),
    onBackground = Color(0xFFE3F3E8),
    surface = Color(0xFF0C1A12),
    onSurface = Color(0xFFE3F3E8),
    surfaceVariant = Color(0xFF13261A),
    onSurfaceVariant = Color(0xFFA9C7B4),
    outline = Color(0xFF3C5A47),
    error = Color(0xFFFF6B6B),
)

@Composable
fun OxyvoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
