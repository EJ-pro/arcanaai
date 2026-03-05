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
        CatMaster("arcana", "아르카나", "신비로운 보랏빛 고양이", R.drawable.char_cat_default, listOf(Color(0xFF0F0C29), Color(0xFF302B63))),
        CatMaster("nero", "네로", "밤의 기운을 담은 검은 고양이", R.drawable.char_nero_default, listOf(Color(0xFF141E30), Color(0xFF243B55))),
        CatMaster("leo", "레오", "태양의 가호를 받는 황금 고양이", R.drawable.char_leo_default, listOf(Color(0xFFED8F03), Color(0xFFFFB75E))),
        CatMaster("white", "화이트", "순수한 눈꽃 고양이", R.drawable.char_cat_white, listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0))) // 👈 White 추가냥!
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
            _isGachaRunning.value = true
            _gachaResult.value = null
            delay(2000)
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
            _isGachaRunning.value = true
            _gachaResult.value = null
            delay(2000)
            val result = unownedCards.random()
            userRepository.addCardBack(profile.id, result.id)
            userRepository.refreshUserData(profile.id)
            _gachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    fun dismissGacha() {
        _gachaResult.value = null
    }
}
