package com.mgm.lostfound.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mgm.lostfound.R
import com.mgm.lostfound.data.supabase.SupabaseRepository
import com.mgm.lostfound.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "MGM Lost & Found"
            val body = notification.body ?: ""
            val type = remoteMessage.data["type"] ?: "general"

            showNotification(title, body, type)
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = "mgm_lost_found_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MGM Lost & Found Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for lost and found items"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Different sound for lost vs found
        val soundUri = when (type) {
            "lost" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "found" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save token to Supabase
        saveTokenToSupabase(token)
    }

    private fun saveTokenToSupabase(token: String) {
        // Get current user ID from Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Use coroutine scope to save token
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val supabaseRepo = SupabaseRepository()
                    supabaseRepo.updateUserFcmToken(userId, token)
                } catch (e: Exception) {
                    Log.e("NotificationService", "Failed to save FCM token", e)
                }
            }
        }
    }
}

