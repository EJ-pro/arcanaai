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

    private val _showGachaOverlay = MutableStateFlow(false)
    val showGachaOverlay = _showGachaOverlay.asStateFlow()

    fun drawCardBack() {
        val unownedCards = _cardBacks.value.filter { !it.isOwned }
        
        if (_userGems.value < 100 || _isGachaRunning.value || unownedCards.isEmpty()) return

        viewModelScope.launch {
            _userGems.value -= 100
            _isGachaRunning.value = true
            _showGachaOverlay.value = true
            _lastResultSet(null)

            delay(3000)

            val result = unownedCards.random()

            _cardBacks.value = _cardBacks.value.map { 
                if (it.id == result.id) it.copy(isOwned = true) else it
            }
            
            _lastGachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    private fun _lastResultSet(value: CardBack?) {
        _lastGachaResult.value = value
    }

    fun addGems(amount: Int) {
        _userGems.value += amount
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
