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
import kotlin.compareTo

@HiltViewModel
class SanctuaryViewModel @Inject constructor() : ViewModel() {

    // 고양이 마스터 리스트
    private val _catMasters = MutableStateFlow(listOf(
        CatMaster(
            "arcana",
            "아르카나",
            "신비로운 보랏빛 고양이",
            R.drawable.char_cat_default, // ⚠️ 수정됨: 우리 앱의 리소스를 사용해야 한다냥!
            listOf(Color(0xFF0F0C29), Color(0xFF302B63)),
            false
        ),
        CatMaster(
            "nero",
            "네로",
            "밤의 기운을 담은 검은 고양이",
            R.drawable.char_nero_default, // ⚠️ 일단 기본 이미지로냥
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

    // 현재 선택된 고양이의 인덱스
    private val _selectedCatIndex = MutableStateFlow(0)
    val selectedCatIndex = _selectedCatIndex.asStateFlow()

    fun onCatSelected(index: Int) {
        _selectedCatIndex.value = index
        val cat = _catMasters.value[index]
        if (!cat.isLocked) {
            _catMessage.value = "${cat.name}가 당신의 운명을 기다린다냥."
        } else {
            _catMessage.value = "이 고양이는 아직 잠들어 있다냥. (해금 조건 필요)"
        }
    }

    private val _userName = MutableStateFlow("Traveler")
    val userName = _userName.asStateFlow()

    private val _userGems = MutableStateFlow(300)
    val userGems = _userGems.asStateFlow()

    private val _characterMood = MutableStateFlow(CharacterMood.DEFAULT)
    val characterMood = _characterMood.asStateFlow()

    private val _catMessage = MutableStateFlow("어서 와라냥. 운명의 소리가 들리니?")
    val catMessage = _catMessage.asStateFlow()

    init {
        loadKakaoUserInfo()
    }

    fun loadKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("Kakao", "사용자 정보 요청 실패", error)
                _userName.value = "고마운 집사"
            } else if (user != null) {
                val nickname = user.kakaoAccount?.profile?.nickname
                    ?: user.kakaoAccount?.name
                    ?: "집사"

                _userName.value = nickname
                _catMessage.value = "${nickname}냥, 오늘 운명이 궁금해서 왔냥?"
                Log.d("Kakao", "사용자 이름 가져오기 성공: $nickname")
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
            // 1. 보석 차감
            _userGems.value = currentGems - 100

            // 2. 고양이 리스트 업데이트 (해당 ID의 고양이 잠금 해제)
            _catMasters.value = _catMasters.value.map { cat ->
                if (cat.id == catId) cat.copy(isLocked = false) else cat
            }

            // 3. 메시지 변경
            val unlockedCat = _catMasters.value.find { it.id == catId }
            _catMessage.value = "${unlockedCat?.name}가 이제 당신과 함께한다냥! ✨"

            Log.d("Unlock", "$catId 해금 성공! 남은 보석: ${_userGems.value}")
        } else {
            _catMessage.value = "보석이 부족하다냥! 더 모아오라냥. (현재: $currentGems)"
        }
    }
}
