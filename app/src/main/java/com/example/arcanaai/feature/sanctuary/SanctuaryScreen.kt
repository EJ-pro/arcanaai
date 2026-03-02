package com.example.arcanaai.feature.sanctuary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R // 리소스 임포트 필요
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.arcanaai.data.model.CharacterMood

// 상담 메뉴 데이터 모델
data class ConsultationTopic(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color
)

@Composable
fun SanctuaryScreen(
    onNavigateToChat: (String) -> Unit,
    viewModel: SanctuaryViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val catMessage by viewModel.catMessage.collectAsState()
    val mood by viewModel.characterMood.collectAsState()

    // 메뉴 리스트 정의
    val topics = listOf(
        ConsultationTopic("love", "연애 상담", "그 사람의 속마음은?", R.drawable.ic_heart, Color(0xFFFFB6C1)),
        ConsultationTopic("money", "금전/취업", "나의 재물운 흐름", R.drawable.ic_heart, Color(0xFFFFD700)),
        ConsultationTopic("choice", "양자택일", "A냐 B냐 그것이 문제", R.drawable.ic_heart, Color(0xFF87CEEB)),
        ConsultationTopic("free", "자유 상담", "무엇이든 물어보살", R.drawable.ic_heart, Color(0xFFE6E6FA))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // 배경 그라데이션 (밤하늘 느낌)
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
                )
            )
            .padding(16.dp)
    ) {

        // 1. 상단 헤더 (수정 개수)
        TopHeaderBar(userName = userName, gems = userGems)

        Spacer(modifier = Modifier.height(20.dp))


        Spacer(modifier = Modifier.height(30.dp))

        // 3. UI에 데이터 바인딩

        Spacer(modifier = Modifier.height(20.dp))

        // 고양이 섹션 (메시지와 기분 연동)
        CharacterHeroSection(
            message = catMessage,
            mood = mood,
            onCatClick = { viewModel.onCharacterTouched() }
        )

        // 4. 상담 메뉴 그리드
        Text(
            text = "어떤 고민이 있어?",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2열 그리드
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topics) { topic ->
                TopicCard(topic = topic, onClick = { onNavigateToChat(topic.id) })
            }
        }
    }
}

@Composable
fun TopHeaderBar(userName: String, gems: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Night 🌙", color = Color.Gray, fontSize = 12.sp)
            Text("Traveler님", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // 수정(Gem) 표시 칩
        Surface(
            color = Color(0x33000000),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("💎 300", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CharacterHeroSection(message: String, mood: CharacterMood, onCatClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 뒤에 은은한 후광 효과 (Glow)
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color(0x339C27B0), shape = androidx.compose.foundation.shape.CircleShape)
        )

        // 캐릭터 이미지 (아까 만든 CharacterView 재사용 가능)
        Image(
            painter = painterResource(id = R.drawable.char_cat_default), // 리소스 필요
            contentDescription = "Arcana",
            modifier = Modifier.size(180.dp)
        )

        // 말풍선 (캐릭터 옆에 띄우기)
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 10.dp)
        ) {
            Text(
                text = "오늘 기운이 좋은걸? ✨",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun DailyTarotCard() {
    // "오늘의 카드 뽑기" 버튼 역할
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E4A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { /* 오늘의 운세 팝업 띄우기 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Daily Tarot", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("오늘의 운세 확인하기", color = Color.White, fontSize = 16.sp)
            }
            // 카드 뒷면 아이콘
            Icon(
                painter = painterResource(id = R.drawable.img_card_back), // 아이콘 필요
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
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
            .aspectRatio(1f) // 정사각형 비율
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 아이콘
            Icon(
                painter = painterResource(id = topic.iconRes),
                contentDescription = null,
                tint = topic.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // 제목
            Text(text = topic.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            // 설명
            Text(text = topic.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
        }
    }
}