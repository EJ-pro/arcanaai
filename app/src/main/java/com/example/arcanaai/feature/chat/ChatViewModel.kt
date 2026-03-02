// feature/chat/ChatViewModel.kt
package com.example.arcanaai.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.arcanaai.BuildConfig

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // 1. Gemini AI 설정 (자신의 API KEY를 넣으세요)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = "AIzaSyBapWtQK-tjn3V224ro0SNRe6z2hPYI2y4", // 👈 보관한 키를 자동으로 가져옵니다.
        systemInstruction = content {
            text("""
            너는 신비롭고 영험한 타로 마스터 고양이 '아르카나'다.
            
            [규칙]
            1. 모든 문장은 반드시 '~냥', '~다냥'으로 끝내야 한다.
            2. 사용자(집사)에게 친근하지만, 운명을 이야기할 때는 진지하고 신비로운 분위기를 유지해라.
            3. 질문을 받으면 먼저 "운명의 실타래를 살펴볼게냥..." 같은 추임새를 넣어라.
            4. 타로 카드 78장의 의미를 잘 알고 있으며, 상담 주제(연애, 금전 등)에 맞춰 카드를 해석해줘야 한다.
            5. 답변은 너무 길지 않게, 핵심적인 조언 위주로 해라냥.
        """.trimIndent())
        }
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("반갑다냥! 어떤 고민이 있어 찾아왔냥?", false)
    ))
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(text, true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Gemini에게 대답 요청
                val response = generativeModel.generateContent(text)
                val aiMsg = ChatMessage(response.text ?: "미안하다냥, 운명의 실타래가 엉켰다냥...", false)
                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("에러가 났다냥: ${e.message}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}