package com.mgm.lostfound.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mgm.lostfound.R
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.HybridRepository
import com.mgm.lostfound.databinding.ActivityLoginBinding
import com.mgm.lostfound.ui.main.MainActivity
import com.mgm.lostfound.ui.registration.RegistrationActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authRepository = AuthRepository()
    private val hybridRepository = HybridRepository()
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        try {
            val clientId = getString(R.string.default_web_client_id)
            
            // Check if Firebase is properly configured
            if (clientId.contains("your-web-client-id") || clientId.contains("YOUR_CLIENT_ID")) {
                // Firebase not configured - disable Google Sign In button
                binding.btnGoogleSignIn.isEnabled = false
                binding.btnGoogleSignIn.text = "Firebase Not Configured"
                Toast.makeText(this, "Please configure Firebase first. See FIREBASE_SETUP.md", Toast.LENGTH_LONG).show()
                return
            }
            
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            // Handle configuration errors
            binding.btnGoogleSignIn.isEnabled = false
            binding.btnGoogleSignIn.text = "Config Error"
            Toast.makeText(this, "Firebase configuration error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnEmailLogin.setOnClickListener {
            signInWithEmailPassword()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnDemoLogin.setOnClickListener {
            demoLogin()
        }

        binding.tvRegister.setOnClickListener {
            navigateToRegistration()
        }
    }

    private fun signInWithEmailPassword() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }

        binding.btnEmailLogin.isEnabled = false
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        lifecycleScope.launch {
            val result = hybridRepository.signInWithEmailPassword(email, password)
            result.onSuccess { user ->
                navigateToMain(user)
            }.onFailure { exception ->
                Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                binding.btnEmailLogin.isEnabled = true
            }
        }
    }

    private fun navigateToRegistration() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun demoLogin() {
        binding.btnDemoLogin.isEnabled = false
        lifecycleScope.launch {
            val result = authRepository.demoLogin()
            result.onSuccess { user ->
                navigateToMain(user)
            }.onFailure { exception ->
                Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_SHORT).show()
                binding.btnDemoLogin.isEnabled = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { handleGoogleSignInResult(it) }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    10 -> "Firebase configuration error. Please check google-services.json"
                    12500 -> "Sign in was cancelled"
                    7 -> "Network error. Please check your internet connection"
                    8 -> "Internal error. Please try again"
                    else -> "Sign in failed: ${e.message} (Code: ${e.statusCode})"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                binding.btnGoogleSignIn.isEnabled = true
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnGoogleSignIn.isEnabled = true
            }
        }
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        binding.btnGoogleSignIn.isEnabled = false
        lifecycleScope.launch {
            // Use HybridRepository for Supabase integration
            account.idToken?.let { idToken ->
                val result = hybridRepository.signInWithGoogle(idToken)
                result.onSuccess { user ->
                    navigateToMain(user)
                }.onFailure { exception ->
                    if (exception.message?.contains("not registered") == true) {
                        // Navigate to registration
                        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
                        intent.putExtra("email", account.email ?: "")
                        intent.putExtra("name", account.displayName ?: "")
                        intent.putExtra("userId", account.id ?: "")
                        startActivity(intent)
                } else {
                    val errorMsg = when {
                        exception.message?.contains("network") == true -> "Network error. Check internet connection"
                        exception.message?.contains("invalid") == true -> "Invalid Firebase configuration"
                        exception.message?.contains("API") == true -> "Firebase API error. Check configuration"
                        else -> exception.message ?: "Authentication failed"
                    }
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
                binding.btnGoogleSignIn.isEnabled = true
                }
            } ?: run {
                Toast.makeText(this@LoginActivity, "Failed to get ID token", Toast.LENGTH_SHORT).show()
                binding.btnGoogleSignIn.isEnabled = true
            }
        }
    }

    private fun navigateToMain(user: User) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }
}

