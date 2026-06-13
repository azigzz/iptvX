package com.iptvx.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IptvColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF27D6A5),
    onPrimary = Color(0xFF08120F),
    background = Color(0xFF080D12),
    onBackground = Color(0xFFEFFAF5),
    surface = Color(0xFF101821),
    onSurface = Color(0xFFEFFAF5),
    surfaceVariant = Color(0xFF172230),
    onSurfaceVariant = Color(0xFFC9D6E2),
    outline = Color(0xFF314154),
    error = Color(0xFFFF746E)
)

@Composable
fun IptvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IptvColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
