package com.example.arcanaai.feature.grimoire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // [체크 1] 필수 임포트
import androidx.compose.runtime.getValue     // [체크 2] 'by' 키워드 사용을 위해 필수
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // [체크 3] 필수 임포트
import com.example.arcanaai.PlaceholderScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 테마 컬러 정의 (Theme.kt와 동기화)
private val MysticDark = Color(0xFF1A1A2E)
private val CardBg = Color(0xFF2E2E4A)
private val Gold = Color(0xFFFFD700)
private val TextGray = Color(0xFFAAAAAA)

@Composable
fun HistoryScreen(
    // Hilt를 통해 뷰모델 주입
    viewModel: HistoryViewModel = hiltViewModel()
) {
    // StateFlow를 Compose 상태로 관찰 (by와 collectAsState 사용)
    val historyList by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDark)
            .padding(16.dp)
    ) {
        // 1. 헤더 영역
        Text(
            text = "My Grimoire",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "당신의 운명이 기록된 마도서입니다.",
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 2. 리스트 영역 (비어있을 때와 데이터가 있을 때 구분)
        if (historyList.isEmpty()) {
            PlaceholderScreen("아직 기록된 운명이 없다냥.\n첫 상담을 시작해봐!")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // 하단 탭 여백
            ) {
                items(historyList) { item ->
                    HistoryItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryUiModel) {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 상세 내용 보기 로직 */ }
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 박스 (그라데이션 적용)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF4B0082), Color(0xFF191970))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.typeIcon,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보 영역
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.topic,
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(item.date),
                        color = TextGray,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.cardName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.summary,
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 이동 화살표
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detail",
                tint = TextGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyHistoryView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "아직 기록된 운명이 없다냥.",
                color = TextGray,
                fontSize = 16.sp
            )
        }
    }
}
data class HistoryUiModel(
    val id: String,           // 각 기록의 고유 ID (Room DB의 id)
    val date: Date,           // 상담이 이루어진 날짜와 시간
    val topic: String,        // 상담 주제 (연애, 금전, 진로 등)
    val cardName: String,     // 뽑았던 타로 카드 이름 (예: The Lovers)
    val summary: String,      // AI가 해준 해석의 짧은 요약문
    val typeIcon: ImageVector // 주제에 따른 아이콘 (Heart, Star 등)
)