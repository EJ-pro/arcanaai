package com.example.arcanaai.data.model

// data/model/CatMaster.kt
data class CatMaster(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int,      // 고양이 이미지
    val bgColors: List<androidx.compose.ui.graphics.Color>, // 고양이마다 다른 배경 그라데이션
    val isLocked: Boolean = true // 해금 여부
)