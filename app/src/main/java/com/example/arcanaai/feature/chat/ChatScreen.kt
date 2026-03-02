package com.example.arcanaai.feature.chat

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.R
import com.example.arcanaai.core.designsystem.components.TarotCardView
import com.example.arcanaai.data.model.TarotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    topic: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCards by viewModel.selectedCards.collectAsState()
    
    // 🔍 카드 확대를 위한 상태냥!
    var zoomedCard by remember { mutableStateOf<TarotCard?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C29), Color(0xFF1A1A2E))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. 상단 바
            TopAppBar(
                title = { Text(text = "${topic}의 운명", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // 2. 고양이 마스터 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = viewModel.catImageRes),
                    contentDescription = viewModel.catName,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = if (uiState is TarotUiState.Picking) {
                            "반갑다냥! 네 고민을 생각하며\n카드 3장을 신중히 골라보라냥. (${selectedCards.size}/3)"
                        } else {
                            "운명의 결과가 나왔다냥!\n카드를 누르면 크게 볼 수 있다냥."
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 13.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // 3. 메인 콘텐츠
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TarotTransition",
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    is TarotUiState.Picking -> {
                        CardPickingLayout(
                            allCards = viewModel.allCards,
                            selectedCards = selectedCards,
                            onCardClick = { viewModel.onCardClick(it) },
                            onComplete = { viewModel.completeSelection() }
                        )
                    }
                    is TarotUiState.Result -> {
                        CardResultLayout(
                            selectedCards = state.selectedCards,
                            interpretation = state.interpretation,
                            onReset = { viewModel.reset() },
                            onCardZoom = { zoomedCard = it } // 👈 결과창에서 카드 클릭 시 호출냥!
                        )
                    }
                }
            }
        }

        // 🔍 카드 확대 다이얼로그 (전체 화면급)
        zoomedCard?.let { card ->
            CardZoomDialog(
                card = card,
                onDismiss = { zoomedCard = null }
            )
        }
    }
}

@Composable
fun CardPickingLayout(
    allCards: List<TarotCard>,
    selectedCards: List<TarotCard>,
    onCardClick: (TarotCard) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 선택된 카드들 미리보기 (뒷면)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(selectedCards) { card ->
                TarotCardView(
                    card = card,
                    isFlipped = false,
                    modifier = Modifier
                        .size(65.dp, 100.dp)
                        .padding(horizontal = 4.dp)
                        .clickable { onCardClick(card) } 
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 펼쳐진 카드들 (그리드)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(70.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allCards) { card ->
                val isSelected = selectedCards.any { it.id == card.id }
                TarotCardView(
                    isFlipped = false,
                    modifier = Modifier
                        .size(70.dp, 110.dp)
                        .clickable { onCardClick(card) }
                        .then(
                            if (isSelected) Modifier.background(Color.Yellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            else Modifier
                        )
                )
            }
        }

        // 선택 완료 버튼
        Button(
            onClick = onComplete,
            enabled = selectedCards.size == 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), disabledContainerColor = Color.Gray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("운명의 결과 확인하기", color = if (selectedCards.size == 3) Color.Black else Color.LightGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CardResultLayout(
    selectedCards: List<TarotCard>,
    interpretation: String,
    onReset: () -> Unit,
    onCardZoom: (TarotCard) -> Unit // 👈 줌 함수 추가냥!
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 결과창: 1:1 비율 카드들 (누르면 확대된다냥!)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCards.forEach { card ->
                TarotCardView(
                    card = card,
                    isFlipped = true,
                    useSquareRatio = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCardZoom(card) } // 👈 클릭 시 줌!
                )
            }
        }

        // 해석 텍스트 박스
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0x33FFFFFF),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = interpretation,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("다시 뽑아볼래냥", color = Color.White)
        }
    }
}

@Composable
fun CardZoomDialog(card: TarotCard, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // 닫기 버튼
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // 큰 카드 이미지 (본래 타로 비율 2:3 정도로 보여주기냥)
                TarotCardView(
                    card = card,
                    isFlipped = true,
                    useSquareRatio = false,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(0.625f) // 5:8 비율 정도냥
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = card.name,
                    color = Color(0xFFFFD700),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = card.keyword,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = card.description,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
