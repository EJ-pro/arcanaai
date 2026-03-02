package com.example.arcanaai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.arcanaai.data.model.ChatMessage

/**
 * 앱의 로컬 데이터베이스 정의 클래스
 * entities: 이 DB가 관리할 테이블(Entity) 목록
 * version: DB 구조가 바뀔 때마다 올려줘야 함 (지금은 초기 버전 1)
 */
@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // DAO(Data Access Object)를 꺼낼 수 있는 추상 메서드 정의
    abstract fun chatDao(): ChatDao
}