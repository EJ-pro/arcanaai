package com.example.arcanaai.feature.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcanaai.R

// 테마 컬러
private val MysticDark = Color(0xFF1A1A2E)
private val Gold = Color(0xFFFFD700)
private val LockedGray = Color(0xFF4A4A6A)

// UI 테스트용 더미 데이터 클래스
data class GalleryItem(
    val id: Int,
    val name: String,
    val isCollected: Boolean, // 수집 여부
    val imageRes: Int = R.drawable.ic_launcher_foreground // 임시 이미지
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    // 실제로는 ViewModel에서 DB 데이터를 가져와야 함
    // 여기서는 UI 확인을 위해 더미 데이터 생성 (78장)
    val galleryItems = remember {
        List(78) { index ->
            GalleryItem(
                id = index,
                name = if (index < 22) "Major $index" else "Minor $index",
                isCollected = index % 3 != 0 // 3장에 1장은 잠겨있도록 설정 (테스트용)
            )
        }
    }

    val collectedCount = galleryItems.count { it.isCollected }
    val progress = collectedCount / 78f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDark)
            .padding(16.dp)
    ) {
        // 1. 헤더 (타이틀 + 진행률)
        Text(
            text = "Tarot Gallery",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 수집 진행률 바
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E4A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Collection Progress", color = Gold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$collectedCount / 78 Cards",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // 원형 진행률 표시
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(50.dp),
                        color = Gold,
                        trackColor = Color.Black.copy(alpha = 0.3f),
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. 검색 및 필터 (수정됨)
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search card...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gold) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),

            // [변경] 최신 문법 적용
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Gold,
                focusedLabelColor = Gold,
                unfocusedLabelColor = Color.Gray,
                // 배경색을 투명하게 하려면
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),

            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 3. 카드 그리드 (3열)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 한 줄에 3개
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // 하단 탭 가리지 않게 여백
        ) {
            items(galleryItems) { item ->
                TarotCardItem(item)
            }
        }
    }
}

@Composable
fun TarotCardItem(item: GalleryItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 이미지 영역
        Box(
            modifier = Modifier
                .aspectRatio(0.6f) // 타로 카드 비율 (약 2:3)
                .clip(RoundedCornerShape(8.dp))
                .background(if (item.isCollected) Color.Transparent else LockedGray)
                .border(
                    width = 1.dp,
                    color = if (item.isCollected) Gold else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    if (item.isCollected) {
                        // TODO: 카드 상세 팝업 띄우기
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (item.isCollected) {
                // 수집된 카드: 컬러 이미지 표시
                Image(
                    painter = painterResource(id = R.drawable.img_card_back), // 샘플 이미지
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 빛나는 효과 (Overlay)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )
            } else {
                // 미수집 카드: 자물쇠 아이콘
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 카드 이름
        Text(
            text = if (item.isCollected) item.name else "???",
            color = if (item.isCollected) Gold else Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            fontWeight = if (item.isCollected) FontWeight.Bold else FontWeight.Normal
        )
    }
}