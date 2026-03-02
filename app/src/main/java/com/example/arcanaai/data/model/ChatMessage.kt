package com.example.arcanaai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val content: String,           // 메시지 내용 (사용자 질문 또는 AI 답변)

    val timestamp: Long = System.currentTimeMillis(), // 생성 시간 (밀리초)

    val isFromUser: Boolean,       // true: 사용자, false: AI(고양이)

    val topic: String = "일반",      // 상담 주제 (연애, 금전 등)

    val relatedCardName: String? = null, // 해석에 사용된 타로 카드 이름 (없으면 null)

    val characterMood: String = "NORMAL" // 고양이의 당시 감정 상태
)