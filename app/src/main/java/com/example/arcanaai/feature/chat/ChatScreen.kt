package com.example.arcanaai.feature.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
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
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.data.model.TarotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    topic: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedCards by viewModel.selectedCards.collectAsState()
    val equippedBackRes by viewModel.equippedBackRes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    
    var zoomedCard by remember { mutableStateOf<TarotCard?>(null) }

    val catImageRes = when (viewModel.catId) {
        "nero" -> R.drawable.char_nero_default
        "leo" -> R.drawable.char_leo_default
        else -> R.drawable.char_cat_default
    }

    Scaffold(
        containerColor = Color(0xFF0F0C29)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0C29), Color(0xFF1A1A2E))
                    )
                )
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TarotTransition"
            ) { state ->
                when (state) {
                    is TarotUiState.Chatting -> {
                        ChattingLayout(
                            messages = messages,
                            isLoading = isLoading,
                            catImageRes = catImageRes,
                            onSendMessage = { viewModel.sendMessage(it) }
                        )
                    }
                    is TarotUiState.Picking -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            HeaderInfo(catImageRes, viewModel.catName, "운명의 카드를 3장 골라보라냥.", imageSize = 150)
                            CardPickingLayout(
                                allCards = viewModel.allCards,
                                selectedCards = selectedCards,
                                onCardClick = { viewModel.onCardClick(it) },
                                onComplete = { viewModel.completeSelection() },
                                equippedBackRes = equippedBackRes
                            )
                        }
                    }
                    is TarotUiState.Loading -> {
                        LoadingLayout(
                            catImageRes = catImageRes,
                            progress = loadingProgress,
                            equippedBackRes = equippedBackRes
                        )
                    }
                    is TarotUiState.Result -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            HeaderInfo(catImageRes, viewModel.catName, "아르카나가 읽어낸 운명이다냥.", imageSize = 120)
                            CardResultLayout(
                                selectedCards = state.selectedCards,
                                interpretation = state.interpretation,
                                onReset = { viewModel.reset() },
                                onCardZoom = { zoomedCard = it }
                            )
                        }
                    }
                }
            }

            zoomedCard?.let { card ->
                CardZoomDialog(card = card, onDismiss = { zoomedCard = null })
            }
        }
    }
}

@Composable
fun LoadingLayout(catImageRes: Int, progress: Int, equippedBackRes: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = catImageRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp).graphicsLayer { rotationY = rotation }
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(80.dp, 130.dp)
                        .graphicsLayer {
                            translationY = if (index % 2 == 0) offsetY else -offsetY
                            rotationZ = if (index % 2 == 0) 5f else -5f
                        },
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = equippedBackRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "운명의 실타래를 푸는 중... $progress%",
            color = Color(0xFFFFD700),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFFFFD700),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun HeaderInfo(imageRes: Int, name: String, text: String, imageSize: Int = 80) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = name,
            modifier = Modifier.size(imageSize.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChattingLayout(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    catImageRes: Int,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message, catImageRes)
            }
            if (isLoading) {
                item {
                    Text(
                        "생각 중이다냥...", 
                        color = Color.Gray, 
                        fontSize = 12.sp, 
                        modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A2E),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("고민을 말해달라냥...", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x33FFFFFF),
                        unfocusedContainerColor = Color(0x1AFFFFFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFFFFD700), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, catImageRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Image(
                painter = painterResource(id = catImageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (message.isFromUser) Color(0xFF4A4E69) else Color(0xFF22223B),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 16.dp else 0.dp,
                topEnd = if (message.isFromUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CardPickingLayout(
    allCards: List<TarotCard>,
    selectedCards: List<TarotCard>,
    onCardClick: (TarotCard) -> Unit,
    onComplete: () -> Unit,
    equippedBackRes: Int
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(selectedCards) { card ->
                Image(
                    painter = painterResource(id = equippedBackRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCardClick(card) },
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allCards) { card ->
                val isSelected = selectedCards.any { it.id == card.id }
                Box(
                    modifier = Modifier
                        .aspectRatio(0.625f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCardClick(card) }
                        // 💡 선택된 카드에 하이라이트 효과 적용냥!
                        .then(
                            if (isSelected) Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = equippedBackRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 💡 선택되면 반투명하게 어둡게 하고 체크 표시냥!
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

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
    onCardZoom: (TarotCard) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCards.forEach { card ->
                TarotCardView(
                    card = card,
                    isFlipped = true,
                    useSquareRatio = false,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.625f)
                        .clickable { onCardZoom(card) }
                )
            }
        }

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
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TarotCardView(
                    card = card,
                    isFlipped = true,
                    useSquareRatio = false,
                    modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(0.625f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = card.name, color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = card.keyword, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = card.description, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
            }
        }
    }
}
