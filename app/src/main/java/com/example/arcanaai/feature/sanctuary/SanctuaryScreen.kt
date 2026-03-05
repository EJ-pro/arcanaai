package com.example.arcanaai.feature.sanctuary

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.feature.altar.GemPurchaseDialog

data class ConsultationTopic(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
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
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val catMessage by viewModel.catMessage.collectAsState()

    val context = LocalContext.current
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

    val activeMaster = catMasters.find { it.id == activeCatId } ?: catMasters.firstOrNull()

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
            TopHeaderBar(
                userName = userName, 
                gems = userGems ?: 0, 
                onGemClick = { showGemPurchaseDialog = true }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally // 👈 전체 중앙 정렬냥!
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 💬 마스터의 한마디 (중앙 정렬 텍스트냥!)
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = catMessage,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            fontSize = 14.sp, 
                            color = Color.Black,
                            textAlign = TextAlign.Center // 👈 텍스트 내부 중앙 정렬냥!
                        )
                    }

                    // 🐱 마스터 원형 이미지 (중앙 배치냥!)
                    activeMaster?.let { master ->
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF31315F).copy(alpha = 0.5f))
                                .border(2.dp, Color(0xFFFFD700), CircleShape)
                                .clickable { viewModel.onCharacterTouched() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = master.imageRes),
                                contentDescription = master.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = master.name,
                            color = Color(0xFFFFD700),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center // 👈 이름 중앙 정렬냥!
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "신비로운 타로 카드 점",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 16.dp).align(Alignment.Start)
                    )
                }
            }

            val topics = listOf(
                ConsultationTopic("연애 상담", "연애 상담", "그 사람의 속마음은?", Icons.Default.Favorite, Color(0xFFFFB6C1)),
                ConsultationTopic("금전 & 취업", "금전/취업", "나의 재물운 흐름", Icons.Default.MonetizationOn, Color(0xFFFFD700)),
                ConsultationTopic("양자택일", "양자택일", "A냐 B냐 그것이 문제", Icons.Default.CompareArrows, Color(0xFF87CEEB)),
                ConsultationTopic("자유상담", "자유 상담", "타로 카드로 고민 해결", Icons.Default.SelfImprovement, Color(0xFFE6E6FA))
            )

            val chunkedTopics = topics.chunked(2)
            items(chunkedTopics) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { topic ->
                        Box(modifier = Modifier.weight(1f)) {
                            TopicCard(topic = topic, onClick = { onNavigateToChat(topic.id, activeCatId) })
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "마스터와 1:1 대화 (챗봇)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 16.dp).align(Alignment.Start)
                    )

                    activeMaster?.let { master ->
                        ChatbotButton(
                            catName = master.name,
                            onClick = { onNavigateToChat("chatbot", activeCatId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatbotButton(catName: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "${catName} 마스터와 1:1 대화",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "무엇이든 자유롭게 물어보라냥!",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
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
                imageVector = topic.icon,
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
