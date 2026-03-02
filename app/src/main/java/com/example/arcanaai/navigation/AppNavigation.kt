package com.example.arcanaai.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.arcanaai.feature.sanctuary.SanctuaryScreen
import com.example.arcanaai.feature.grimoire.HistoryScreen
import com.example.arcanaai.feature.gallery.GalleryScreen
import com.example.arcanaai.feature.altar.SettingsScreen
import com.example.arcanaai.feature.auth.LoginScreen
import com.example.arcanaai.feature.splash.SplashScreen


@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        // 앱 시작 시 첫 화면: 성전(로비)
        startDestination = Routes.SPLASH,
        // 화면 전환 애니메이션 (부드러운 페이드 효과)
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        // 1. 스플래시 화면
        composable(Routes.SPLASH) {
            SplashScreen(onNavigateToMain = { // 👈 이름을 onNavigateToMain으로 수정!
                // 카카오 로그인 세션 확인 로직
                com.kakao.sdk.user.UserApiClient.instance.me { user, error ->
                    if (user != null) {
                        navController.navigate(BottomNavItem.Sanctuary.route) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            })
        }

        // 2. 로그인 화면
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(BottomNavItem.Sanctuary.route) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        // [Tab 1] 성전: 메인 로비 화면
        composable(BottomNavItem.Sanctuary.route) {
            SanctuaryScreen(
                onNavigateToChat = { topic ->
                    // 선택한 주제(love, money 등)를 경로에 담아 이동
                    navController.navigate("chat/$topic")
                }
            )
        }

        // [Tab 2] 마도서: 상담 기록 화면
        composable(BottomNavItem.Grimoire.route) {
            HistoryScreen()
        }

        // [Tab 3] 도감: 타로 카드 컬렉션 화면
        composable(BottomNavItem.Gallery.route) {
            GalleryScreen()
        }

        // [Tab 4] 제단: 설정 및 프로필 화면
        composable(BottomNavItem.Altar.route) {
            SettingsScreen()
        }

        // [Detail Screen] 상담 채팅방
        // "chat/{topic}" 형태의 경로를 통해 어떤 상담인지 구분함
        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("topic") {
                    type = NavType.StringType
                    defaultValue = "free"
                }
            )
        ) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: "free"

            // 👈 임시 화면을 지우고 실제 ChatScreen을 연결합니다.
            com.example.arcanaai.feature.chat.ChatScreen(
                topic = topic,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // [Detail Screen] 운명의 방 (카드 뽑기)
        composable(Routes.SHUFFLE) {
            // TODO: ShuffleScreen 구현 시 교체
            PlaceholderScreen("카드를 섞는 중입니다...")
        }
    }
}

/**
 * 아직 구현되지 않은 화면을 위한 임시 뷰
 */
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
