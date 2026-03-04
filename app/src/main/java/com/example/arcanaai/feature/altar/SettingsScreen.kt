package com.example.arcanaai.feature.altar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.feature.sanctuary.TopHeaderBar
import kotlinx.coroutines.flow.collectLatest

private val MysticDark = Color(0xFF0F0C29)
private val CardBg = Color(0xFF1A1A2E)
private val Gold = Color(0xFFFFD700)
private val TextWhite = Color(0xFFEEEEEE)
private val TextGray = Color(0xFFAAAAAA)

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: AltarViewModel = hiltViewModel()
) {
    val userGems by viewModel.userGems.collectAsState()
    
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scrollState = rememberScrollState()

    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collectLatest { onLogout() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MysticDark)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            TopHeaderBar(
                userName = "운명의 여행자", 
                gems = userGems,
                onGemClick = { showGemPurchaseDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "The Altar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            ProfileCard()
            
            Spacer(modifier = Modifier.height(24.dp))
            GemStatusCard(gems = userGems, onAddClick = { showGemPurchaseDialog = true })
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("Rituals (설정)")
            SettingsSwitchItem("일일 예언 알림", "매일 아침 8시, 운명의 메시지를 받습니다.", Icons.Default.Notifications, isNotificationEnabled) { isNotificationEnabled = it }
            SettingsSwitchItem("신비로운 소리", "배경음악과 효과음을 켭니다.", Icons.Default.Star, isSoundEnabled) { isSoundEnabled = it }
            
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Forbidden (관리)")
            SettingsActionItem("로그아웃", "현재 세션을 종료합니다.", Icons.AutoMirrored.Filled.ExitToApp) { viewModel.logout() }
            SettingsActionItem("운명 기록 지우기", "모든 상담 내역이 영원히 사라집니다.", Icons.Default.Refresh, true) { Toast.makeText(context, "기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show() }
            SettingsActionItem("앱 버전 정보", "v1.0.0 (Arcana Core)", Icons.Default.Settings) {}
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showGemPurchaseDialog) {
            GemPurchaseDialog(
                onDismiss = { showGemPurchaseDialog = false },
                onPurchase = { amount ->
                    if (activity != null) {
                        viewModel.startPurchase(activity, "gem_$amount", amount)
                    } else {
                        viewModel.addGems(amount)
                    }
                    showGemPurchaseDialog = false
                }
            )
        }
    }
}

@Composable
fun GemPurchaseDialog(onDismiss: () -> Unit, onPurchase: (Int) -> Unit) {
    val purchaseItems = listOf(
        Triple(100, "수정 묶음", "₩ 1,100"),
        Triple(300, "수정 주머니", "₩ 3,300"),
        Triple(500, "수정 상자", "₩ 5,500"),
        Triple(1000, "거대 수정", "₩ 11,000"),
        Triple(2500, "고대 보물", "₩ 25,000"),
        Triple(5000, "아르카나 정수", "₩ 49,000")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp)),
            color = CardBg
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "수정 충전소", 
                    color = Color.White, 
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    purchaseItems.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (amount, label, price) ->
                                PurchaseItemCard(
                                    amount = amount,
                                    label = label,
                                    price = price,
                                    onClick = { onPurchase(amount) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("다음에 하기", color = Color.Gray, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun PurchaseItemCard(
    amount: Int,
    label: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(Gold.copy(0.25f), Color.Transparent)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        amount >= 5000 -> "🌌"
                        amount >= 2500 -> "👑"
                        amount >= 1000 -> "🔮"
                        amount >= 500 -> "📦"
                        amount >= 300 -> "💰"
                        else -> "💎"
                    },
                    fontSize = 36.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            Text("$amount", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = Gold,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = price,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun GemStatusCard(gems: Int, onAddClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(0.3f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("보유한 수정", color = Color.Gray, fontSize = 12.sp)
                Text("💎 $gems", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.background(Gold, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Gems", tint = Color.Black)
            }
        }
    }
}

@Composable
fun ProfileCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(0.2f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .border(2.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "운명의 여행자",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lv. 1 Wanderer",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = TextWhite, fontSize = 16.sp)
                Text(text = subtitle, color = TextGray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Gold,
                checkedTrackColor = Gold.copy(alpha = 0.5f),
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFFF6B6B) else TextGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isDestructive) Color(0xFFFF6B6B) else TextWhite,
                fontSize = 16.sp
            )
            Text(text = subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}
