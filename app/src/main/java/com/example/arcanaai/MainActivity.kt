package com.example.arcanaai

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.arcanaai.core.designsystem.ArcanaTheme
import com.example.arcanaai.feature.altar.SettingsScreen
import com.example.arcanaai.feature.chat.ChatScreen
import com.example.arcanaai.feature.gallery.GalleryScreen
import com.example.arcanaai.feature.grimoire.HistoryScreen
import com.example.arcanaai.feature.sanctuary.SanctuaryScreen
import com.example.arcanaai.navigation.BottomNavItem
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArcanaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
        try {
            val info = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "해시를 가져올 수 없음", e)
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarItems = listOf(
        BottomNavItem.Sanctuary,
        BottomNavItem.Grimoire,
        BottomNavItem.Gallery,
        BottomNavItem.Altar
    )
    val showBottomBar = bottomBarItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ArcanaBottomBar(navController, currentRoute, bottomBarItems)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // [수정] 내부에 정의된 함수가 아니라 navigation 패키지의 함수를 호출해야 함
            com.example.arcanaai.navigation.AppNavigationGraph(navController = navController)
        }
    }
}

@Composable
fun ArcanaBottomBar(
    navController: NavHostController, 
    currentRoute: String?,
    items: List<BottomNavItem>
) {
    NavigationBar(
        containerColor = Color(0xFF0F0C29),
        contentColor = Color(0xFFFFD700),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = if (isSelected) Modifier.size(26.dp) else Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(text = item.title, style = MaterialTheme.typography.labelSmall)
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF31315F),
                    selectedIconColor = Color(0xFFFFD700),
                    selectedTextColor = Color(0xFFFFD700),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}
