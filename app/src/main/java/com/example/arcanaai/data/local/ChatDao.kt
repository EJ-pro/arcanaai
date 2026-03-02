package com.example.arcanaai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arcanaai.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // 모든 채팅 내역 가져오기 (시간순 정렬) - 실시간 감지(Flow)
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    // 메시지 저장하기
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    // 기록 초기화 (설정 화면에서 사용)
    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}