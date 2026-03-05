package com.example.arcanaai.data.repository

import android.content.Context
import android.util.Log
import com.example.arcanaai.BuildConfig
import com.example.arcanaai.data.model.TarotCard
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TarotRepository(private val context: Context) {

    // 💡 모델 명칭은 반드시 소문자와 하이픈(-) 조합이어야 한다냥!
    // 'gemini-1.5-flash' 또는 'gemini-pro'를 추천한다냥.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text("너는 신비롭고 영험한 고양이 타로 마스터 '아르카나'다냥. 항상 말끝에 '~다냥', '~해봐냥' 같은 고양이 말투를 사용해야 한다냥. 사용자의 고민을 따뜻하게 들어주고 공감해줘냥.") }
    )

    suspend fun getAiChatResponse(history: List<Pair<String, Boolean>>, userMessage: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "YOUR_API_KEY") {
            return@withContext "집사야, local.properties에 GEMINI_API_KEY를 먼저 넣어줘야 한다냥! 🐾"
        }

        try {
            val chat = generativeModel.startChat(
                history = history.map { (text, isUser) ->
                    content(if (isUser) "user" else "model") { text(text) }
                }
            )
            val response = chat.sendMessage(userMessage)
            response.text ?: "미안하다냥, 우주의 기운이 잠시 끊겼다냥. 다시 말해줘냥!"
        } catch (e: Exception) {
            Log.e("Gemini", "Chat Error: ${e.message}", e)
            "우주의 기운이 어지럽다냥! (오류: ${e.localizedMessage}). 모델 명칭이나 API 키를 확인해봐냥!"
        }
    }

    suspend fun getDeepInterpretation(userGoal: String, selectedCards: List<TarotCard>): String = withContext(Dispatchers.IO) {
        try {
            val cardsInfo = selectedCards.joinToString("\n") { "- ${it.name}: ${it.keyword} (${it.description})" }
            val prompt = """
                [사용자의 고민]
                $userGoal
                
                [선택된 카드들]
                $cardsInfo
                
                이 상황에 대해 고양이 타로 술사로서 아주 상세하고 희망찬 해석을 해줘냥. 
                각 카드가 의미하는 바를 이 고민과 연결해서 설명하고, 마지막에는 '집사를 위한 마법의 한마디'를 꼭 해줘냥!
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            response.text ?: "카드가 너무 신비로워 해석이 안 된다냥..."
        } catch (e: Exception) {
            Log.e("Gemini", "Interpretation Error: ${e.message}", e)
            "고양이가 지금 낮잠 시간인가보다냥... (오류: ${e.localizedMessage})"
        }
    }

    fun getAllCards(): List<TarotCard> {
        val cards = mutableListOf<TarotCard>()
        val majorNames = listOf(
            "광대", "마법사", "고위 여사제", "황후", "황제", "교황", "연인", "전차", "힘", "은둔자", 
            "운명의 수레바퀴", "정의", "매달린 사람", "죽음", "절제", "악마", "탑", "별", "달", "태양", "심판", "세계"
        )
        
        majorNames.forEachIndexed { i, name ->
            cards.add(TarotCard(
                id = i, 
                name = name, 
                imageRes = getResourceId("card_${String.format("%02d", i)}"), 
                keyword = "키워드 $i", 
                description = "설명 $i"
            ))
        }
        return cards
    }

    private fun getResourceId(resourceName: String): Int {
        val id = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        return if (id != 0) id else 0
    }
}
