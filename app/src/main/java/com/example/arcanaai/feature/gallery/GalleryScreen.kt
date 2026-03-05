package com.example.arcanaai.feature.gallery

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.CardBackGrade
import com.example.arcanaai.data.model.CatMaster
import com.example.arcanaai.feature.altar.GemPurchaseDialog
import com.example.arcanaai.feature.sanctuary.TopHeaderBar

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val cardBacks by viewModel.cardBacks.collectAsState()
    val catMasters by viewModel.catMasters.collectAsState()
    val equippedMasterId by viewModel.equippedMasterId.collectAsState()
    val equippedBackId by viewModel.equippedBackId.collectAsState()
    val isGachaRunning by viewModel.isGachaRunning.collectAsState()
    val gachaResult by viewModel.gachaResult.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var showGemPurchaseDialog by remember { mutableStateOf(false) }
    var itemToEquip by remember { mutableStateOf<Any?>(null) }

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

    // 장착 확인 다이얼로그
    itemToEquip?.let { item ->
        val name = when(item) {
            is CatMaster -> item.name
            is CardBack -> item.name
            else -> ""
        }
        AlertDialog(
            onDismissRequest = { itemToEquip = null },
            title = { Text("운명 변경", color = Color.White) },
            text = { Text("$name (으)로 변경하시겠습니까?", color = Color.LightGray) },
            containerColor = Color(0xFF1A1A2E),
            confirmButton = {
                TextButton(onClick = {
                    if (item is CatMaster) viewModel.equipMaster(item.id)
                    else if (item is CardBack) viewModel.equipCardBack(item.id)
                    itemToEquip = null
                    Toast.makeText(context, "변경이 완료되었습니다! ✨", Toast.LENGTH_SHORT).show()
                }) { Text("네!", color = Color(0xFFFFD700)) }
            },
            dismissButton = {
                TextButton(onClick = { itemToEquip = null }) { Text("취소", color = Color.Gray) }
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
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TopHeaderBar(userName = userName, gems = userGems, onGemClick = { showGemPurchaseDialog = true })
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "신비로운 도감", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFD700),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFFFFD700))
                },
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("마스터", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("카드 뒷면", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    MasterGallery(catMasters, equippedMasterId, { if (!it.isLocked) itemToEquip = it }, { viewModel.drawMaster() }, userGems >= 300)
                } else {
                    CardBackGallery(cardBacks, equippedBackId, { if (it.isOwned) itemToEquip = it }, { viewModel.drawCardBack() }, userGems >= 300)
                }
            }
        }

        if (isGachaRunning || gachaResult != null) {
            GachaOverlay(isRunning = isGachaRunning, result = gachaResult, onDismiss = { viewModel.dismissGacha() })
        }
    }
}

@Composable
fun MasterGallery(masters: List<CatMaster>, equippedId: String, onItemClick: (CatMaster) -> Unit, onGacha: () -> Unit, canGacha: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(masters) { master -> MasterItem(master, master.id == equippedId, { onItemClick(master) }) }
        }
        GachaButton("새로운 마스터 영접하기", onGacha, canGacha)
    }
}

@Composable
fun CardBackGallery(backs: List<CardBack>, equippedId: String, onItemClick: (CardBack) -> Unit, onGacha: () -> Unit, canGacha: Boolean) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(backs) { back -> CardBackGridItem(back, back.id == equippedId, { onItemClick(back) }) }
        }
        GachaButton("신비로운 뒷면 연성하기", onGacha, canGacha)
    }
}

@Composable
fun MasterItem(master: CatMaster, isEquipped: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(90.dp).clip(CircleShape).background(if (master.isLocked) Color.Black.copy(alpha = 0.6f) else Color(0xFF31315F))
                .border(if (isEquipped) 2.dp else 1.dp, if (isEquipped) Color(0xFFFFD700) else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = master.imageRes), contentDescription = null, modifier = Modifier.fillMaxSize().graphicsLayer(alpha = if (master.isLocked) 0.3f else 1f), contentScale = ContentScale.Crop)
            if (master.isLocked) Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            if (isEquipped) Box(modifier = Modifier.fillMaxSize().background(Color(0x4DFFD700)), contentAlignment = Alignment.Center) {
                Text("장착됨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = master.name, color = if (master.isLocked) Color.Gray else Color.White, fontSize = 12.sp)
    }
}

@Composable
fun CardBackGridItem(back: CardBack, isEquipped: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.aspectRatio(0.7f).clip(RoundedCornerShape(8.dp))
                .border(if (isEquipped) 2.dp else 1.dp, if (isEquipped) Color(0xFFFFD700) else Color.Gray, RoundedCornerShape(8.dp))
        ) {
            Image(painter = painterResource(id = back.imageRes), contentDescription = null, modifier = Modifier.fillMaxSize().graphicsLayer(alpha = if (!back.isOwned) 0.3f else 1f), contentScale = ContentScale.Crop)
            if (!back.isOwned) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.White.copy(alpha = 0.7f))
            if (isEquipped) Box(modifier = Modifier.fillMaxSize().background(Color(0x4DFFD700)), contentAlignment = Alignment.Center) {
                Text("장착됨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = back.name, color = if (back.isOwned) Color.White else Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun GachaButton(label: String, onGacha: () -> Unit, enabled: Boolean) {
    Button(onClick = onGacha, enabled = enabled, modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), disabledContainerColor = Color.Gray), shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text("$label (💎 300)", color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GachaOverlay(isRunning: Boolean, result: Any?, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable { if (!isRunning) onDismiss() }, contentAlignment = Alignment.Center) {
        if (isRunning) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                Text("차원의 문을 여는 중이다냥...", color = Color.White)
            }
        } else if (result != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✨ 획득 성공! ✨", color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                if (result is CatMaster) {
                    Image(painter = painterResource(id = result.imageRes), contentDescription = null, modifier = Modifier.size(200.dp).clip(CircleShape))
                    Text("${result.name} 마스터", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
                } else if (result is CardBack) {
                    Image(painter = painterResource(id = result.imageRes), contentDescription = null, modifier = Modifier.height(300.dp).clip(RoundedCornerShape(16.dp)))
                    Text(result.name, color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
                }
                Spacer(modifier = Modifier.height(48.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) { Text("확인", color = Color.Black) }
            }
        }
    }
}
