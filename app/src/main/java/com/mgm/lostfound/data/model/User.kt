package com.mgm.lostfound.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val studentId: String = "",
    val phone: String = "",
    val department: String = "",
    val year: String = "",
    val role: UserRole = UserRole.STUDENT,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        fun fromDocument(document: DocumentSnapshot): User? {
            return document.toObject(User::class.java)?.copy(id = document.id)
        }
    }
}

enum class UserRole {
    STUDENT,
    ADMIN,
    SECURITY
}

