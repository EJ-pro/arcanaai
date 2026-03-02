package com.example.arcanaai.feature.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.arcanaai.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C29)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 앱 로고나 고양이 아이콘
            Text("Arcana AI", color = Color(0xFFFFD700), modifier = Modifier.padding(bottom = 50.dp))

            // 카카오 로그인 버튼 (이미지 리소스가 없다면 배경색으로 대체)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .background(Color(0xFFFEE500), RoundedCornerShape(12.dp))
                    .clickable { handleKakaoLogin(context, onLoginSuccess) },
                contentAlignment = Alignment.Center
            ) {
                Text("카카오로 시작하기", color = Color.Black)
            }
        }
    }
}

private fun handleKakaoLogin(context: Context, onSuccess: () -> Unit) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("Kakao", "로그인 실패", error)
        } else if (token != null) {
            Log.i("Kakao", "로그인 성공 ${token.accessToken}")
            onSuccess()
        }
    }

    // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 계정 로그인
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            } else {
                callback(token, null)
            }
        }
    } else {
        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
    }
}