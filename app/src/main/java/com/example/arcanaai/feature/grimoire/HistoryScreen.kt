package com.example.arcanaai.feature.grimoire

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.feature.altar.GemPurchaseDialog
import com.example.arcanaai.feature.sanctuary.TopHeaderBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    
    val context = LocalContext.current
    var showGemPurchaseDialog by remember { mutableStateOf(false) }

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
            // 성전 양식에 맞춰 16.dp 패딩 적용냥!
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TopHeaderBar(
                    userName = userName,
                    gems = userGems,
                    onGemClick = { showGemPurchaseDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 페이지 타이틀도 성전 타이틀 양식(start 8dp padding 추가)에 맞춘다냥!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "운명의 마도서",
                    color = Color.White,
                    fontSize = 18.sp, // 성전 타이틀 크기에 맞춤냥!
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = { viewModel.clearAllHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "전체 삭제", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chatHistory.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("아직 기록된 운명이 없다냥...", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp), // 성전과 동일한 패딩냥!
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatHistory.reversed()) { message ->
                        HistoryItem(message)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(message: ChatMessage) {
    val date = remember(message.timestamp) {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF22223B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = message.topic, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = date, color = Color.Gray, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.content,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 3
            )
            message.relatedCardName?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "🎴 관련 카드: $it", color = Color(0xFF87CEEB), fontSize = 11.sp)
            }
        }
    }
}
