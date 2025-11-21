package com.mgm.lostfound.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.data.model.UserRole
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseConfig.auth
    private val db = FirebaseConfig.firestore

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign in failed"))

            // Check if user exists in Firestore
            val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
            
            if (userDoc.exists()) {
                val user = User.fromDocument(userDoc)
                Result.success(user ?: throw Exception("User data error"))
            } else {
                // New user - need registration
                Result.failure(Exception("User not registered. Please complete registration."))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
            val user = User(
                id = userId,
                email = email,
                name = name,
                studentId = studentId,
                phone = phone,
                department = department,
                year = year,
                role = UserRole.STUDENT
            )
            
            db.collection("users").document(userId).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun demoLogin(): Result<User> {
        return try {
            // Demo user for testing
            val demoUser = User(
                id = "demo_user",
                email = "demo@mgm.ac.in",
                name = "Demo Student",
                studentId = "DEMO001",
                phone = "1234567890",
                department = "Computer Science",
                year = "3rd Year",
                role = UserRole.STUDENT
            )
            Result.success(demoUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}

