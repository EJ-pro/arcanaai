package com.example.arcanaai.feature.sanctuary

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.data.model.CharacterMood
import com.example.arcanaai.data.model.UserProfile
import com.example.arcanaai.data.repository.UserRepository
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SanctuaryViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 🔮 모든 고양이 마스터 정보를 여기서도 동기화냥!
    private val _allMasters = listOf(
        CatMaster("arcana", "아르카나", "신비로운 보랏빛 고양이", R.drawable.char_cat_default, listOf(Color(0xFF6f42c1), Color(0xFF2c003e))),
        CatMaster("rubi", "루비", "불꽃처럼 강렬한 고양이", R.drawable.char_cat_red, listOf(Color(0xFFd90429), Color(0xFF6f0000))),
        CatMaster("coral", "코랄", "따듯한 햇살의 고양이", R.drawable.char_cat_orange, listOf(Color(0xFFf95738), Color(0xFF8c2f00))),
        CatMaster("topaz", "토파즈", "밝고 긍정적인 고양이", R.drawable.char_cat_yellow, listOf(Color(0xFFffca3a), Color(0xFFc37300))),
        CatMaster("gaia", "가이아", "자연과 치유의 고양이", R.drawable.char_cat_green, listOf(Color(0xFF8ac926), Color(0xFF005f08))),
        CatMaster("sapphier", "사파이어", "깊은 바다속 고양이", R.drawable.char_cat_blue, listOf(Color(0xFF1982c4), Color(0xFF003566))),
        CatMaster("night", "나이트", "심연의 신비로운 고양이", R.drawable.char_cat_dark_blue, listOf(Color(0xFF001233), Color(0xFF000000))),
        CatMaster("shadow", "쉐도우", "모든것을 삼키는 고양이", R.drawable.char_cat_dark, listOf(Color(0xFF333333), Color(0xFF000000))),
        CatMaster("crystal", "크리스탈", "순수한 시작의 고양이", R.drawable.char_cat_white, listOf(Color(0xFFe0e0e0), Color(0xFF9e9e9e))),
        CatMaster("nero", "네로", "밤의 기운을 담은 검은 고양이", R.drawable.char_nero_default, listOf(Color(0xFF141E30), Color(0xFF243B55))),
        CatMaster("leo", "레오", "태양의 가호를 받는 황금 고양이", R.drawable.char_leo_default, listOf(Color(0xFFED8F03), Color(0xFFFFB75E))),
        CatMaster("white", "화이트", "순수한 눈꽃 고양이", R.drawable.char_cat_white, listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0)))
    )

    val userName = userRepository.userProfile.map { it?.nickname ?: "Traveler" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Traveler")

    val userGems = userRepository.userProfile.map { it?.gems }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCatId = userRepository.userProfile.map { it?.equippedMasterId ?: "arcana" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "arcana")

    // 해금 정보와 연동된 고양이 리스트냥!
    val catMasters = combine(userRepository.unlockedMasters, userRepository.userProfile) { unlockedIds, _ ->
        _allMasters.map { it.copy(isLocked = !unlockedIds.contains(it.id) && it.id != "arcana") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _allMasters)

    private val _viewingCatIndex = MutableStateFlow(0)
    val viewingCatIndex = _viewingCatIndex.asStateFlow()

    private val _characterMood = MutableStateFlow(CharacterMood.DEFAULT)
    val characterMood = _characterMood.asStateFlow()

    private val _catMessage = MutableStateFlow("어서 와라냥. 운명의 소리가 들리니?")
    val catMessage = _catMessage.asStateFlow()

    private var currentUserId: String? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                currentUserId = user.id.toString()
                viewModelScope.launch {
                    userRepository.refreshUserData(currentUserId!!)
                    if (userRepository.userProfile.value == null) {
                        val newProfile = UserProfile(id = currentUserId!!, nickname = user.kakaoAccount?.profile?.nickname ?: "집사", gems = 300)
                        userRepository.createProfile(newProfile)
                    }
                }
            }
        }
    }

    fun onCatPagerChanged(index: Int) {
        _viewingCatIndex.value = index
        val cat = _allMasters[index]
        _catMessage.value = if (cat.isLocked) "잠들어 있는 마스터다냥." else "${cat.name}가 함께할 준비가 됐다냥."
    }

    fun selectCat(catId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            userRepository.updateEquippedMaster(userId, catId)
        }
    }

    fun addGems(amount: Int) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val current = userGems.value ?: 0
            userRepository.updateGems(userId, current + amount)
        }
    }

    fun unlockCat(catId: String) {
        val userId = currentUserId ?: return
        val current = userGems.value ?: 0
        if (current >= 100) {
            viewModelScope.launch {
                userRepository.updateGems(userId, current - 100)
                userRepository.unlockMaster(userId, catId)
            }
        }
    }

    fun onCharacterTouched() {
        viewModelScope.launch {
            _characterMood.value = CharacterMood.LISTENING
            _catMessage.value = "앗! 간지럽다냥! 냐옹!"
            delay(1500)
            _characterMood.value = CharacterMood.DEFAULT
            _catMessage.value = "${userName.value}냥, 궁금한 게 있냥?"
        }
    }
}
