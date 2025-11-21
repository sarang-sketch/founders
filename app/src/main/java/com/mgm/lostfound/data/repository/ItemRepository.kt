package com.mgm.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ItemRepository {
    private val db = FirebaseConfig.firestore
    private val storage = FirebaseConfig.storage

    suspend fun reportItem(item: LostFoundItem): Result<String> {
        return try {
            val docRef = db.collection("items").document(item.id)
            docRef.set(item).await()
            Result.success(item.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(imageBytes: ByteArray, itemId: String): Result<String> {
        return try {
            val fileName = "${itemId}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("items/$fileName")
            val uploadTask = ref.putBytes(imageBytes).await()
            val url = uploadTask.storage.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLostItems(): Result<List<LostFoundItem>> {
        return try {
            val snapshot = db.collection("items")
                .whereEqualTo("type", ItemType.LOST.name)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { 
                LostFoundItem.fromDocument(it) 
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoundItems(): Result<List<LostFoundItem>> {
        return try {
            val snapshot = db.collection("items")
                .whereEqualTo("type", ItemType.FOUND.name)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { 
                LostFoundItem.fromDocument(it) 
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyReports(userId: String): Result<List<LostFoundItem>> {
        return try {
            val snapshot = db.collection("items")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { 
                LostFoundItem.fromDocument(it) 
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(itemId: String): Result<LostFoundItem> {
        return try {
            val doc = db.collection("items").document(itemId).get().await()
            val item = LostFoundItem.fromDocument(doc)
                ?: return Result.failure(Exception("Item not found"))
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findMatchingItems(item: LostFoundItem): Result<List<LostFoundItem>> {
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
            
            // Simple matching based on category and description similarity
            val matchedItems = allItems.filter { matchItem ->
                item.category == matchItem.category &&
                (item.description.lowercase().contains(matchItem.description.lowercase()) ||
                 matchItem.description.lowercase().contains(item.description.lowercase()))
            }
            
            Result.success(matchedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemStatus(itemId: String, status: String): Result<Unit> {
        return try {
            db.collection("items").document(itemId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

