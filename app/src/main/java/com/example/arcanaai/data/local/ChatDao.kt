package com.example.arcanaai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arcanaai.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()

    // 💡 개별 삭제 기능을 추가했다냥!
    @Query("DELETE FROM chat_history WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    // 💡 선택 삭제 기능을 추가했다냥!
    @Query("DELETE FROM chat_history WHERE id IN (:messageIds)")
    suspend fun deleteMessagesByIds(messageIds: List<Long>)
}