package com.mgm.lostfound.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimRequest(
    val id: String = "",
    val itemId: String = "",
    val ownerId: String = "",
    val finderId: String = "",
    val status: ClaimStatus = ClaimStatus.PENDING,
    val ownerContactShared: Boolean = false,
    val finderContactShared: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        fun fromDocument(document: DocumentSnapshot): ClaimRequest? {
            return document.toObject(ClaimRequest::class.java)?.copy(id = document.id)
        }
    }
}

enum class ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
}
