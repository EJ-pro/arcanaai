package com.example.arcanaai.feature.sanctuary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.model.CharacterMood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 성전(메인 로비)의 상태를 관리하는 뷰모델
 */
@HiltViewModel
class SanctuaryViewModel @Inject constructor() : ViewModel() {

    // 1. 사용자 정보 상태 (실제로는 UserRepo나 DataStore에서 가져와야 함)
    private val _userName = MutableStateFlow("Traveler")
    val userName = _userName.asStateFlow()

    private val _userGems = MutableStateFlow(300)
    val userGems = _userGems.asStateFlow()

    // 2. 캐릭터(고양이) 기분 상태
    private val _characterMood = MutableStateFlow(CharacterMood.DEFAULT)
    val characterMood = _characterMood.asStateFlow()

    // 3. 오늘의 타로 뽑기 가능 여부
    private val _isDailyTarotAvailable = MutableStateFlow(true)
    val isDailyTarotAvailable = _isDailyTarotAvailable.asStateFlow()

    // 4. 고양이의 랜덤 대사
    private val _catMessage = MutableStateFlow("어서 와라냥. 운명의 소리가 들리니?")
    val catMessage = _catMessage.asStateFlow()

    private val randomMessages = listOf(
        "오늘 기운이 아주 묘~하다냥. ✨",
        "수정구슬이 네 고민을 기다리고 있어.",
        "그루밍 하느라 바빴다냥. 무슨 일이니?",
        "별들이 너에게 할 말이 많은 것 같아.",
        "츄르 한 그릇 주면 더 잘 봐줄 텐데... 🐾"
    )

    init {
        // 앱 실행 시 고양이가 랜덤하게 인사를 건넴
        rotateCatMessage()
    }

    /**
     * 고양이를 터치했을 때 반응
     */
    fun onCharacterTouched() {
        viewModelScope.launch {
            _characterMood.value = CharacterMood.LISTENING
            _catMessage.value = "앗, 깜짝이야! 냐옹!"
            delay(2000)
            _characterMood.value = CharacterMood.DEFAULT
            rotateCatMessage()
        }
    }

    /**
     * 오늘의 타로 뽑기 실행
     */
    fun drawDailyTarot() {
        if (_isDailyTarotAvailable.value) {
            _isDailyTarotAvailable.value = false
            // TODO: 실제 카드 뽑기 로직 연동
        }
    }

    private fun rotateCatMessage() {
        _catMessage.value = randomMessages.random()
    }
}