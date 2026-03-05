package com.example.arcanaai.feature.grimoire

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arcanaai.data.model.ChatMessage
import com.example.arcanaai.feature.altar.GemPurchaseDialog
import com.example.arcanaai.feature.sanctuary.TopHeaderBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userGems by viewModel.userGems.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    
    val context = LocalContext.current
    var showGemPurchaseDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("기록 삭제", color = Color.White) },
            text = { Text("선택한 ${selectedIds.size}개의 기록을 정말 삭제하시겠습니까?", color = Color.LightGray) },
            containerColor = Color(0xFF1A1A2E),
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelectedMessages()
                    showDeleteConfirmDialog = false
                    Toast.makeText(context, "삭제가 완료되었습니다냥! 🧹", Toast.LENGTH_SHORT).show()
                }) { Text("삭제", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("취소", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F0C29)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectionMode) "${selectedIds.size}개 선택됨" else "운명의 마도서",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    Row {
                        if (isSelectionMode) {
                            // 💡 전체 선택 버튼냥!
                            IconButton(onClick = { 
                                viewModel.selectAll(chatHistory.map { it.id }) 
                            }) {
                                val isAllSelected = selectedIds.size == chatHistory.size && chatHistory.isNotEmpty()
                                Icon(
                                    Icons.Default.SelectAll, 
                                    contentDescription = "전체 선택", 
                                    tint = if (isAllSelected) Color(0xFFFFD700) else Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                                Icon(Icons.Default.Close, contentDescription = "취소", tint = Color.Gray)
                            }
                            IconButton(
                                onClick = { if (selectedIds.isNotEmpty()) showDeleteConfirmDialog = true },
                                enabled = selectedIds.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "삭제", tint = if (selectedIds.isNotEmpty()) Color.Red else Color.DarkGray)
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                                Icon(Icons.Default.Delete, contentDescription = "선택 모드", tint = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (chatHistory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("아직 기록된 운명이 없다냥...", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatHistory.reversed(), key = { it.id }) { message ->
                            HistoryItem(
                                message = message,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedIds.contains(message.id),
                                onToggleSelect = { viewModel.toggleMessageSelection(message.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    message: ChatMessage,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit
) {
    val date = remember(message.timestamp) {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF31315F) else Color(0xFF22223B).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isSelectionMode) onToggleSelect() },
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFD700))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = message.topic, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = date, color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 3
                )
                message.relatedCardName?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🎴 관련 카드: $it", color = Color(0xFF87CEEB), fontSize = 11.sp)
                }
            }
        }
    }
}
