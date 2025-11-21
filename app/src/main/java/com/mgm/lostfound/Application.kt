package com.mgm.lostfound

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mgm.lostfound.utils.NotificationHelper

class MGMApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationHelper.createNotificationChannel(this)
    }
}

