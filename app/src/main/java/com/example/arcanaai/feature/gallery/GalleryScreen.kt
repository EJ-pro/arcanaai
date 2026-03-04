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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.feature.altar.SectionTitle

private val MysticDark = Color(0xFF0F0C29)
private val CardBg = Color(0xFF1A1A2E)
private val Gold = Color(0xFFFFD700)
private val LockedGray = Color(0xFF2A2A3E)

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
    val cardBacks by viewModel.cardBacks.collectAsState()
    val equippedId by viewModel.equippedBackId.collectAsState()
    
    val ownedCount = cardBacks.count { it.isOwned }
    val totalCount = cardBacks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDark)
            .padding(16.dp)
    ) {
        Text(text = "Collection", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(0.2f), RoundedCornerShape(20.dp))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("카드 뒷면 수집도", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("$ownedCount / $totalCount 획득함", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                CircularProgressIndicator(progress = { ownedCount.toFloat() / totalCount }, color = Gold, strokeWidth = 6.dp, modifier = Modifier.size(52.dp), trackColor = Color.White.copy(0.1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("뒷면 갤러리 (장착하려면 누르라냥!)")
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(cardBacks) { item ->
                GalleryCardItem(
                    item = item, 
                    isEquipped = item.id == equippedId,
                    onEquip = { viewModel.equipCardBack(it) }
                )
            }
        }
    }
}

@Composable
fun GalleryCardItem(item: CardBack, isEquipped: Boolean, onEquip: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = item.isOwned) { onEquip(item.id) }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (item.isOwned) Color.Transparent else LockedGray)
                .border(
                    width = if (isEquipped) 3.dp else 1.dp,
                    color = if (isEquipped) Gold else if (item.isOwned) Gold.copy(0.4f) else Color.White.copy(0.1f), 
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isOwned) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isEquipped) {
                    Box(modifier = Modifier.fillMaxSize().background(Gold.copy(alpha = 0.1f)))
                }
            } else {
                // 잠긴 카드는 어둡게 처리냥!
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(0.2f), modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = if (isEquipped) "장착됨 ✨" else if (item.isOwned) item.name else "???",
            color = if (isEquipped) Gold else if (item.isOwned) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isEquipped) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
