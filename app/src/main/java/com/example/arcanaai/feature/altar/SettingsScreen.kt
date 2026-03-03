package com.example.arcanaai.feature.altar

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.feature.sanctuary.TopHeaderBar
import kotlinx.coroutines.delay

private val MysticDark = Color(0xFF0F0C29)
private val CardBg = Color(0xFF1A1A2E)
private val Gold = Color(0xFFFFD700)
private val TextWhite = Color(0xFFEEEEEE)
private val TextGray = Color(0xFFAAAAAA)

@Composable
fun SettingsScreen(viewModel: AltarViewModel = hiltViewModel()) {
    val userGems by viewModel.userGems.collectAsState()
    val cardBacks by viewModel.cardBacks.collectAsState()
    val equippedId by viewModel.equippedBackId.collectAsState()
    val isGachaRunning by viewModel.isGachaRunning.collectAsState()
    val lastResult by viewModel.lastGachaResult.collectAsState()
    val showOverlay by viewModel.showGachaOverlay.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

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
            
            GemStatusCard(
                gems = userGems, 
                onAddClick = { showGemPurchaseDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
            GachaTriggerSection(onDraw = { 
                if (userGems >= 100) showConfirmDialog = true 
                else Toast.makeText(context, "수정이 부족하다냥! 💎", Toast.LENGTH_SHORT).show()
            })

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("뒷면 보관함")
            CardBackInventory(cardBacks = cardBacks, equippedId = equippedId, onEquip = { viewModel.equipCardBack(it) })
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("Rituals (설정)")
            SettingsSwitchItem("일일 예언 알림", "매일 아침 8시, 운명의 메시지를 받습니다.", Icons.Default.Notifications, isNotificationEnabled) { isNotificationEnabled = it }
            SettingsSwitchItem("신비로운 소리", "배경음악과 효과음을 켭니다.", Icons.Default.Star, isSoundEnabled) { isSoundEnabled = it }
            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Forbidden (관리)")
            SettingsActionItem("운명 기록 지우기", "모든 상담 내역이 영원히 사라집니다.", Icons.Default.Refresh, true) { Toast.makeText(context, "기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show() }
            SettingsActionItem("앱 버전 정보", "v1.0.0 (Arcana Core)", Icons.Default.Settings) {}
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("운명의 계약", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("수정 100개를 사용하여 새로운 카드 뒷면을 뽑으시겠습니까?", color = Color.White) },
                containerColor = CardBg,
                confirmButton = {
                    TextButton(onClick = { showConfirmDialog = false; viewModel.drawCardBack() }) { Text("네, 뽑겠습니다냥", color = Gold, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("아니오", color = Color.Gray) }
                }
            )
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

        if (showOverlay) {
            GachaFullScreenOverlay(isRunning = isGachaRunning, result = lastResult, onDismiss = { viewModel.dismissGacha() })
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
                .clip(RoundedCornerShape(28.dp)),
            color = CardBg
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "수정 충전소", 
                    color = Color.White, 
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 3줄 2열 배치 (한 화면에 쏙 들어오게냥!)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    purchaseItems.chunked(2).forEach { rowItems ->
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

                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("다음에 하기", color = Color.Gray, fontSize = 14.sp)
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 보석 아이콘 (크게!)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(Gold.copy(0.2f), Color.Transparent)),
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
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text("$amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // 가격 태그
            Surface(
                color = Gold,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = price,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
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
fun GachaTriggerSection(onDraw: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDraw() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFF4B0082), Color(0xFF1A1A2E))))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("운명의 뒷면 뽑기", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("💎 100개 소모 (중복 없음!)", color = Gold.copy(0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GachaFullScreenOverlay(isRunning: Boolean, result: CardBack?, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
                label = "rotationZ"
            )

            if (isRunning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(300.dp).graphicsLayer { rotationZ = rotation }) {
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Gold.copy(0.5f), Color.Transparent)),
                                radius = size.minDimension / 2
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.img_card_back),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp, 240.dp).scale(0.8f + (rotation % 40 / 400f))
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("운명의 실타래를 잣는 중...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else if (result != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("새로운 운명을 마주하라!", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TarotCardResult(result)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(result.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(result.grade.name, color = Gold, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                    ) {
                        Text("획득하기", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TarotCardResult(card: CardBack) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "flip"
    )

    LaunchedEffect(Unit) {
        delay(500)
        flipped = true
    }

    Box(
        modifier = Modifier
            .size(180.dp, 300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Image(
                painter = painterResource(id = R.drawable.img_card_back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = card.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
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
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray).border(2.dp, Gold, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "운명의 여행자", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Lv. 1 Wanderer", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CardBackInventory(cardBacks: List<CardBack>, equippedId: String, onEquip: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cardBacks) { item ->
            val isEquipped = item.id == equippedId
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).alpha(if (item.isOwned) 1f else 0.4f).clickable(enabled = item.isOwned) { onEquip(item.id) }) {
                Box(modifier = Modifier.size(80.dp, 120.dp).clip(RoundedCornerShape(8.dp)).border(width = if (isEquipped) 3.dp else 1.dp, color = if (isEquipped) Gold else Color.Gray, shape = RoundedCornerShape(8.dp))) {
                    Image(painter = painterResource(id = item.imageRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    if (!item.isOwned) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                Text(text = if (isEquipped) "장착됨 ✨" else item.name, color = if (isEquipped) Gold else Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = TextWhite, fontSize = 16.sp)
                Text(text = subtitle, color = TextGray, fontSize = 12.sp)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = Gold.copy(alpha = 0.5f), uncheckedThumbColor = TextGray, uncheckedTrackColor = Color.DarkGray))
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String, icon: ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isDestructive) Color(0xFFFF6B6B) else TextGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = if (isDestructive) Color(0xFFFF6B6B) else TextWhite, fontSize = 16.sp)
            Text(text = subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp, top = 8.dp))
}
