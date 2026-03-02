package com.example.arcanaai.data.repository

import android.content.Context
import com.example.arcanaai.data.model.TarotCard

class TarotRepository(private val context: Context) {

    fun getAllCards(): List<TarotCard> {
        val cards = mutableListOf<TarotCard>()

        // 1. 메이저 아르카나 데이터 (0~21)
        val majorData = listOf(
            Pair("광대", "시작, 모험 / 새로운 여정이 시작되려 한다냥. 두려워 말고 발걸음을 내딛어봐냥."),
            Pair("마법사", "창조, 능력 / 너에겐 이미 모든 준비가 되어있다냥. 네 능력을 믿고 시작해보라냥."),
            Pair("고위 여사제", "직관, 지혜 / 내면의 목소리에 귀를 기울여라냥. 정답은 이미 네 마음속에 있다냥."),
            Pair("황후", "풍요, 모성 / 따뜻한 기운이 너를 감싸고 있다냥. 결실을 맺을 시기가 다가온다냥."),
            Pair("황제", "권위, 질서 / 규칙과 계획이 필요한 때다냥. 중심을 잘 잡고 나아가라냥."),
            Pair("교황", "전통, 교육 / 누군가의 조언이나 가르침이 큰 힘이 될 거다냥. 주변을 둘러보라냥."),
            Pair("연인", "선택, 사랑 / 마음이 이끄는 대로 결정해라냥. 소중한 인연이 기다리고 있다냥."),
            Pair("전차", "승리, 전진 / 강한 의지로 밀어붙여라냥. 포기하지 않으면 결국 승리할 거다냥."),
            Pair("힘", "힘, 인내 / 부드러움이 강함을 이긴다냥. 인내심을 갖고 상황을 다스려봐냥."),
            Pair("은둔자", "탐구, 고독 / 잠시 멈춰서 자신을 돌아보는 시간이 필요하다냥. 깊이 생각해보라냥."),
            Pair("운명의 수레바퀴", "운명, 변화 / 운명의 수레바퀴가 돌고 있다냥. 변화를 기꺼이 받아들여라냥."),
            Pair("정의", "정의, 균형 / 공정하게 판단해야 할 때다냥. 원인과 결과는 명확하다냥."),
            Pair("매달린 사람", "희생, 정지 / 다른 각도에서 세상을 바라봐냥. 기다림 또한 성장의 과정이다냥."),
            Pair("죽음", "종료, 새로운 시작 / 낡은 것을 보내줘야 새로운 것이 온다냥. 끝은 곧 시작이다냥."),
            Pair("절제", "절제, 조화 / 서로 다른 것들을 잘 섞어야 한다냥. 중용의 미덕을 발휘해봐냥."),
            Pair("악마", "구속, 중독 / 유혹에 빠지지 않게 조심해라냥. 마음의 짐을 내려놓을 때다냥."),
            Pair("탑", "붕괴, 급변 / 갑작스러운 변화에 놀라지 마라냥. 무너진 자리에서 다시 세우면 된다냥."),
            Pair("별", "희망, 영감 / 어둠 속에서도 빛나는 별이 있다냥. 꿈을 향해 계속 나아가라냥."),
            Pair("달", "불안, 무의식 / 안개 속을 걷는 기분일 수 있지만, 직관을 믿고 조심스레 나아가냥."),
            Pair("태양", "성공, 행복 / 밝은 태양이 너를 비춘다냥! 모든 일이 잘 풀리고 기쁨이 가득할 거다냥."),
            Pair("심판", "부활, 결단 / 이제 결정을 내려야 할 시간이다냥. 과거를 딛고 일어서라냥."),
            Pair("세계", "완성, 통합 / 하나의 주기가 완성되었다냥. 충분히 자격이 있으니 즐기라냥.")
        )

        majorData.forEachIndexed { i, data ->
            val (keyword, desc) = data.second.split(" / ")
            cards.add(TarotCard(i, data.first, getResourceId("card_${String.format("%02d", i)}"), keyword, desc))
        }

        // 2. 마이너 아르카나 데이터 (22~77)
        val suits = listOf(
            Triple("지팡이", "열정, 행동", "에너지가 넘치는 시기다냥!"),
            Triple("컵", "감정, 관계", "마음의 소리에 귀 기울여야 한다냥."),
            Triple("검", "지성, 판단", "냉철한 이성이 필요한 때다냥."),
            Triple("펜타클", "물질, 결실", "실질적인 성과가 눈에 보일 거다냥.")
        )
        
        var currentId = 22
        suits.forEach { suit ->
            for (num in 1..14) {
                val rankName = when(num) {
                    1 -> "에이스"
                    11 -> "페이지"
                    12 -> "나이트"
                    13 -> "퀸"
                    14 -> "킹"
                    else -> num.toString()
                }
                
                val specificDesc = when(num) {
                    1 -> "새로운 ${suit.first}의 기운이 솟아난다냥!"
                    in 2..10 -> "${suit.first}의 기운이 점차 쌓여가는 과정이다냥."
                    else -> "${suit.first}의 정점에 도달한 인물을 상징한다냥."
                }

                cards.add(
                    TarotCard(
                        id = currentId,
                        name = "${suit.first} $rankName",
                        imageRes = getResourceId("card_${String.format("%02d", currentId)}"),
                        keyword = suit.second,
                        description = "${suit.third} $specificDesc"
                    )
                )
                currentId++
            }
        }
        return cards
    }

    private fun getResourceId(resourceName: String): Int {
        val id = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (id != 0) id else 0 // 이미지가 없으면 0 반환
    }
}
