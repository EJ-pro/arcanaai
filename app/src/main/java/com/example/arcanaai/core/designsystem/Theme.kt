package com.example.arcanaai.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcanaColorScheme = darkColorScheme(
    primary = Gold,
    secondary = MysticPurple,
    tertiary = LightGold,
    background = DeepBlack,
    surface = DarkGray,
    onPrimary = DeepBlack,
    onSecondary = Color.White,
    onBackground = LightGold,
    onSurface = LightGold,
    primaryContainer = DeepPurple,
    onPrimaryContainer = Color.White,
    secondaryContainer = DarkGray,
    onSecondaryContainer = Gold
)

@Composable
fun ArcanaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcanaColorScheme,
        typography = Typography,
        content = content
    )
}
