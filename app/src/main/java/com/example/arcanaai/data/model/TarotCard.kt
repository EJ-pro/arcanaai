package com.example.arcanaai.data.model

data class TarotCard(
    val id: Int,
    val name: String,
    val keyword: String,
    val imageResId: Int, // R.drawable.xxx
    val description: String // 카드 기본 설명
)