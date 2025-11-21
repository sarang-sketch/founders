package com.mgm.lostfound.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class LostFoundItem(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val type: ItemType = ItemType.LOST,
    val category: ItemCategory = ItemCategory.OTHER,
    val title: String = "",
    val description: String = "",
    val location: Location? = null,
    val photoUrls: List<String> = emptyList(),
    val serialNumber: String = "",
    val reward: String = "",
    val qrCode: String = "",
    val status: ItemStatus = ItemStatus.ACTIVE,
    val matchedItems: List<String> = emptyList(),
    val claimRequestId: String = "",
    val contactShared: Boolean = false,
    val finderInfo: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        fun fromDocument(document: DocumentSnapshot): LostFoundItem? {
            return document.toObject(LostFoundItem::class.java)?.copy(id = document.id)
        }
    }
}

enum class ItemType {
    LOST,
    FOUND
}

enum class ItemCategory {
    PHONE,
    ID_CARD,
    LAPTOP,
    KEYS,
    WALLET,
    BAG,
    OTHER
}

enum class ItemStatus {
    ACTIVE,
    CLAIMED,
    CLOSED,
    FLAGGED
}

@Parcelize
data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
) : Parcelable

