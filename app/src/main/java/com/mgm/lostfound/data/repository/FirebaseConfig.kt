package com.mgm.lostfound.data.repository

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseConfig {
    fun initialize(app: FirebaseApp) {
        // Firebase is auto-initialized with google-services.json
    }
    
    /**
     * Check if Firebase is properly configured
     */
    fun isFirebaseConfigured(): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            val projectId = app.options.projectId
            !projectId.isNullOrEmpty() && !projectId.contains("your-project-id")
        } catch (e: Exception) {
            false
        }
    }

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val storage: FirebaseStorage
        get() = FirebaseStorage.getInstance()

    val messaging: FirebaseMessaging
        get() = FirebaseMessaging.getInstance()
}

