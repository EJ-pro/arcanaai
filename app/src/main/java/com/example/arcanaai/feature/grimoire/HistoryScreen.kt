package com.example.arcanaai.feature.grimoire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.PlaceholderScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MysticDark = Color(0xFF0F0C29)
private val CardBg = Color(0xFF1A1A2E)
private val Gold = Color(0xFFFFD700)
private val TextGray = Color(0xFFAAAAAA)

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val historyList by viewModel.historyList.collectAsState()
    
    // 🔍 상세 보기를 위한 상태냥!
    var selectedItem by remember { mutableStateOf<HistoryUiModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDark)
            .padding(16.dp)
    ) {
        Text(text = "My Grimoire", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = "당신의 운명이 기록된 마도서입니다.", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 20.dp))

        if (historyList.isEmpty()) {
            EmptyHistoryView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(historyList) { item ->
                    // 👈 'it' 대신 'item'을 사용하도록 수정했다냥!
                    HistoryItemCard(item = item, onClick = { selectedItem = item })
                }
            }
        }
    }

    // 📖 상세 보기 다이얼로그냥!
    selectedItem?.let { item ->
        HistoryDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}

@Composable
fun HistoryItemCard(item: HistoryUiModel, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4B0082), Color(0xFF191970)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.typeIcon, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.topic, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dateFormat.format(item.date), color = TextGray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.cardName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.summary, color = TextGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Detail", tint = TextGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun HistoryDetailDialog(item: HistoryUiModel, onDismiss: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(24.dp)),
            color = CardBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 닫기 바
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("운명의 기록", color = Gold, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = item.topic, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = dateFormat.format(item.date), color = TextGray, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 뽑았던 카드 정보냥!
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("선택했던 카드들냥", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = item.cardName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 해석 전문냥!
                    Text(
                        text = item.fullContent,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "아직 기록된 운명이 없다냥.\n첫 상담을 시작해봐!", color = TextGray, fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }
}

data class HistoryUiModel(
    val id: String,
    val date: Date,
    val topic: String,
    val cardName: String,
    val summary: String,
    val fullContent: String,
    val typeIcon: ImageVector
)
