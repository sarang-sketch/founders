package com.mgm.lostfound.data.supabase

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.createChannel
import io.github.jan.supabase.realtime.postgresChanges
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String,
    val name: String,
    val student_id: String,
    val phone: String? = null,
    val department: String? = null,
    val year: String? = null,
    val role: String = "STUDENT",
    val profile_image_url: String? = null,
    val fcm_token: String? = null,
    val created_at: String
)

@Serializable
data class SupabaseItem(
    val id: String,
    val user_id: String,
    val type: String, // LOST or FOUND
    val category: String,
    val title: String,
    val description: String,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photo_urls: List<String> = emptyList(),
    val serial_number: String? = null,
    val reward: String? = null,
    val qr_code: String? = null,
    val status: String = "ACTIVE",
    val matched_items: List<String> = emptyList(),
    val claim_request_id: String? = null,
    val contact_shared: Boolean = false,
    val finder_info: String? = null,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class SupabaseClaimRequest(
    val id: String,
    val item_id: String,
    val owner_id: String,
    val finder_id: String,
    val status: String = "PENDING",
    val owner_contact_shared: Boolean = false,
    val finder_contact_shared: Boolean = false,
    val created_at: String
)

class SupabaseRepository {
    private val supabase = SupabaseClient.client

    // Real-time channel for items
    fun subscribeToItems(type: String? = null): Flow<SupabaseItem> = flow {
        val channel = supabase.realtime.createChannel("items_channel") {
            postgresChanges("public", "items") {
                filter("type", type ?: "*")
            }
        }
        
        channel.subscribe()
        
        channel.postgresChanges.collect { change ->
            when (change) {
                is io.github.jan.supabase.realtime.PostgresChange.Insert -> {
                    val item = change.decodeRecord<SupabaseItem>()
                    emit(item)
                }
                is io.github.jan.supabase.realtime.PostgresChange.Update -> {
                    val item = change.decodeRecord<SupabaseItem>()
                    emit(item)
                }
                else -> {}
            }
        }
    }

    // Get all items with real-time updates
    suspend fun getItems(type: String? = null): Result<List<SupabaseItem>> {
        return try {
            val query = supabase.from("items").select()
            if (type != null) {
                query.eq("type", type)
            }
            val items = query.decodeList<SupabaseItem>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Insert item
    suspend fun insertItem(item: SupabaseItem): Result<String> {
        return try {
            supabase.from("items").insert(item)
            Result.success(item.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update item
    suspend fun updateItem(itemId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            supabase.from("items").update(updates) {
                filter {
                    eq("id", itemId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user
    suspend fun getUser(userId: String): Result<SupabaseUser> {
        return try {
            val user = supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<SupabaseUser>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Insert user
    suspend fun insertUser(user: SupabaseUser): Result<String> {
        return try {
            supabase.from("users").insert(user)
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get claim requests
    suspend fun getClaimRequests(userId: String): Result<List<SupabaseClaimRequest>> {
        return try {
            val requests = supabase.from("claim_requests")
                .select {
                    filter {
                        or {
                            eq("owner_id", userId)
                            eq("finder_id", userId)
                        }
                    }
                }
                .decodeList<SupabaseClaimRequest>()
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Insert claim request
    suspend fun insertClaimRequest(request: SupabaseClaimRequest): Result<String> {
        return try {
            supabase.from("claim_requests").insert(request)
            Result.success(request.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user FCM token
    suspend fun updateUserFcmToken(userId: String, fcmToken: String): Result<Unit> {
        return try {
            supabase.from("users").update(mapOf("fcm_token" to fcmToken)) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all user FCM tokens (for broadcasting notifications)
    suspend fun getAllUserFcmTokens(excludeUserId: String? = null): Result<List<String>> {
        return try {
            val query = supabase.from("users").select(Columns.ALL) {
                filter {
                    isNotNull("fcm_token")
                }
            }
            val users = query.decodeList<SupabaseUser>()
            val tokens = users
                .filter { it.fcm_token != null && it.id != excludeUserId }
                .mapNotNull { it.fcm_token }
            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

