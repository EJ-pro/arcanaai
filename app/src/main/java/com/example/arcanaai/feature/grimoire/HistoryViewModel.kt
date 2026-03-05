package com.example.arcanaai.feature.grimoire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.local.ChatDao
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatDao: ChatDao,
    private val userRepository: UserRepository
) : ViewModel() {

    val userName = userRepository.userProfile
        .map { it?.nickname ?: "집사" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "집사")

    val userGems = userRepository.userProfile
        .map { it?.gems ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chatHistory = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 💡 선택 모드 및 선택된 ID 관리냥!
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) _selectedIds.value = emptySet()
    }

    fun toggleMessageSelection(id: Long) {
        val current = _selectedIds.value
        if (current.contains(id)) {
            _selectedIds.value = current - id
        } else {
            _selectedIds.value = current + id
        }
    }

    // 💡 전체 선택/해제 기능 추가냥!
    fun selectAll(ids: List<Long>) {
        if (_selectedIds.value.size == ids.size) {
            _selectedIds.value = emptySet()
        } else {
            _selectedIds.value = ids.toSet()
        }
    }

    fun deleteSelectedMessages() {
        viewModelScope.launch {
            chatDao.deleteMessagesByIds(_selectedIds.value.toList())
            _selectedIds.value = emptySet()
            _isSelectionMode.value = false
        }
    }

    fun addGems(amount: Int) {
        val profile = userRepository.userProfile.value ?: return
        viewModelScope.launch {
            userRepository.updateGems(profile.id, profile.gems + amount)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }
}
