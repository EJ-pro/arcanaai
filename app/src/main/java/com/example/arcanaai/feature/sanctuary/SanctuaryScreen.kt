package com.example.arcanaai.feature.sanctuary

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.feature.altar.GemPurchaseDialog
import kotlin.math.absoluteValue
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp

// 상담 메뉴 데이터 모델
data class ConsultationTopic(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color
)

@SuppressLint("RestrictedApi")
@Composable
fun SanctuaryScreen(
    onNavigateToChat: (String, String) -> Unit,
    viewModel: SanctuaryViewModel = hiltViewModel()
) {
    val catMasters by viewModel.catMasters.collectAsState()
    val activeCatId by viewModel.activeCatId.collectAsState()
    val pagerState = rememberPagerState(pageCount = { catMasters.size })
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val catMessage by viewModel.catMessage.collectAsState()

    val context = LocalContext.current
    var showUnlockDialog by remember { mutableStateOf(false) }
    var selectedCatToUnlock by remember { mutableStateOf<CatMaster?>(null) }
    
    // 💎 젬 충전 다이얼로그 상태냥!
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onCatPagerChanged(pagerState.currentPage)
    }

    if (showUnlockDialog && selectedCatToUnlock != null) {
        AlertDialog(
            onDismissRequest = { showUnlockDialog = false },
            title = { Text(text = "마스터 해금", color = Color.White) },
            text = { Text("${selectedCatToUnlock!!.name}를 해금하시겠습니까?\n(💎 100개가 소모됩니다)", color = Color.White) },
            containerColor = Color(0xFF1A1A2E),
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unlockCat(selectedCatToUnlock!!.id)
                    showUnlockDialog = false
                }) { Text("해금하기", color = Color(0xFFFFD700)) }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockDialog = false }) { Text("취소", color = Color.Gray) }
            }
        )
    }

    // 💎 메인 화면용 젬 충전 마법
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
                )
            )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            // 👈 젬 카드를 누르면 충전 창이 뜨게 연결했다냥!
            TopHeaderBar(
                userName = userName, 
                gems = userGems,
                onGemClick = { showGemPurchaseDialog = true }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = catMessage,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp, color = Color.Black
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        HorizontalPager(state = pagerState) { page ->
                            val cat = catMasters[page]
                            CatMasterCard(
                                cat = cat,
                                isLocked = cat.isLocked,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                        alpha = lerp(0.4f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                        scaleX = lerp(0.8f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                        scaleY = lerp(0.8f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val currentCat = catMasters[pagerState.currentPage]
                    val isSelected = activeCatId == currentCat.id

                    Button(
                        onClick = {
                            if (currentCat.isLocked) {
                                selectedCatToUnlock = currentCat
                                showUnlockDialog = true
                            } else if (!isSelected) {
                                viewModel.selectCat(currentCat.id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentCat.isLocked) Color(0xFF444444) 
                                            else if (isSelected) Color(0xFF2E7D32) 
                                            else Color(0xFFFFD700)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isSelected || currentCat.isLocked
                    ) {
                        if (currentCat.isLocked) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("💎 100개로 해금")
                        } else if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("선택됨 ✨")
                        } else {
                            Text("이 마스터 선택하기", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.padding(vertical = 12.dp)) {
                        repeat(catMasters.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color(0xFFFFD700) else Color.Gray
                            Box(modifier = Modifier.padding(3.dp).clip(CircleShape).background(color).size(8.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "어떤 고민이 있어?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
            }

            val topics = listOf(
                ConsultationTopic("연애 상담", "연애 상담", "그 사람의 속마음은?", R.drawable.ic_heart, Color(0xFFFFB6C1)),
                ConsultationTopic("금전 & 취업", "금전/취업", "나의 재물운 흐름", R.drawable.ic_heart, Color(0xFFFFD700)),
                ConsultationTopic("양자택일", "양자택일", "A냐 B냐 그것이 문제", R.drawable.ic_heart, Color(0xFF87CEEB)),
                ConsultationTopic("자유상담", "자유 상담", "무엇이든 물어보살", R.drawable.ic_heart, Color(0xFFE6E6FA))
            )

            val chunkedTopics = topics.chunked(2)
            items(chunkedTopics) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { topic ->
                        Box(modifier = Modifier.weight(1f)) {
                            TopicCard(topic = topic, onClick = { onNavigateToChat(topic.id, activeCatId) })
                        }
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TopHeaderBar(userName: String, gems: Int, onGemClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Night 🌙", color = Color.Gray, fontSize = 12.sp)
            Text("${userName}님", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // 💎 충전 가능한 젬 카드냥!
        Surface(
            color = Color(0x33000000),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
            modifier = Modifier.clickable { onGemClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💎 $gems", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ... CatMasterCard, TopicCard 등은 동일 (생략 가능)

@Composable
fun CatMasterCard(cat: CatMaster, isLocked: Boolean, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = cat.imageRes),
            contentDescription = cat.name,
            modifier = Modifier
                .fillMaxHeight()
                .then(if (isLocked) Modifier.blur(12.dp) else Modifier)
        )
    }
}

@Composable
fun TopicCard(topic: ConsultationTopic, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = topic.iconRes),
                contentDescription = null,
                tint = topic.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = topic.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = topic.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
        }
    }
}
