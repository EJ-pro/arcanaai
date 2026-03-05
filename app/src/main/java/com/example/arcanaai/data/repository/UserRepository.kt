package com.example.arcanaai.data.repository

import com.example.arcanaai.data.model.UnlockedMaster
import com.example.arcanaai.data.model.UserCardBack
import com.example.arcanaai.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _ownedCardBacks = MutableStateFlow<List<String>>(emptyList())
    val ownedCardBacks: StateFlow<List<String>> = _ownedCardBacks.asStateFlow()

    private val _unlockedMasters = MutableStateFlow<List<String>>(listOf("arcana"))
    val unlockedMasters: StateFlow<List<String>> = _unlockedMasters.asStateFlow()

    suspend fun refreshUserData(userId: String) = withContext(Dispatchers.IO) {
        try {
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
            
            if (profile != null) {
                _userProfile.value = profile
                
                val cardBacks = supabase.postgrest["user_card_backs"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UserCardBack>().map { it.backId }
                _ownedCardBacks.value = cardBacks

                val masters = supabase.postgrest["unlocked_masters"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UnlockedMaster>().map { it.masterId }
                _unlockedMasters.value = listOf("arcana") + masters
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateEquippedMaster(userId: String, masterId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"].update({ set("equipped_master_id", masterId) }) { filter { eq("id", userId) } }
            _userProfile.value = _userProfile.value?.copy(equippedMasterId = masterId)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateEquippedBack(userId: String, backId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"].update({ set("equipped_back_id", backId) }) { filter { eq("id", userId) } }
            _userProfile.value = _userProfile.value?.copy(equippedBackId = backId)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun createProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"].upsert(profile)
            _userProfile.value = profile
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun unlockMaster(userId: String, masterId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["unlocked_masters"].insert(UnlockedMaster(userId, masterId))
            _unlockedMasters.value = _unlockedMasters.value + masterId
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun gainExp(userId: String, amount: Int) = withContext(Dispatchers.IO) {
        val current = _userProfile.value ?: return@withContext
        var newExp = current.exp + amount
        var newLevel = current.level
        while (newExp >= newLevel * 100) {
            newExp -= (newLevel * 100)
            newLevel++
        }
        try {
            supabase.postgrest["profiles"].update({ 
                set("exp", newExp)
                set("level", newLevel)
            }) { filter { eq("id", userId) } }
            _userProfile.value = current.copy(exp = newExp, level = newLevel)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateGems(userId: String, newGems: Int) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"].update({ set("gems", newGems) }) { filter { eq("id", userId) } }
            _userProfile.value = _userProfile.value?.copy(gems = newGems)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun addCardBack(userId: String, backId: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["user_card_backs"].insert(UserCardBack(userId, backId))
            _ownedCardBacks.value = _ownedCardBacks.value + backId
        } catch (e: Exception) { e.printStackTrace() }
    }
}
