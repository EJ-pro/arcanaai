package com.example.arcanaai.feature.altar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcanaai.R

// 테마 컬러 (나중에 Theme.kt에서 가져와도 됩니다)
private val MysticDark = Color(0xFF1A1A2E)
private val CardBg = Color(0xFF2E2E4A)
private val Gold = Color(0xFFFFD700)
private val TextWhite = Color(0xFFEEEEEE)
private val TextGray = Color(0xFFAAAAAA)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 스위치 상태 관리 (실제로는 ViewModel이나 DataStore에 연결해야 함)
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 1. 헤더 타이틀
        Text(
            text = "The Altar",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 2. 프로필 카드 (영혼 정보)
        ProfileCard()

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 상점 섹션 (공물 바치기)
        GemShopSection()

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 설정 리스트 (의식 준비)
        SectionTitle("Rituals (설정)")

        SettingsSwitchItem(
            title = "일일 예언 알림",
            subtitle = "매일 아침 8시, 운명의 메시지를 받습니다.",
            icon = Icons.Default.Notifications,
            checked = isNotificationEnabled,
            onCheckedChange = { isNotificationEnabled = it }
        )

        SettingsSwitchItem(
            title = "신비로운 소리",
            subtitle = "배경음악과 효과음을 켭니다.",
            icon = Icons.Default.Star, // 적절한 아이콘으로 변경 가능
            checked = isSoundEnabled,
            onCheckedChange = { isSoundEnabled = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. 데이터 관리 (금기)
        SectionTitle("Forbidden (관리)")

        SettingsActionItem(
            title = "운명 기록 지우기",
            subtitle = "모든 상담 내역이 영원히 사라집니다.",
            icon = Icons.Default.Refresh,
            isDestructive = true,
            onClick = {
                Toast.makeText(context, "기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                // TODO: Room DB Clear 로직 연결
            }
        )

        SettingsActionItem(
            title = "앱 버전 정보",
            subtitle = "v1.0.0 (Arcana Core)",
            icon = Icons.Default.Settings,
            onClick = { /* 버전 정보 팝업 */ }
        )

        Spacer(modifier = Modifier.height(50.dp)) // 하단 여백
    }
}

@Composable
fun ProfileCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지 (임시)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // 이미지 없을 때 회색 배경
                    .border(2.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 실제 이미지가 있다면 여기에 Image 컴포넌트 사용
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MysticDark,
                    modifier = Modifier.size(40.dp)
                )
                // Image(painter = painterResource(id = R.drawable.char_cat_default), contentDescription = null)
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
fun GemShopSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // 금색 테두리
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4B0082), Color(0xFF1A1A2E))
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("보유한 수정", color = TextGray, fontSize = 12.sp)
                Text("💎 300", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { /* 결제 로직 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("충전하기", color = MysticDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextGray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
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
                uncheckedTrackColor = MysticDark
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
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray
        )
    }
}