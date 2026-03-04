package com.example.arcanaai.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String, // 카카오 UID
    @SerialName("nickname")
    val nickname: String,
    @SerialName("gems")
    val gems: Int = 300,
    @SerialName("equipped_back_id")
    val equippedBackId: String = "default"
)

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
