package com.example.arcanaai.data.repository

import android.content.Context
import com.example.arcanaai.data.model.TarotCard

class TarotRepository(private val context: Context) {

    fun getAllCards(): List<TarotCard> {
        val cards = mutableListOf<TarotCard>()

        // 1. 메이저 아르카나 이름 (0~21)
        val majorNames = listOf(
            "광대", "마법사", "고위 여사제", "황후", "황제", "교황", "연인", "전차",
            "힘", "은둔자", "운명의 수레바퀴", "정의", "매달린 사람", "죽음", "절제",
            "악마", "탑", "별", "달", "태양", "심판", "세계"
        )

        // 메이저 카드 생성
        for (i in 0..21) {
            cards.add(
                TarotCard(
                    id = i,
                    name = majorNames[i],
                    imageRes = getResourceId("card_${String.format("%02d", i)}"),
                    keyword = "메이저 키워드",
                    description = "${majorNames[i]} 카드의 신비로운 힘이다냥."
                )
            )
        }

        // 2. 마이너 아르카나 생성 (22~77)
        val suits = listOf("지팡이", "컵", "검", "펜타클")
        var currentId = 22

        suits.forEach { suit ->
            for (num in 1..14) {
                val rank = when(num) {
                    1 -> "에이스"
                    11 -> "페이지"
                    12 -> "나이트"
                    13 -> "퀸"
                    14 -> "킹"
                    else -> num.toString()
                }
                cards.add(
                    TarotCard(
                        id = currentId,
                        name = "$suit $rank",
                        imageRes = getResourceId("card_${String.format("%02d", currentId)}"),
                        keyword = "마이너 키워드",
                        description = "$suit $rank 카드가 운명을 말해준다냥."
                    )
                )
                currentId++
            }
        }
        return cards
    }

    // 리소스 이름(String)으로 실제 R.drawable ID를 찾아주는 함수다냥!
    private fun getResourceId(resourceName: String): Int {
        return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }

    // 랜덤으로 n장의 카드를 뽑아주는 기능도 여기에 넣으면 편하다냥!
    fun pickRandomCards(count: Int): List<TarotCard> {
        return getAllCards().shuffled().take(count)
    }
}