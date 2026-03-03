package com.example.arcanaai.feature.sanctuary

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.data.model.CharacterMood
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SanctuaryViewModel @Inject constructor() : ViewModel() {

    // 고양이 마스터 리스트
    private val _catMasters = MutableStateFlow(listOf(
        CatMaster(
            "arcana",
            "아르카나",
            "신비로운 보랏빛 고양이",
            R.drawable.char_cat_default,
            listOf(Color(0xFF0F0C29), Color(0xFF302B63)),
            false
        ),
        CatMaster(
            "nero",
            "네로",
            "밤의 기운을 담은 검은 고양이",
            R.drawable.char_nero_default,
            listOf(Color(0xFF141E30), Color(0xFF243B55)),
            true
        ),
        CatMaster(
            "leo",
            "레오",
            "태양의 가호를 받는 황금 고양이",
            R.drawable.char_leo_default,
            listOf(Color(0xFFED8F03), Color(0xFFFFB75E)),
            true
        )
    ))
    val catMasters = _catMasters.asStateFlow()

    private val _viewingCatIndex = MutableStateFlow(0)
    val viewingCatIndex = _viewingCatIndex.asStateFlow()

    private val _activeCatId = MutableStateFlow("arcana")
    val activeCatId = _activeCatId.asStateFlow()

    fun onCatPagerChanged(index: Int) {
        _viewingCatIndex.value = index
        val cat = _catMasters.value[index]
        if (cat.isLocked) {
            _catMessage.value = "이 고양이는 아직 잠들어 있다냥. (해금 조건 필요)"
        } else {
            if (cat.id == _activeCatId.value) {
                _catMessage.value = "${cat.name}가 당신과 함께하고 있다냥."
            } else {
                _catMessage.value = "${cat.name}를 선택할 수 있다냥."
            }
        }
    }

    fun selectCat(catId: String) {
        val cat = _catMasters.value.find { it.id == catId }
        if (cat != null && !cat.isLocked) {
            _activeCatId.value = catId
            _catMessage.value = "${cat.name}를 상담사로 선택했다냥! ✨"
        }
    }

    private val _userName = MutableStateFlow("Traveler")
    val userName = _userName.asStateFlow()

    // 💎 젬 상태 (실제 앱에서는 전역 관리가 필요함냥!)
    private val _userGems = MutableStateFlow(300)
    val userGems = _userGems.asStateFlow()

    private val _characterMood = MutableStateFlow(CharacterMood.DEFAULT)
    val characterMood = _characterMood.asStateFlow()

    private val _catMessage = MutableStateFlow("어서 와라냥. 운명의 소리가 들리니?")
    val catMessage = _catMessage.asStateFlow()

    init {
        loadKakaoUserInfo()
    }

    fun addGems(amount: Int) {
        _userGems.value += amount
    }

    fun loadKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("Kakao", "사용자 정보 요청 실패", error)
                _userName.value = "고마운 집사"
            } else if (user != null) {
                val nickname = user.kakaoAccount?.profile?.nickname ?: "집사"
                _userName.value = nickname
                _catMessage.value = "${nickname}냥, 오늘 운명이 궁금해서 왔냥?"
            }
        }
    }

    fun onCharacterTouched() {
        viewModelScope.launch {
            val currentName = _userName.value
            _characterMood.value = CharacterMood.LISTENING
            _catMessage.value = "앗, $currentName! 간지럽다냥! 냐옹!"
            delay(2000)
            _characterMood.value = CharacterMood.DEFAULT
            _catMessage.value = "${currentName}냥, 궁금한 게 있다면 물어보라냥."
        }
    }

    fun unlockCat(catId: String) {
        val currentGems = _userGems.value
        if (currentGems >= 100) {
            _userGems.value = currentGems - 100
            _catMasters.value = _catMasters.value.map { cat ->
                if (cat.id == catId) cat.copy(isLocked = false) else cat
            }
            val unlockedCat = _catMasters.value.find { it.id == catId }
            _catMessage.value = "${unlockedCat?.name}가 이제 당신과 함께한다냥! 선택하기 버튼을 눌러보라냥. ✨"
        } else {
            _catMessage.value = "보석이 부족하다냥! 더 모아오라냥. (현재: $currentGems)"
        }
    }
}
