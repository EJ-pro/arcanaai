package com.example.arcanaai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // 🏛️ 성전: 그리스 로마 신화 신전 느낌냥!
    object Sanctuary : BottomNavItem("sanctuary", "성전", Icons.Default.AccountBalance)
    
    // 📖 마도서: 기록을 담은 책냥!
    object Grimoire : BottomNavItem("grimoire", "마도서", Icons.Default.MenuBook)
    
    // 📚 도감: 카드들이 꽂힌 책장냥!
    object Gallery : BottomNavItem("gallery", "도감", Icons.Default.LibraryBooks)
    
    // ✨ 제단: 수정구슬과 마법의 빛냥!
    object Altar : BottomNavItem("altar", "제단", Icons.Default.AutoAwesome)
}
