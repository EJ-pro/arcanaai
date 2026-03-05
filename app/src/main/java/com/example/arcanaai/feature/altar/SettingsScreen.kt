package com.example.arcanaai.feature.altar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.UserProfile
import com.example.arcanaai.feature.sanctuary.TopHeaderBar

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: AltarViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val cardBacks by viewModel.cardBacks.collectAsState()
    val equippedBackId by viewModel.equippedBackId.collectAsState()
    val isGachaRunning by viewModel.isGachaRunning.collectAsState()
    val showGachaOverlay by viewModel.showGachaOverlay.collectAsState()
    val lastGachaResult by viewModel.lastGachaResult.collectAsState()

    val context = LocalContext.current
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

    // 로그아웃 이벤트 감지냥!
    LaunchedEffect(viewModel.logoutEvent) {
        viewModel.logoutEvent.collect { onLogout() }
    }

    if (showGemPurchaseDialog) {
        GemPurchaseDialog(
            onDismiss = { showGemPurchaseDialog = false },
            onPurchase = { amount ->
                viewModel.addGems(amount)
                showGemPurchaseDialog = false
                Toast.makeText(context, "수정 $amount 개가 충전되었습니다냥! ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F0C29)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0C29), Color(0xFF1A1A2E))
                    )
                )
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1️⃣ 상단 유저 정보 (이름 & 젬)
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TopHeaderBar(
                    userName = userProfile?.nickname ?: "집사",
                    gems = userGems,
                    onGemClick = { showGemPurchaseDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 2️⃣ 프로필 & 레벨 섹션냥!
                item {
                    UserProfileSection(userProfile)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "신비로운 제단",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // 3️⃣ 카드 뒷면 가챠 섹션냥!
                item {
                    GachaSection(onDraw = { viewModel.drawCardBack() })
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "소유한 카드 뒷면",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // 4️⃣ 카드 리스트
                items(cardBacks.chunked(3)) { rowBacks ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBacks.forEach { back ->
                            CardBackItem(
                                back = back,
                                isEquipped = back.id == equippedBackId,
                                onClick = { viewModel.equipCardBack(back.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowBacks.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 5️⃣ 하단 메뉴
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    ListItem(
                        headlineContent = { Text("로그아웃", color = Color.LightGray) },
                        leadingContent = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.clickable { viewModel.logout() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        // 가챠 오버레이 연출냥!
        if (showGachaOverlay) {
            GachaOverlay(
                isRunning = isGachaRunning,
                result = lastGachaResult,
                onDismiss = { viewModel.dismissGacha() }
            )
        }
    }
}

@Composable
fun UserProfileSection(profile: UserProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지냥!
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF31315F))
                .border(2.dp, Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profile?.profileImage != null) {
                AsyncImage(
                    model = profile.profileImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 닉네임냥!
        Text(
            text = profile?.nickname ?: "아르카나 집사",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 레벨 & 경험치 바냥!
        val level = profile?.level ?: 1
        val exp = profile?.exp ?: 0
        val maxExp = profile?.maxExp ?: 100
        val progress = exp.toFloat() / maxExp

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Lv.$level", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "$exp / $maxExp", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFFFD700),
            trackColor = Color(0xFF31315F)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lv.5 마다 특별한 카드 뒷면을 선물한다냥! 🎁",
            color = Color(0xFF87CEEB),
            fontSize = 11.sp
        )
    }
}

@Composable
fun GachaSection(onDraw: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF31315F).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onDraw() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("새로운 카드 뒷면 연성하기", color = Color.White, fontWeight = FontWeight.Bold)
                Text("신비로운 힘으로 뒷면을 소환한다냥! (💎 100개)", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CardBackItem(
    back: CardBack,
    isEquipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = back.isOwned) { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isEquipped) 3.dp else 0.dp,
                    color = if (isEquipped) Color(0xFFFFD700) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                painter = painterResource(id = back.imageRes),
                contentDescription = back.name,
                modifier = Modifier.fillMaxSize().then(if (!back.isOwned) Modifier.background(Color.Black.copy(alpha = 0.6f)) else Modifier),
                contentScale = ContentScale.Crop
            )
            if (!back.isOwned) {
                Icon(
                    Icons.Default.Lock, 
                    contentDescription = null, 
                    modifier = Modifier.align(Alignment.Center).size(24.dp), 
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = back.name, color = if (back.isOwned) Color.White else Color.Gray, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun GachaOverlay(isRunning: Boolean, result: CardBack?, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { if (!isRunning) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        if (isRunning) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                Text("아르카나의 기운을 모으는 중...", color = Color.White)
            }
        } else if (result != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✨ 새로운 뒷면 획득! ✨", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = result.imageRes),
                    contentDescription = null,
                    modifier = Modifier.height(300.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = result.name, color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                    Text("고맙다냥!", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun GemPurchaseDialog(onDismiss: () -> Unit, onPurchase: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("수정 충전", color = Color.White) },
        text = {
            Column {
                Text("충전할 수정 개수를 선택해라냥!", color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                listOf(100, 500, 1000).forEach { amount ->
                    Button(
                        onClick = { onPurchase(amount) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF31315F))
                    ) {
                        Text("💎 $amount 개 충전")
                    }
                }
            }
        },
        containerColor = Color(0xFF1A1A2E),
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("닫기", color = Color.Gray) } }
    )
}
