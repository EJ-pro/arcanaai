package com.example.arcanaai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Sanctuary : BottomNavItem("sanctuary", "성전", Icons.Default.Home)
    object Grimoire : BottomNavItem("grimoire", "마도서", Icons.Default.List)
    object Gallery : BottomNavItem("gallery", "도감", Icons.Default.Star)
    object Altar : BottomNavItem("altar", "제단", Icons.Default.Settings)
}
