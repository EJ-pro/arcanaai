package com.example.arcanaai.feature.grimoire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.local.ChatDao
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val userRepository: UserRepository // 👈 유저 금고 주입냥!
) : ViewModel() {

    // 🔮 상단바에 필요한 유저 정보냥!
    val userName = userRepository.userProfile
        .map { it?.nickname ?: "집사" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "집사")

    val userGems = userRepository.userProfile
        .map { it?.gems ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 모든 채팅 기록을 실시간으로 가져온다냥!
    val chatHistory = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 젬 충전 기능냥!
    fun addGems(amount: Int) {
        val profile = userRepository.userProfile.value ?: return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems + amount)
        }
    }
    
    // 모든 기록 삭제 기능이다냥!
    fun clearAllHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }
}
