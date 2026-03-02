package com.example.arcanaai.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    topic: String,
    onNavigateBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    // 임시 메시지 리스트 (나중에 ViewModel과 연결)
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$topic 상담실", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0C29)),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("◀", color = Color.White) // 뒤로가기 아이콘 대신 임시
                    }
                }
            )
        },
        bottomBar = {
            // 메시지 입력창
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding(), // 키보드 올라올 때 같이 올라오도록
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("고양이 마스터에게 물어보라냥...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        messages.add(inputText to true) // 내 메시지 추가
                        inputText = ""
                        // TODO: 여기에 Gemini API 호출 로직 추가
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "전송", tint = Color(0xFFFFD700))
                }
            }
        },
        containerColor = Color(0xFF1A1A2E)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { (text, isMe) ->
                ChatBubble(text = text, isMe = isMe)
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isMe: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (isMe) Color(0xFF3D3D70) else Color(0xFF2E2E4E),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}