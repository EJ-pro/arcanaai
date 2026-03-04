package com.example.arcanaai.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 🎨 디자이너님의 요청에 따른 타이포그래피 '자간+행간' 정밀 수선냥!
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp, // 👈 행간을 24 -> 20으로 줄여서 쫀득하게냥!
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp, // 👈 제목 행간도 28 -> 26으로 압축냥!
        letterSpacing = (-0.5).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp, // 👈 작은 글씨 행간도 16 -> 14로 세련되게냥!
        letterSpacing = 0.sp
    )
)
