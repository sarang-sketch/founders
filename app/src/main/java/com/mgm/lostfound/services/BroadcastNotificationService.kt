package com.mgm.lostfound.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.mgm.lostfound.data.supabase.SupabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Service to handle broadcast notifications to all users
 * Note: Sending FCM messages to topics requires a backend service with FCM server key.
 * This service sets up the infrastructure and subscribes users to topics.
 * For actual message sending, use a backend service or Supabase Edge Function.
 */
object BroadcastNotificationService {
    private const val FOUND_ITEMS_TOPIC = "found_items"
    
    /**
     * Subscribe current user to found items topic for broadcast notifications
     */
    suspend fun subscribeToFoundItemsTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(FOUND_ITEMS_TOPIC).await()
            Log.d("BroadcastNotificationService", "Subscribed to $FOUND_ITEMS_TOPIC topic")
        } catch (e: Exception) {
            Log.e("BroadcastNotificationService", "Failed to subscribe to topic", e)
        }
    }
    
    /**
     * Send notification about a found item to all users
     * This should be called from a backend service or Supabase Edge Function
     * that has access to FCM server key.
     * 
     * For now, this is a placeholder. In production, implement this in:
     * 1. Supabase Edge Function (recommended)
     * 2. Firebase Cloud Function
     * 3. Your own backend service
     * 
     * The backend should send FCM message to topic "found_items" with:
     * - title: "Found Item: [item.title]"
     * - body: "Found ${item.title} - is it yours?"
     * - data: { "type": "found", "itemId": item.id }
     */
    fun sendFoundItemNotificationToAllUsers(itemTitle: String, itemId: String) {
        // This is a placeholder - actual implementation should be in backend
        Log.d("BroadcastNotificationService", 
            "Should send notification: Found $itemTitle - is it yours? (Item ID: $itemId)")
        Log.d("BroadcastNotificationService", 
            "Note: This requires a backend service with FCM server key")
    }
    
    /**
     * Save FCM token for current user
     */
    fun saveFCMTokenForUser(context: Context, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val supabaseRepo = SupabaseRepository()
                supabaseRepo.updateUserFcmToken(userId, token)
                
                // Also subscribe to topic
                subscribeToFoundItemsTopic()
                
                Log.d("BroadcastNotificationService", "FCM token saved and subscribed to topic")
            } catch (e: Exception) {
                Log.e("BroadcastNotificationService", "Failed to save FCM token", e)
            }
        }
    }
}

