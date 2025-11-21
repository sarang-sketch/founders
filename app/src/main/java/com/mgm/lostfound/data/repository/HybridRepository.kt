package com.mgm.lostfound.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.data.model.UserRole
import com.mgm.lostfound.data.supabase.SupabaseItem
import com.mgm.lostfound.data.supabase.SupabaseRepository
import com.mgm.lostfound.data.supabase.SupabaseUser
import com.mgm.lostfound.services.BroadcastNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Hybrid Repository - Uses Firebase for Auth & Notifications, Supabase for Database
 */
class HybridRepository {
    private val firebaseAuth = FirebaseConfig.auth
    private val firebaseMessaging = FirebaseConfig.messaging
    private val supabaseRepo = SupabaseRepository()

    // ========== AUTHENTICATION (Firebase) ==========
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign in failed"))

            // Check if user exists in Supabase
            val supabaseUserResult = supabaseRepo.getUser(firebaseUser.uid)
            if (supabaseUserResult.isSuccess) {
                val supabaseUser = supabaseUserResult.getOrNull()!!
                
                // Save FCM token and subscribe to topics
                saveFCMTokenForCurrentUser()
                
                Result.success(convertSupabaseUserToUser(supabaseUser))
            } else {
                // User not registered
                Result.failure(Exception("User not registered. Please complete registration."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmailPassword(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign in failed"))

            // Check if user exists in Supabase
            val supabaseUserResult = supabaseRepo.getUser(firebaseUser.uid)
            if (supabaseUserResult.isSuccess) {
                val supabaseUser = supabaseUserResult.getOrNull()!!
                
                // Save FCM token and subscribe to topics
                saveFCMTokenForCurrentUser()
                
                Result.success(convertSupabaseUserToUser(supabaseUser))
            } else {
                // User not registered
                Result.failure(Exception("User not registered. Please complete registration."))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("password") == true -> "Invalid email or password"
                e.message?.contains("network") == true -> "Network error. Check internet connection"
                e.message?.contains("user-not-found") == true -> "No account found with this email"
                e.message?.contains("wrong-password") == true -> "Incorrect password"
                else -> e.message ?: "Sign in failed"
            }
            Result.failure(Exception(errorMsg))
        }
    }
    
    suspend fun createUserWithEmailPassword(email: String, password: String, name: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed"))
            
            // Update Firebase user profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            Result.success(firebaseUser.uid)
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("email-already-in-use") == true -> "Email already registered"
                e.message?.contains("weak-password") == true -> "Password is too weak"
                e.message?.contains("invalid-email") == true -> "Invalid email address"
                e.message?.contains("network") == true -> "Network error. Check internet connection"
                else -> e.message ?: "Registration failed"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun registerStudent(
        userId: String,
        email: String,
        name: String,
        studentId: String,
        phone: String,
        department: String,
        year: String
    ): Result<User> {
        return try {
            val supabaseUser = SupabaseUser(
                id = userId,
                email = email,
                name = name,
                student_id = studentId,
                phone = phone,
                department = department,
                year = year,
                role = "STUDENT",
                created_at = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .format(Date())
            )

            val result = supabaseRepo.insertUser(supabaseUser)
            result.onSuccess {
                // Save FCM token and subscribe to topics after registration
                saveFCMTokenForCurrentUser()
                
                Result.success(convertSupabaseUserToUser(supabaseUser))
            }.onFailure {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser
    fun signOut() = firebaseAuth.signOut()

    // ========== ITEMS (Supabase with Real-time) ==========
    fun subscribeToItems(type: ItemType? = null): Flow<LostFoundItem> {
        return supabaseRepo.subscribeToItems(type?.name)
            .map { convertSupabaseItemToItem(it) }
    }

    suspend fun getItems(type: ItemType? = null): Result<List<LostFoundItem>> {
        return try {
            val result = supabaseRepo.getItems(type?.name)
            result.onSuccess { items ->
                Result.success(items.map { convertSupabaseItemToItem(it) })
            }.onFailure {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportItem(item: LostFoundItem): Result<String> {
        return try {
            val supabaseItem = convertItemToSupabaseItem(item)
            val result = supabaseRepo.insertItem(supabaseItem)
            result.onSuccess {
                // Save FCM token for current user
                saveFCMTokenForCurrentUser()
                
                // If it's a FOUND item, trigger notification to all users
                if (item.type == ItemType.FOUND) {
                    // This will be handled by backend/Supabase Edge Function
                    // For now, log that notification should be sent
                    BroadcastNotificationService.sendFoundItemNotificationToAllUsers(
                        item.title,
                        item.id
                    )
                }
                
                Result.success(item.id)
            }.onFailure {
                Result.failure(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(itemId: String, status: String): Result<Unit> {
        return try {
            val result = supabaseRepo.updateItem(itemId, mapOf(
                "status" to status,
                "updated_at" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .format(Date())
            ))
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== NOTIFICATIONS (Firebase) ==========
    private suspend fun saveFCMTokenForCurrentUser() {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return
            val token = firebaseMessaging.token.await()
            supabaseRepo.updateUserFcmToken(userId, token)
            
            // Subscribe user to topic for broadcast notifications
            BroadcastNotificationService.subscribeToFoundItemsTopic()
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    // ========== CONVERSION HELPERS ==========
    private fun convertSupabaseUserToUser(supabaseUser: SupabaseUser): User {
        return User(
            id = supabaseUser.id,
            email = supabaseUser.email,
            name = supabaseUser.name,
            studentId = supabaseUser.student_id,
            phone = supabaseUser.phone ?: "",
            department = supabaseUser.department ?: "",
            year = supabaseUser.year ?: "",
            role = UserRole.valueOf(supabaseUser.role),
            profileImageUrl = supabaseUser.profile_image_url ?: "",
            createdAt = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(supabaseUser.created_at)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
    }

    private fun convertSupabaseItemToItem(supabaseItem: SupabaseItem): LostFoundItem {
        return LostFoundItem(
            id = supabaseItem.id,
            userId = supabaseItem.user_id,
            type = ItemType.valueOf(supabaseItem.type),
            category = com.mgm.lostfound.data.model.ItemCategory.valueOf(supabaseItem.category),
            title = supabaseItem.title,
            description = supabaseItem.description,
            location = if (supabaseItem.latitude != null && supabaseItem.longitude != null) {
                com.mgm.lostfound.data.model.Location(
                    latitude = supabaseItem.latitude,
                    longitude = supabaseItem.longitude,
                    address = supabaseItem.location ?: ""
                )
            } else null,
            photoUrls = supabaseItem.photo_urls,
            serialNumber = supabaseItem.serial_number ?: "",
            reward = supabaseItem.reward ?: "",
            qrCode = supabaseItem.qr_code ?: "",
            status = com.mgm.lostfound.data.model.ItemStatus.valueOf(supabaseItem.status),
            matchedItems = supabaseItem.matched_items,
            claimRequestId = supabaseItem.claim_request_id ?: "",
            contactShared = supabaseItem.contact_shared,
            finderInfo = supabaseItem.finder_info ?: "",
            createdAt = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(supabaseItem.created_at)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            },
            updatedAt = try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(supabaseItem.updated_at)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
    }

    private fun convertItemToSupabaseItem(item: LostFoundItem): SupabaseItem {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return SupabaseItem(
            id = item.id,
            user_id = item.userId,
            type = item.type.name,
            category = item.category.name,
            title = item.title,
            description = item.description,
            location = item.location?.address,
            latitude = item.location?.latitude,
            longitude = item.location?.longitude,
            photo_urls = item.photoUrls,
            serial_number = item.serialNumber.takeIf { it.isNotEmpty() },
            reward = item.reward.takeIf { it.isNotEmpty() },
            qr_code = item.qrCode,
            status = item.status.name,
            matched_items = item.matchedItems,
            claim_request_id = item.claimRequestId.takeIf { it.isNotEmpty() },
            contact_shared = item.contactShared,
            finder_info = item.finderInfo.takeIf { it.isNotEmpty() },
            created_at = dateFormat.format(Date(item.createdAt)),
            updated_at = dateFormat.format(Date(item.updatedAt))
        )
    }
}

