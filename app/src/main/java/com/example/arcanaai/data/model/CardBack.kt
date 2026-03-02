package com.example.arcanaai.data.model

data class CardBack(
    val id: String,
    val name: String,
    val imageRes: Int,
    val grade: CardBackGrade = CardBackGrade.NORMAL,
    val isOwned: Boolean = false
)

enum class CardBackGrade {
    NORMAL, RARE, MYSTIC, LEGENDARY
}
