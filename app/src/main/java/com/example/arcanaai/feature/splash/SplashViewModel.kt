package com.example.arcanaai.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.model.UserProfile
import com.example.arcanaai.data.repository.UserRepository
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded = _isDataLoaded.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin = _navigateToLogin.asStateFlow()

    init {
        checkLoginAndFetchData()
    }

    private fun checkLoginAndFetchData() {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                viewModelScope.launch {
                    // 1️⃣ 데이터 로딩 시작냥!
                    userRepository.refreshUserData(user.id.toString())
                    
                    // 2️⃣ 프로필 없으면 생성냥!
                    if (userRepository.userProfile.value == null) {
                        val newProfile = UserProfile(
                            id = user.id.toString(),
                            nickname = user.kakaoAccount?.profile?.nickname ?: "집사"
                        )
                        userRepository.createProfile(newProfile)
                    }
                    _isDataLoaded.value = true
                }
            } else {
                _navigateToLogin.value = true
            }
        }
    }
}
