package com.example.arcanaai.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcanaai.R
import com.example.arcanaai.data.model.TarotCard
import com.example.arcanaai.core.designsystem.Gold

@Composable
fun TarotCardView(
    modifier: Modifier = Modifier,
    card: TarotCard? = null,
    isFlipped: Boolean = false,
    useSquareRatio: Boolean = false
) {
    val baseModifier = if (useSquareRatio) {
        modifier.aspectRatio(1f)
    } else {
        modifier.width(100.dp).height(160.dp)
    }

    Box(
        modifier = baseModifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFlipped) Color.White else Color.DarkGray)
            .border(2.dp, Gold, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFlipped && card != null) {
            if (card.imageRes != 0) {
                Image(
                    painter = painterResource(id = card.imageRes),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = card.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_card_back),
                contentDescription = "Tarot Card Back",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (!useSquareRatio) {
                Text(
                    text = "ARCANA",
                    color = Gold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
