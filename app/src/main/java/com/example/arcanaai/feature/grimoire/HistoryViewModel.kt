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

    /**
     * DB의 모든 메시지를 관찰하되, UI에 맞게 변환(Mapping)합니다.
     * 채팅 내역 중 'relatedCardName'이 있는 메시지(AI의 타로 해석)만 역사로 취급합니다.
     */
    val historyList: StateFlow<List<HistoryUiModel>> = database.chatDao().getAllMessages()
        .map { messages ->
            messages
                .filter { it.relatedCardName != null } // 카드를 뽑았던 기록만 필터링
                .sortedByDescending { it.timestamp }   // 최신순 정렬
                .map { entity ->
                    entity.toUiModel()
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Room Entity -> UI Model 변환 함수
     */
    private fun ChatMessage.toUiModel(): HistoryUiModel {
        // 1. 요약 텍스트 생성 (너무 길면 자르기)
        val summaryText = if (this.content.length > 30) {
            this.content.take(30) + "..."
        } else {
            this.content
        }

        // 2. 주제 아이콘 결정 (임시 로직: 카드 이름에 따라 다르게)
        // 실제로는 저장할 때 주제(Topic)를 같이 저장하는 게 좋습니다.
        val icon = when {
            this.content.contains("사랑") || this.content.contains("연애") -> Icons.Default.Favorite
            this.content.contains("돈") || this.content.contains("취업") -> Icons.Default.Star
            else -> Icons.Default.ThumbUp
        }

        val topicTitle = when {
            this.content.contains("사랑") -> "연애 상담"
            this.content.contains("돈") -> "금전운"
            else -> "운명의 조언"
        }

        return HistoryUiModel(
            id = this.id.toString(),
            date = Date(this.timestamp),
            topic = topicTitle,
            cardName = this.relatedCardName ?: "Unknown Card",
            summary = summaryText,
            typeIcon = icon
        )
    }
}