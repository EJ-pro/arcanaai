package com.example.arcanaai.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var loadingProgress by remember { mutableIntStateOf(0) }
    val isDataLoaded by viewModel.isDataLoaded.collectAsState()
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    // 로고 깜빡임 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        // 1️⃣ 0% -> 90% 까지 천천히 진행냥! (데이터 로딩 시간 확보)
        for (i in 1..90) {
            delay(60) 
            loadingProgress = i
        }
        
        // 2️⃣ 데이터 로딩이 완료될 때까지 대기냥!
        while (!isDataLoaded && !navigateToLogin) {
            delay(100)
        }

        // 3️⃣ 로딩 결과에 따른 이동냥!
        if (navigateToLogin) {
            onNavigateToLogin()
        } else {
            // 100%까지 빠르게 마무리냥!
            for (i in 91..100) {
                delay(20)
                loadingProgress = i
            }
            delay(500)
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C29)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.arcanaai_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp).alpha(logoAlpha)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("ARCANA AI", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Text("운명을 읽는 고양이", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(60.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (navigateToLogin) "접속 중이다냥..." else "운명의 기운을 모으는 중... $loadingProgress%",
                    color = Color(0xFFFFD700).copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).clip(CircleShape),
                    color = Color(0xFFFFD700),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}
