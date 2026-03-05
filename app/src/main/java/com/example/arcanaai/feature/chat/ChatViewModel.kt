package com.example.arcanaai.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.local.ChatDao
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.data.model.TarotCard
import com.example.arcanaai.data.repository.TarotRepository
import com.example.arcanaai.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.arcanaai.R
import kotlinx.coroutines.delay

sealed class TarotUiState {
    object Chatting : TarotUiState()
    object Picking : TarotUiState()
    data class Loading(val selectedCards: List<TarotCard>) : TarotUiState()
    data class Result(val selectedCards: List<TarotCard>, val interpretation: String) : TarotUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: TarotRepository,
    private val userRepository: UserRepository,
    private val chatDao: ChatDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val topic: String = savedStateHandle["topic"] ?: "free"
    val catId: String = savedStateHandle["catId"] ?: "arcana"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<TarotUiState>(
        if (topic == "chatbot") TarotUiState.Chatting else TarotUiState.Picking
    )
    val uiState = _uiState.asStateFlow()

    private val _selectedCards = MutableStateFlow<List<TarotCard>>(emptyList())
    val selectedCards = _selectedCards.asStateFlow()

    private val _allCards = MutableStateFlow(repository.getAllCards().shuffled())
    val allCards: List<TarotCard> get() = _allCards.value

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress = _loadingProgress.asStateFlow()

    private var isGemConsumed = false

    val equippedBackRes = userRepository.userProfile.map { profile ->
        when (profile?.equippedBackId ?: "default") {
            "moon" -> R.drawable.img_card_back_moon
            "sun" -> R.drawable.img_card_back_sun
            "mystic" -> R.drawable.img_card_back_eyes
            "legend" -> R.drawable.img_card_back_legend
            "butterfly" -> R.drawable.img_card_bac_butterfly
            "twin_moon" -> R.drawable.img_card_bac_twin_moons
            "yggdrasil" -> R.drawable.img_card_bac_yggdrasil
            "cut_eyes" -> R.drawable.img_card_back_cut_eyes
            else -> R.drawable.img_card_back
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), R.drawable.img_card_back)

    val catName: String = when (catId) {
        "nero" -> "네로"
        "leo" -> "레오"
        else -> "아르카나"
    }

    init {
        if (topic == "chatbot") {
            addMessage("안녕냥! 나는 ${catName} 마스터다냥. 고민이 있거나 심심할 때 언제든 나랑 수다 떨자냥!", false)
        } else {
            consumeGemsForTarot()
        }
    }

    private fun consumeGemsForTarot() {
        if (isGemConsumed) return
        viewModelScope.launch {
            userRepository.userProfile.filterNotNull().first().let { profile ->
                if (profile.gems >= 30) {
                    userRepository.updateGems(profile.id, profile.gems - 30)
                    isGemConsumed = true
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _isLoading.value) return
        viewModelScope.launch {
            addMessage(content, true)
            _isLoading.value = true
            val history = _messages.value.map { it.content to it.isFromUser }
            val response = repository.getAiChatResponse(history, content)
            addMessage(response, false)
            _isLoading.value = false
            if (topic != "chatbot" && (response.contains("카드") || response.contains("뽑"))) {
                delay(1500)
                startPicking()
            }
        }
    }

    private fun addMessage(content: String, isFromUser: Boolean) {
        val newMessage = ChatMessage(content = content, isFromUser = isFromUser, topic = topic, characterMood = "NORMAL")
        _messages.value = _messages.value + newMessage
    }

    fun startPicking() {
        if (!isGemConsumed) consumeGemsForTarot()
        _uiState.value = TarotUiState.Picking
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
            viewModelScope.launch {
                _uiState.value = TarotUiState.Loading(_selectedCards.value)
                _loadingProgress.value = 0
                
                // 💡 10초 동안 100%까지 천천히 올리기냥! (100ms * 100회 = 10초)
                val progressJob = launch {
                    for (i in 1..100) {
                        delay(100) 
                        _loadingProgress.value = i
                    }
                }

                val userGoal = if (topic == "chatbot") "자유 대화" else topic
                val interpretation = repository.getDeepInterpretation(userGoal, _selectedCards.value)
                
                // 10초 애니메이션이 끝날 때까지 대기냥!
                progressJob.join()
                
                _uiState.value = TarotUiState.Result(_selectedCards.value, interpretation)
                
                userRepository.userProfile.value?.let { profile ->
                    userRepository.gainExp(profile.id, 20)
                }
                saveResultToHistory(_selectedCards.value, interpretation)
            }
        }
    }

    private fun saveResultToHistory(cards: List<TarotCard>, interpretation: String) {
        viewModelScope.launch {
            val cardNames = cards.joinToString { it.name }
            val historyEntry = ChatMessage(
                content = interpretation,
                isFromUser = false,
                topic = topic,
                relatedCardName = cardNames,
                characterMood = "HAPPY"
            )
            chatDao.insertMessage(historyEntry)
        }
    }

    fun reset() {
        isGemConsumed = false
        if (topic == "chatbot") {
            _messages.value = emptyList()
            addMessage("그래냥! 다시 이야기를 시작해보자냥.", false)
            _uiState.value = TarotUiState.Chatting
        } else {
            _selectedCards.value = emptyList()
            _uiState.value = TarotUiState.Picking
            _allCards.value = repository.getAllCards().shuffled()
            consumeGemsForTarot()
        }
    }
}
