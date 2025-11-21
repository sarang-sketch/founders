package com.mgm.lostfound.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mgm.lostfound.R
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.ui.auth.LoginActivity
import com.mgm.lostfound.ui.main.MainActivity
import com.mgm.lostfound.utils.NotificationHelper

class SplashActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        handler.postDelayed({
            checkAuthStatus()
        }, 2000) // 2 second splash
    }

    private fun checkAuthStatus() {
        if (authRepository.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

