package com.example.arcanaai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,
    @SerialName("nickname")
    val nickname: String,
    @SerialName("profile_image")
    val profileImage: String? = null,
    @SerialName("gems")
    val gems: Int = 300,
    @SerialName("level")
    val level: Int = 1,
    @SerialName("exp")
    val exp: Int = 0,
    @SerialName("equipped_master_id")
    val equippedMasterId: String = "arcana", // 👈 장착된 마스터 ID
    @SerialName("equipped_back_id")
    val equippedBackId: String = "default"   // 👈 장착된 카드 뒷면 ID
) {
    val maxExp: Int get() = level * 100
}

@Serializable
data class UserCardBack(
    @SerialName("user_id")
    val userId: String,
    @SerialName("back_id")
    val backId: String
)

@Serializable
data class UnlockedMaster(
    @SerialName("user_id")
    val userId: String,
    @SerialName("master_id")
    val masterId: String
)
