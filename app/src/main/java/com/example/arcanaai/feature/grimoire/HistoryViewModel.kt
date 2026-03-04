package com.example.arcanaai.feature.grimoire

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcanaai.data.local.AppDatabase
import com.example.arcanaai.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    val historyList: StateFlow<List<HistoryUiModel>> = database.chatDao().getAllMessages()
        .map { messages ->
            messages
                .filter { it.relatedCardName != null }
                .sortedByDescending { it.timestamp }
                .map { entity ->
                    entity.toUiModel()
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun ChatMessage.toUiModel(): HistoryUiModel {
        val summaryText = if (this.content.length > 40) {
            this.content.take(40).replace("\n", " ") + "..."
        } else {
            this.content.replace("\n", " ")
        }

        val icon = when {
            this.topic.contains("연애") -> Icons.Default.Favorite
            this.topic.contains("금전") -> Icons.Default.Star
            else -> Icons.Default.ThumbUp
        }

        return HistoryUiModel(
            id = this.id.toString(),
            date = Date(this.timestamp),
            topic = this.topic,
            cardName = this.relatedCardName ?: "알 수 없는 운명",
            summary = summaryText,
            fullContent = this.content, // 👈 전체 내용 전달냥!
            typeIcon = icon
        )
    }
}
