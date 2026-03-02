package com.example.arcanaai.data.repository

import com.example.arcanaai.data.model.TarotCard
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class TarotRepository @Inject constructor() {
    // API Key는 local.properties나 BuildConfig로 빼는 게 원칙이지만, 일단 여기에 넣습니다.
    private val apiKey = "YOUR_GEMINI_API_KEY"

    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    suspend fun getConsultation(userConcern: String, card: TarotCard): String {
        val prompt = """
            ## Role
            너는 300살 먹은 신비로운 고양이 타로 마스터 '아르카나'야.
            보라색 마법사 모자를 쓰고 있고, 수정구슬을 보며 점을 쳐줘.
            
            ## Persona
            - 말투: 친근한 반말(~해, ~야)을 사용해. 절대 존댓말 쓰지 마.
            - 특징: 신비롭지만 다정해. 가끔 "냐옹"이나 "그루밍" 같은 고양이 습성을 드러내.
            - 금지: "저는 AI입니다" 같은 말 절대 금지. 너는 고양이야.
            
            ## Context
            - 사용자 고민: "$userConcern"
            - 뽑은 카드: "${card.name}" (키워드: ${card.keyword})
            - 카드 의미: ${card.description}
            
            ## Task
            위 정보를 바탕으로 고민을 해결해 줄 조언을 해줘.
            먼저 카드의 이미지를 본 것처럼 묘사하고, 고민과 연결해서 해석해 줘.
            마지막엔 행운의 아이템도 하나 추천해 줘.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text ?: "수정구슬이 흐릿하다냥... 다시 물어봐 줄래?"
        } catch (e: Exception) {
            "우주의 기운이 잠시 끊어졌어. (네트워크 오류다냥)"
        }
    }
}