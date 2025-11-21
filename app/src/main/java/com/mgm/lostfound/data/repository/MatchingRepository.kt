package com.mgm.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mgm.lostfound.data.model.ItemCategory
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.utils.NotificationHelper
import kotlinx.coroutines.tasks.await
import android.content.Context

class MatchingRepository(private val context: Context) {
    private val db = FirebaseConfig.firestore

    suspend fun findAndNotifyMatches(item: LostFoundItem): Result<List<LostFoundItem>> {
        return try {
            val oppositeType = if (item.type == ItemType.LOST) ItemType.FOUND else ItemType.LOST
            val snapshot = db.collection("items")
                .whereEqualTo("type", oppositeType.name)
                .whereEqualTo("category", item.category.name)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()

            val allItems = snapshot.documents.mapNotNull {
                LostFoundItem.fromDocument(it)
            }

            // Advanced matching algorithm
            val matchedItems = allItems.filter { matchItem ->
                val categoryMatch = item.category == matchItem.category
                val descriptionMatch = checkDescriptionSimilarity(
                    item.description,
                    matchItem.description
                )
                val locationMatch = checkLocationProximity(
                    item.location,
                    matchItem.location
                )

                categoryMatch && (descriptionMatch || locationMatch)
            }

            // Update matched items in database
            matchedItems.forEach { matchedItem ->
                val updatedMatches = matchedItem.matchedItems.toMutableList()
                if (!updatedMatches.contains(item.id)) {
                    updatedMatches.add(item.id)
                }
                db.collection("items").document(matchedItem.id)
                    .update("matchedItems", updatedMatches)
                    .await()

                // Notify user about match
                NotificationHelper.showNotification(
                    context,
                    "Matching Item Found!",
                    "A ${item.type.name.lowercase()} item matches your ${matchedItem.type.name.lowercase()} report: ${matchedItem.title}",
                    "match"
                )
            }

            // Also update current item with matches
            if (matchedItems.isNotEmpty()) {
                val updatedMatches = item.matchedItems.toMutableList()
                matchedItems.forEach { matchedItem ->
                    if (!updatedMatches.contains(matchedItem.id)) {
                        updatedMatches.add(matchedItem.id)
                    }
                }
                db.collection("items").document(item.id)
                    .update("matchedItems", updatedMatches)
                    .await()
            }

            Result.success(matchedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun checkDescriptionSimilarity(desc1: String, desc2: String): Boolean {
        val words1 = desc1.lowercase().split(" ").toSet()
        val words2 = desc2.lowercase().split(" ").toSet()
        val commonWords = words1.intersect(words2)
        val similarity = commonWords.size.toDouble() / maxOf(words1.size, words2.size)
        return similarity >= 0.3 // 30% similarity threshold
    }

    private fun checkLocationProximity(loc1: com.mgm.lostfound.data.model.Location?, loc2: com.mgm.lostfound.data.model.Location?): Boolean {
        if (loc1 == null || loc2 == null) return false
        val distance = calculateDistance(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude
        )
        return distance <= 500 // Within 500 meters
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}

