package com.example.arcanaai.feature.altar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.CardBackGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AltarViewModel @Inject constructor() : ViewModel() {

    private val _userGems = MutableStateFlow(500)
    val userGems = _userGems.asStateFlow()

    private val _cardBacks = MutableStateFlow(listOf(
        CardBack("default", "기본 뒷면", R.drawable.img_card_back, CardBackGrade.NORMAL, true),
        CardBack("moon", "초승달의 노래", R.drawable.img_card_back_moon, CardBackGrade.RARE, false),
        CardBack("sun", "태양의 가호", R.drawable.img_card_back_sun, CardBackGrade.RARE, false),
        CardBack("mystic", "심연의 눈", R.drawable.img_card_back_eyes, CardBackGrade.MYSTIC, false),
        CardBack("legend", "아르카나의 전설", R.drawable.img_card_back_legend, CardBackGrade.LEGENDARY, false)
    ))
    val cardBacks = _cardBacks.asStateFlow()

    private val _equippedBackId = MutableStateFlow("default")
    val equippedBackId = _equippedBackId.asStateFlow()

    private val _isGachaRunning = MutableStateFlow(false)
    val isGachaRunning = _isGachaRunning.asStateFlow()

    private val _lastGachaResult = MutableStateFlow<CardBack?>(null)
    val lastGachaResult = _lastGachaResult.asStateFlow()

    // 가챠 대화상자/오버레이 표시 여부
    private val _showGachaOverlay = MutableStateFlow(false)
    val showGachaOverlay = _showGachaOverlay.asStateFlow()

    fun drawCardBack() {
        // 중복 방지: 아직 소유하지 않은 카드들만 필터링
        val unownedCards = _cardBacks.value.filter { !it.isOwned }
        
        if (_userGems.value < 100 || _isGachaRunning.value || unownedCards.isEmpty()) return

        viewModelScope.launch {
            _userGems.value -= 100
            _isGachaRunning.value = true
            _showGachaOverlay.value = true
            _lastGachaResult.value = null

            // 도파민 터지는 연출 시간 (3초로 연장냥!)
            delay(3000)

            val result = unownedCards.random()

            // 소유권 업데이트
            _cardBacks.value = _cardBacks.value.map { 
                if (it.id == result.id) it.copy(isOwned = true) else it
            }
            
            _lastGachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    fun dismissGacha() {
        _showGachaOverlay.value = false
        _lastGachaResult.value = null
    }

    fun equipCardBack(id: String) {
        val target = _cardBacks.value.find { it.id == id }
        if (target != null && target.isOwned) {
            _equippedBackId.value = id
        }
    }
}
