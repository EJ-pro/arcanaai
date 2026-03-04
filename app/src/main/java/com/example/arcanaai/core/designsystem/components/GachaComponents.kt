package com.example.arcanaai.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arcanaai.R
import com.example.arcanaai.data.model.CardBack
import kotlinx.coroutines.delay

private val Gold = Color(0xFFFFD700)

@Composable
fun GachaFullScreenOverlay(isRunning: Boolean, result: CardBack?, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
                label = "rotationZ"
            )

            if (isRunning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(300.dp).graphicsLayer { rotationZ = rotation }) {
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Gold.copy(0.5f), Color.Transparent)),
                                radius = size.minDimension / 2
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.img_card_back),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp, 240.dp).scale(0.8f + (rotation % 40 / 400f))
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("운명의 실타래를 잣는 중...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else if (result != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("새로운 운명을 마주하라!", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TarotCardResultAnimation(result)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(result.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(result.grade.name, color = Gold, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                    ) {
                        Text("획득하기", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TarotCardResultAnimation(card: CardBack) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "flip"
    )

    LaunchedEffect(Unit) {
        delay(500)
        flipped = true
    }

    Box(
        modifier = Modifier
            .size(180.dp, 300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Image(
                painter = painterResource(id = R.drawable.img_card_back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = card.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
