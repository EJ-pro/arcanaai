package com.example.arcanaai.feature.altar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.CardBackGrade
import com.example.arcanaai.data.model.UserProfile
import com.example.arcanaai.data.repository.UserRepository
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AltarViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 🏰 전역 저장소의 상태를 그대로 가져와서 쓴다냥!
    val userGems = userRepository.userProfile.map { it?.gems ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equippedBackId = userRepository.userProfile.map { it?.equippedBackId ?: "default" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")

    private val _cardBacks = MutableStateFlow(listOf(
        CardBack("default", "기본 뒷면", R.drawable.img_card_back, CardBackGrade.NORMAL, true),
        CardBack("moon", "초승달의 노래", R.drawable.img_card_back_moon, CardBackGrade.RARE, false),
        CardBack("sun", "태양의 가호", R.drawable.img_card_back_sun, CardBackGrade.RARE, false),
        CardBack("mystic", "심연의 눈", R.drawable.img_card_back_eyes, CardBackGrade.MYSTIC, false),
        CardBack("legend", "아르카나의 전설", R.drawable.img_card_back_legend, CardBackGrade.LEGENDARY, false)
    ))
    
    // 소유권 정보 연동냥!
    val cardBacks = combine(_cardBacks, userRepository.ownedCardBacks) { backs, ownedIds ->
        backs.map { it.copy(isOwned = ownedIds.contains(it.id) || it.id == "default") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _cardBacks.value)

    private val _isGachaRunning = MutableStateFlow(false)
    val isGachaRunning = _isGachaRunning.asStateFlow()

    private val _lastGachaResult = MutableStateFlow<CardBack?>(null)
    val lastGachaResult = _lastGachaResult.asStateFlow()

    private val _showGachaOverlay = MutableStateFlow(false)
    val showGachaOverlay = _showGachaOverlay.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    fun drawCardBack() {
        val currentProfile = userRepository.userProfile.value ?: return
        val unownedCards = _cardBacks.value.filter { back -> 
            !userRepository.ownedCardBacks.value.contains(back.id) && back.id != "default"
        }
        
        if (currentProfile.gems < 100 || _isGachaRunning.value || unownedCards.isEmpty()) return

        viewModelScope.launch {
            // 1. 젬 차감 및 가챠 시작냥!
            userRepository.updateGems(currentProfile.id, currentProfile.gems - 100)
            _isGachaRunning.value = true
            _showGachaOverlay.value = true
            _lastGachaResult.value = null

            delay(3000)

            val result = unownedCards.random()

            // 2. 서버에 카드 추가냥!
            userRepository.addCardBack(currentProfile.id, result.id)
            
            _lastGachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    fun addGems(amount: Int) {
        val currentProfile = userRepository.userProfile.value ?: return
        viewModelScope.launch {
            userRepository.updateGems(currentProfile.id, currentProfile.gems + amount)
        }
    }

    fun dismissGacha() {
        _showGachaOverlay.value = false
        _lastGachaResult.value = null
    }

    fun equipCardBack(id: String) {
        val currentProfile = userRepository.userProfile.value ?: return
        val ownedIds = userRepository.ownedCardBacks.value
        if (ownedIds.contains(id) || id == "default") {
            viewModelScope.launch {
                userRepository.updateEquippedCat(currentProfile.id, id)
            }
        }
    }

    fun logout() {
        UserApiClient.instance.logout { error ->
            viewModelScope.launch {
                _logoutEvent.emit(Unit)
            }
        }
    }
}
