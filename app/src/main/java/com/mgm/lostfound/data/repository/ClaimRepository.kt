package com.mgm.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mgm.lostfound.data.model.ClaimRequest
import com.mgm.lostfound.data.model.ClaimStatus
import com.mgm.lostfound.data.supabase.SupabaseRepository
import com.mgm.lostfound.utils.NotificationHelper
import kotlinx.coroutines.tasks.await
import android.content.Context

class ClaimRepository(private val context: Context) {
    private val db = FirebaseConfig.firestore
    private val supabaseRepo = SupabaseRepository()

    suspend fun createClaimRequest(claimRequest: ClaimRequest): Result<String> {
        return try {
            db.collection("claim_requests").document(claimRequest.id).set(claimRequest).await()
            
            // Get the username of the person claiming (owner)
            val ownerName = try {
                val userResult = supabaseRepo.getUser(claimRequest.ownerId)
                userResult.getOrNull()?.name ?: "Someone"
            } catch (e: Exception) {
                "Someone"
            }
            
            // Notify finder with username
            NotificationHelper.showNotification(
                context,
                "New Claim Request",
                "$ownerName wants to claim the item you found",
                "claim"
            )
            
            Result.success(claimRequest.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClaimStatus(
        claimId: String,
        status: ClaimStatus,
        contactShared: Boolean = false
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (contactShared) {
                updates["ownerContactShared"] = true
                updates["finderContactShared"] = true
            }
            
            db.collection("claim_requests").document(claimId).update(updates).await()
            
            // Notify owner
            NotificationHelper.showNotification(
                context,
                "Claim Request ${status.name}",
                "Your claim request has been ${status.name.lowercase()}",
                "claim"
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClaimRequests(userId: String): Result<List<ClaimRequest>> {
        return try {
            val snapshot = db.collection("claim_requests")
                .whereEqualTo("finderId", userId)
                .get()
                .await()
            
            val requests = snapshot.documents.mapNotNull {
                ClaimRequest.fromDocument(it)
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

