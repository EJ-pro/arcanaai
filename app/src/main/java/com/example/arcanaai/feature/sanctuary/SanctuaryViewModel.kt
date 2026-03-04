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

    // 🏰 전역 저장소의 상태를 그대로 가져와서 쓴다냥!
    val userName = userRepository.userProfile.map { it?.nickname ?: "Traveler" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Traveler")

    val userGems = userRepository.userProfile.map { it?.gems }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCatId = userRepository.userProfile.map { it?.equippedBackId ?: "arcana" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "arcana")

    private val _catMasters = MutableStateFlow(listOf(
        CatMaster("arcana", "아르카나", "신비로운 보랏빛 고양이", R.drawable.char_cat_default, listOf(Color(0xFF0F0C29), Color(0xFF302B63)), false),
        CatMaster("nero", "네로", "밤의 기운을 담은 검은 고양이", R.drawable.char_nero_default, listOf(Color(0xFF141E30), Color(0xFF243B55)), true),
        CatMaster("leo", "레오", "태양의 가호를 받는 황금 고양이", R.drawable.char_leo_default, listOf(Color(0xFFED8F03), Color(0xFFFFB75E)), true)
    ))
    
    // 해금 정보도 저장소와 연동냥!
    val catMasters = combine(_catMasters, userRepository.unlockedMasters) { masters, unlockedIds ->
        masters.map { it.copy(isLocked = !unlockedIds.contains(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _catMasters.value)

    private val _viewingCatIndex = MutableStateFlow(0)
    val viewingCatIndex = _viewingCatIndex.asStateFlow()

    private val _characterMood = MutableStateFlow(CharacterMood.DEFAULT)
    val characterMood = _characterMood.asStateFlow()

    private val _catMessage = MutableStateFlow("어서 와라냥. 운명의 소리가 들리니?")
    val catMessage = _catMessage.asStateFlow()

    init {
        // 앱 실행하자마자 데이터 가져오기냥!
        syncWithServer()
    }

    private fun syncWithServer() {
        UserApiClient.instance.me { user, error ->
            if (user != null) {
                viewModelScope.launch {
                    userRepository.refreshUserData(user.id.toString())
                    // 만약 프로필이 아예 없다면 생성냥
                    if (userRepository.userProfile.value == null) {
                        val newProfile = UserProfile(
                            id = user.id.toString(),
                            nickname = user.kakaoAccount?.profile?.nickname ?: "집사",
                            gems = 300
                        )
                        userRepository.createProfile(newProfile)
                    }
                }
            }
        }
    }

    fun onCatPagerChanged(index: Int) {
        _viewingCatIndex.value = index
        val cat = _catMasters.value[index]
        _catMessage.value = if (cat.isLocked) "잠들어 있는 마스터다냥." else "${cat.name}가 함께할 준비가 됐다냥."
    }

    fun selectCat(catId: String) {
        viewModelScope.launch {
            val userId = UserApiClient.instance.me { user, _ -> 
                user?.id?.let { id ->
                    viewModelScope.launch { userRepository.updateEquippedCat(id.toString(), catId) }
                }
            }
        }
    }

    fun addGems(amount: Int) {
        viewModelScope.launch {
            val profile = userRepository.userProfile.value ?: return@launch
            userRepository.updateGems(profile.id, profile.gems + amount)
        }
    }

    fun unlockCat(catId: String) {
        viewModelScope.launch {
            val profile = userRepository.userProfile.value ?: return@launch
            if (profile.gems >= 100) {
                userRepository.updateGems(profile.id, profile.gems - 100)
                userRepository.unlockMaster(profile.id, catId)
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
