package com.example.arcanaai.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import com.example.arcanaai.data.model.CatMaster
import kotlinx.coroutines.delay

private val Gold = Color(0xFFFFD700)

private val RainbowColors = listOf(
    Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
    Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082), Color(0xFF9400D3)
)

@Composable
fun GachaFullScreenOverlay(
    isRunning: Boolean, 
    gachaType: String?,
    result: Any?,
    allMasters: List<CatMaster>, // 👈 마스터 리스트를 받아온다냥!
    onDismiss: () -> Unit
) {
    if (!isRunning && result == null) return

    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            val isMasterGacha = gachaType == "master"

            if (isRunning) {
                // 🌀 뽑기 중: 슬롯머신 연출냥!
                RunningGachaAnimation(isMasterGacha, allMasters)
            } else if (result != null) {
                // ✨ 결과 등장냥!
                GachaResultView(result, onDismiss)
            }
        }
    }
}

@Composable
private fun RunningGachaAnimation(isMasterGacha: Boolean, allMasters: List<CatMaster>) {
    val infiniteTransition = rememberInfiniteTransition(label = "gacha_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing)),
        label = "rotation"
    )

    var slotIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(80) // 휙휙 지나가는 속도냥!
            slotIndex = (slotIndex + 1) % allMasters.size
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // 배경 오라
            Canvas(modifier = Modifier.size(350.dp).graphicsLayer { rotationZ = rotation }) {
                val brush = if (isMasterGacha) Brush.sweepGradient(RainbowColors + RainbowColors.first()) else Brush.radialGradient(listOf(Gold.copy(0.5f), Color.Transparent))
                drawCircle(brush = brush, radius = size.minDimension / 2, alpha = if (isMasterGacha) 0.7f else 1f)
            }

            // 빠르게 지나가는 이미지
            if (isMasterGacha) {
                Image(
                    painter = painterResource(id = allMasters[slotIndex].imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .scale(0.9f + (rotation % 30 / 300f)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_card_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp, 240.dp)
                        .scale(0.9f + (rotation % 30 / 300f))
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = if (isMasterGacha) "차원의 너머에서 마스터를 소환 중..." else "운명의 실타래를 잣는 중...",
            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GachaResultView(result: Any, onDismiss: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (result is CatMaster) "마스터 강림! ✨" else "운명 획득! 🎴",
            color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        ResultRevealAnimation(result)

        Spacer(modifier = Modifier.height(32.dp))
        
        val name = if (result is CatMaster) result.name else (result as CardBack).name
        val grade = if (result is CatMaster) "NEW MASTER" else (result as CardBack).grade.name
        
        Text(name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(grade, color = Gold, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("운명 받아들이기", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultRevealAnimation(result: Any) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "flip"
    )

    LaunchedEffect(Unit) {
        delay(300)
        flipped = true
    }

    val isMaster = result is CatMaster
    Box(
        modifier = Modifier
            .size(width = if (isMaster) 220.dp else 180.dp, height = if (isMaster) 220.dp else 300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 15f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Image(
                painter = painterResource(id = R.drawable.img_card_back),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (isMaster) CircleShape else RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                if (result is CatMaster) {
                    Image(
                        painter = painterResource(id = result.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(4.dp, Gold, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (result is CardBack) {
                    Image(
                        painter = painterResource(id = result.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Gold, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
