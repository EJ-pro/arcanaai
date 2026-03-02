package com.example.arcanaai.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    topic: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C29)) // 신비로운 심야 색상
    ) {
        // 상단 바
        TopAppBar(
            title = { Text("${topic} 상담소", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // 메시지 리스트
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // 입력창
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("마스터에게 물어보기...") },
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "..." else "전송")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val color = if (message.isFromUser) Color(0xFF6200EE) else Color(0xFF37474F)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}