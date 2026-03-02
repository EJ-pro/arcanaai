package com.example.arcanaai.data.model

data class TarotCard(
    val id: Int,             // 0 ~ 77 (순서)
    val name: String,       // 카드 이름 (영문/한글)
    val imageRes: Int,      // R.drawable.card_00
    val keyword: String,    // 핵심 키워드
    val description: String  // 기본적인 의미
)