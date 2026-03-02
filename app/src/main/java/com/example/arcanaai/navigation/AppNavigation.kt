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
import com.example.arcanaai.feature.chat.ChatScreen

@Composable
fun AppNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onNavigateToMain = {
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

        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(BottomNavItem.Sanctuary.route) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(BottomNavItem.Sanctuary.route) {
            SanctuaryScreen(
                onNavigateToChat = { topic, catId ->
                    navController.navigate("chat/$topic/$catId")
                }
            )
        }

        composable(BottomNavItem.Grimoire.route) {
            HistoryScreen()
        }

        composable(BottomNavItem.Gallery.route) {
            GalleryScreen()
        }

        composable(BottomNavItem.Altar.route) {
            SettingsScreen()
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("topic") { type = NavType.StringType },
                navArgument("catId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topic = backStackEntry.arguments?.getString("topic") ?: "free"
            ChatScreen(
                topic = topic,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHUFFLE) {
            PlaceholderScreen("카드를 섞는 중입니다...")
        }
    }
}

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
