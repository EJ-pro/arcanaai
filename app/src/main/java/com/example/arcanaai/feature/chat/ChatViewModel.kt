package com.example.arcanaai.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.arcanaai.data.model.TarotCard
import com.example.arcanaai.data.repository.TarotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.arcanaai.R

sealed class TarotUiState {
    object Picking : TarotUiState()
    data class Result(val selectedCards: List<TarotCard>, val interpretation: String) : TarotUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: TarotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val topic: String = savedStateHandle["topic"] ?: "free"
    val catId: String = savedStateHandle["catId"] ?: "arcana"

    private val _uiState = MutableStateFlow<TarotUiState>(TarotUiState.Picking)
    val uiState = _uiState.asStateFlow()

    private val _selectedCards = MutableStateFlow<List<TarotCard>>(emptyList())
    val selectedCards = _selectedCards.asStateFlow()

    private val _allCards = MutableStateFlow(repository.getAllCards().shuffled())
    val allCards: List<TarotCard> get() = _allCards.value

    val catImageRes: Int = when (catId) {
        "nero" -> R.drawable.char_nero_default
        "leo" -> R.drawable.char_leo_default
        else -> R.drawable.char_cat_default
    }

    val catName: String = when (catId) {
        "nero" -> "네로"
        "leo" -> "레오"
        else -> "아르카나"
    }

    fun onCardClick(card: TarotCard) {
        if (_uiState.value is TarotUiState.Picking) {
            val currentSelected = _selectedCards.value
            if (currentSelected.any { it.id == card.id }) {
                _selectedCards.value = currentSelected.filter { it.id != card.id }
            } else if (currentSelected.size < 3) {
                _selectedCards.value = currentSelected + card
            }
        }
    }

    fun completeSelection() {
        if (_selectedCards.value.size == 3) {
            val interpretation = generateLocalInterpretation(_selectedCards.value)
            _uiState.value = TarotUiState.Result(_selectedCards.value, interpretation)
        }
    }

    fun reset() {
        _allCards.value = repository.getAllCards().shuffled()
        _selectedCards.value = emptyList()
        _uiState.value = TarotUiState.Picking
    }

    private fun generateLocalInterpretation(cards: List<TarotCard>): String {
        if (cards.size < 3) return "운명의 실타래가 꼬였다냥! 카드가 부족하다냥."

        val c1 = cards[0]
        val c2 = cards[1]
        val c3 = cards[2]

        return """
            🔮 ${catName} 마스터가 집사의 운명을 읽어냈다냥! 🐾
            
            🌿 [현재의 기운: ${c1.name}]
            지금 집사의 주변에는 '${c1.keyword}'의 에너지가 강하게 감돌고 있다냥. 
            ${c1.description.replace("다냥.", "다냥,")} 그래서 지금은 집사의 내면을 먼저 들여다보는 게 중요한 시기다냥.
            
            ⚡ [마주할 변화: ${c2.name}]
            하지만 조만간 '${c2.keyword}'와(과) 관련된 새로운 흐름이 찾아올 거다냥! 
            ${c2.description.replace("다냥.", "다냥,")} 이 변화를 어떻게 받아들이느냐에 따라 운명의 방향이 크게 바뀔 수 있다냥. 
            
            ✨ [운명의 결말: ${c3.name}]
            결국 이 모든 여정의 끝에서 집사는 '${c3.keyword}'의 결실을 맺게 될 거다냥. 
            ${c3.description} 아르카나가 보기엔 아주 신비롭고 값진 결과가 기다리고 있으니 기대해도 좋다냥!
            
            🐾 ${catName}의 마법 한마디 🐾
            집사가 선택한 세 개의 운명이 묘하게 얽혀있다냥. 
            '${c1.keyword}'의 기운을 소중히 간직하고 '${c2.keyword}'를 잘 헤쳐나간다면, 결국 '${c3.keyword}'의 결말에 닿게 될 거다냥!
            
            더 깊은 통찰력은 나중에 집사와 1:1 대화에서 나누자냥! 🐈🐾
        """.trimIndent()
    }
}
