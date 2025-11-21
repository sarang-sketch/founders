package com.mgm.lostfound.ui.registration

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.HybridRepository
import com.mgm.lostfound.databinding.ActivityRegistrationBinding
import com.mgm.lostfound.ui.main.MainActivity
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val authRepository = AuthRepository()
    private val hybridRepository = HybridRepository()
    private var userId: String = ""
    private var email: String = ""
    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: ""
        email = intent.getStringExtra("email") ?: ""
        name = intent.getStringExtra("name") ?: ""

        binding.etEmail.setText(email)
        binding.etName.setText(name)
        
        // Show/hide password field based on registration type
        if (userId.isNotEmpty()) {
            // Google Sign-In registration - hide password field
            binding.tilPassword.visibility = android.view.View.GONE
        } else {
            // Email/Password registration - show password field
            binding.tilPassword.visibility = android.view.View.VISIBLE
            binding.etEmail.isEnabled = true
            binding.etName.isEnabled = true
        }

        binding.btnRegister.setOnClickListener {
            registerStudent()
        }
    }

    private fun registerStudent() {
        val studentId = binding.etStudentId.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val department = binding.etDepartment.text.toString().trim()
        val year = binding.etYear.text.toString().trim()
        val password = binding.etPassword?.text?.toString()?.trim() ?: ""

        if (validateInput(studentId, phone, department, year)) {
            binding.btnRegister.isEnabled = false
            lifecycleScope.launch {
                // If userId is empty, create Firebase user with email/password
                val finalUserId = if (userId.isEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    val createResult = hybridRepository.createUserWithEmailPassword(email, password, name)
                    createResult.getOrNull() ?: run {
                        Toast.makeText(this@RegistrationActivity, createResult.exceptionOrNull()?.message ?: "Failed to create account", Toast.LENGTH_LONG).show()
                        binding.btnRegister.isEnabled = true
                        return@launch
                    }
                } else {
                    userId
                }

                // Use HybridRepository to save to Supabase
                val result = hybridRepository.registerStudent(
                    userId = finalUserId,
                    email = email,
                    name = name,
                    studentId = studentId,
                    phone = phone,
                    department = department,
                    year = year
                )

                result.onSuccess { user ->
                    val intent = android.content.Intent(this@RegistrationActivity, MainActivity::class.java)
                    intent.putExtra("user", user)
                    startActivity(intent)
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(this@RegistrationActivity, exception.message, Toast.LENGTH_LONG).show()
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }

    private fun validateInput(studentId: String, phone: String, department: String, year: String): Boolean {
        if (studentId.isEmpty()) {
            binding.etStudentId.error = "Student ID is required"
            return false
        }
        if (phone.isEmpty() || phone.length < 10) {
            binding.etPhone.error = "Valid phone number is required"
            return false
        }
        if (department.isEmpty()) {
            binding.etDepartment.error = "Department is required"
            return false
        }
        if (year.isEmpty()) {
            binding.etYear.error = "Year is required"
            return false
        }
        return true
    }
}

