package com.example.arcanaai.feature.gallery

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.CardBackGrade
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val userProfile = userRepository.userProfile
    
    val userName = userProfile.map { it?.nickname ?: "집사" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "집사")

    val userGems = userProfile.map { it?.gems ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equippedMasterId = userProfile.map { it?.equippedMasterId ?: "arcana" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "arcana")

    val equippedBackId = userProfile.map { it?.equippedBackId ?: "default" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")

    private val _allCardBacks = listOf(
        CardBack("default", "기본 뒷면", R.drawable.img_card_back, CardBackGrade.NORMAL),
        CardBack("moon", "초승달의 노래", R.drawable.img_card_back_moon, CardBackGrade.RARE),
        CardBack("sun", "태양의 가호", R.drawable.img_card_back_sun, CardBackGrade.RARE),
        CardBack("mystic", "심연의 눈", R.drawable.img_card_back_eyes, CardBackGrade.MYSTIC),
        CardBack("legend", "아르카나의 전설", R.drawable.img_card_back_legend, CardBackGrade.LEGENDARY),
        CardBack("butterfly", "나비의 춤", R.drawable.img_card_bac_butterfly, CardBackGrade.LEGENDARY),
        CardBack("twin_moon", "쌍둥이 달", R.drawable.img_card_bac_twin_moons, CardBackGrade.LEGENDARY),
        CardBack("yggdrasil", "세계수의 잎", R.drawable.img_card_bac_yggdrasil, CardBackGrade.LEGENDARY),
        CardBack("cut_eyes", "큐트 아르카나", R.drawable.img_card_back_cut_eyes, CardBackGrade.LEGENDARY)
    )

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
        CatMaster("leo", "레오", "태양의 가호를 받는 황금 고양이", R.drawable.char_leo_default, listOf(Color(0xFFED8F03), Color(0xFFFFB75E)))
    )

    val cardBacks = combine(userRepository.ownedCardBacks, userProfile) { ownedIds, _ ->
        _allCardBacks.map { it.copy(isOwned = ownedIds.contains(it.id) || it.id == "default") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _allCardBacks)

    val catMasters = combine(userRepository.unlockedMasters, userProfile) { unlockedIds, _ ->
        _allMasters.map { it.copy(isLocked = !unlockedIds.contains(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _allMasters)

    private val _isGachaRunning = MutableStateFlow(false)
    val isGachaRunning = _isGachaRunning.asStateFlow()

    private val _gachaResult = MutableStateFlow<Any?>(null)
    val gachaResult = _gachaResult.asStateFlow()

    private val _gachaType = MutableStateFlow<String?>(null) // "master" or "card"
    val gachaType = _gachaType.asStateFlow()

    fun addGems(amount: Int) {
        val profile = userProfile.value ?: return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems + amount)
        }
    }

    fun equipMaster(masterId: String) {
        val profile = userProfile.value ?: return
        val isUnlocked = userRepository.unlockedMasters.value.contains(masterId) || masterId == "arcana"
        if (!isUnlocked) return

        viewModelScope.launch {
            userRepository.updateEquippedMaster(profile.id, masterId)
            userRepository.refreshUserData(profile.id)
        }
    }

    fun equipCardBack(backId: String) {
        val profile = userProfile.value ?: return
        val isOwned = userRepository.ownedCardBacks.value.contains(backId) || backId == "default"
        if (!isOwned) return

        viewModelScope.launch {
            userRepository.updateEquippedBack(profile.id, backId)
            userRepository.refreshUserData(profile.id)
        }
    }

    fun drawMaster() {
        val profile = userProfile.value ?: return
        val lockMasters = _allMasters.filter { master -> !userRepository.unlockedMasters.value.contains(master.id) }
        
        if (profile.gems < 300 || _isGachaRunning.value || lockMasters.isEmpty()) return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems - 300)
            _gachaType.value = "master"
            _isGachaRunning.value = true
            _gachaResult.value = null
            delay(2500)
            val result = lockMasters.random()
            userRepository.unlockMaster(profile.id, result.id)
            userRepository.refreshUserData(profile.id)
            _gachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    fun drawCardBack() {
        val profile = userProfile.value ?: return
        val unownedCards = _allCardBacks.filter { back -> !userRepository.ownedCardBacks.value.contains(back.id) && back.id != "default" }
        
        if (profile.gems < 300 || _isGachaRunning.value || unownedCards.isEmpty()) return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems - 300)
            _gachaType.value = "card"
            _isGachaRunning.value = true
            _gachaResult.value = null
            delay(2500)
            val result = unownedCards.random()
            userRepository.addCardBack(profile.id, result.id)
            userRepository.refreshUserData(profile.id)
            _gachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    fun dismissGacha() {
        _gachaResult.value = null
        _gachaType.value = null
    }
}
