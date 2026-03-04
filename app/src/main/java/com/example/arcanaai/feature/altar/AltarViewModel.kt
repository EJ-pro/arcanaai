package com.example.arcanaai.feature.altar

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.R
import com.example.arcanaai.core.network.BillingManager
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
    private val userRepository: UserRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    val userProfile = userRepository.userProfile
    
    val userGems = userProfile.map { it?.gems ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equippedBackId = userProfile.map { it?.equippedBackId ?: "default" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "default")

    // 🎴 전체 카드 뒷면 리스트냥!
    private val _cardBacks = MutableStateFlow(listOf(
        CardBack("default", "기본 뒷면", R.drawable.img_card_back, CardBackGrade.NORMAL, true),
        CardBack("moon", "초승달의 노래", R.drawable.img_card_back_moon, CardBackGrade.RARE, false),
        CardBack("sun", "태양의 가호", R.drawable.img_card_back_sun, CardBackGrade.RARE, false),
        CardBack("mystic", "심연의 눈", R.drawable.img_card_back_eyes, CardBackGrade.MYSTIC, false),
        CardBack("legend", "아르카나의 전설", R.drawable.img_card_back_legend, CardBackGrade.LEGENDARY, false),
        CardBack("butterfly", "나비의 춤", R.drawable.img_card_bac_butterfly, CardBackGrade.LEGENDARY, false),
        CardBack("twin_moon", "쌍둥이 달", R.drawable.img_card_bac_twin_moons, CardBackGrade.LEGENDARY, false),
        CardBack("yggdrasil", "세계수의 잎", R.drawable.img_card_bac_yggdrasil, CardBackGrade.LEGENDARY, false),
        CardBack("cut_eyes", "큐트 아르카나", R.drawable.img_card_back_cut_eyes, CardBackGrade.LEGENDARY, false)
    ))
    
    // 소유권 정보를 서버 금고와 실시간 동기화냥!
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

    private var currentUserId: String? = null

    init {
        syncUserData()
        observePurchaseEvents()
    }

    private fun syncUserData() {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                currentUserId = user.id.toString()
                viewModelScope.launch {
                    userRepository.refreshUserData(currentUserId!!)
                }
            }
        }
    }

    private fun observePurchaseEvents() {
        viewModelScope.launch {
            billingManager.purchaseSuccessEvent.collect { (productId, amount) ->
                val profile = userProfile.value ?: return@collect
                userRepository.updateGems(profile.id, profile.gems + amount)
            }
        }
    }

    fun startPurchase(activity: Activity, productId: String, amount: Int) {
        billingManager.launchPurchaseFlow(activity, productId, amount)
    }

    fun addGems(amount: Int) {
        val profile = userProfile.value ?: return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems + amount)
        }
    }

    // 🎰 도파민 터지는 가챠 로직냥!
    fun drawCardBack() {
        val profile = userProfile.value ?: return
        val unownedCards = _cardBacks.value.filter { back -> 
            !userRepository.ownedCardBacks.value.contains(back.id) && back.id != "default"
        }
        
        if (profile.gems < 100 || _isGachaRunning.value || unownedCards.isEmpty()) return

        viewModelScope.launch {
            // 젬 차감 기록냥!
            userRepository.updateGems(profile.id, profile.gems - 100)
            
            _isGachaRunning.value = true
            _showGachaOverlay.value = true
            _lastGachaResult.value = null

            delay(3000) // 연출 시간냥!

            val result = unownedCards.random()
            userRepository.addCardBack(profile.id, result.id) // 서버에 저장냥!
            
            _lastGachaResult.value = result
            _isGachaRunning.value = false
        }
    }

    // 👕 새로운 옷 장착하기냥!
    fun equipCardBack(id: String) {
        val profile = userProfile.value ?: return
        val ownedIds = userRepository.ownedCardBacks.value
        if (ownedIds.contains(id) || id == "default") {
            viewModelScope.launch {
                userRepository.updateEquippedCat(profile.id, id)
            }
        }
    }

    fun logout() {
        UserApiClient.instance.logout { 
            viewModelScope.launch { _logoutEvent.emit(Unit) }
        }
    }

    fun dismissGacha() {
        _showGachaOverlay.value = false
        _lastGachaResult.value = null
    }
}
