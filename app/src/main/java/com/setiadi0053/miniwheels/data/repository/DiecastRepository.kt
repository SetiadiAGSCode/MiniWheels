package com.setiadi0053.miniwheels.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DiecastRepository(
    private val db: FirebaseFirestore,
) {

    /**
     * Point 1c: Fetch only data belonging to the logged-in user.
     */
    fun getDiecasts(userId: String): Flow<NetworkResult<List<Diecast>>> = callbackFlow {
        trySend(NetworkResult.Loading())
        val subscription = db.collection("diecasts")
            .whereEqualTo("ownerId", userId) // Filter by ownerId
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(NetworkResult.Error(error.message ?: "Unknown Firestore Error"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DiecastMap::class.java)?.toDiecast(doc.id)
                    }
                    trySend(NetworkResult.Success(items))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addDiecast(
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageSource: String,
        ownerId: String
    ): NetworkResult<Diecast> {
        return try {
            val diecastMap = hashMapOf(
                "name" to name,
                "brand" to brand,
                "scale" to scale,
                "releaseYear" to year,
                "imageUrl" to imageSource,
                "ownerId" to ownerId
            )
            val docRef = db.collection("diecasts").add(diecastMap).await()
            val newDiecast = Diecast(docRef.id, name, brand, scale, year, imageSource, ownerId)
            NetworkResult.Success(newDiecast)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add diecast")
        }
    }

    suspend fun deleteDiecast(id: String): NetworkResult<Unit> {
        return try {
            db.collection("diecasts").document(id).delete().await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to delete diecast")
        }
    }

    suspend fun updateDiecast(
        id: String,
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageSource: String? = null
    ): NetworkResult<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "brand" to brand,
                "scale" to scale,
                "releaseYear" to year
            )
            imageSource?.let { updates["imageUrl"] = it }
            
            db.collection("diecasts").document(id).update(updates).await()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update diecast")
        }
    }

    private data class DiecastMap(
        val name: String = "",
        val brand: String = "",
        val scale: String = "",
        val releaseYear: Int = 0,
        val imageUrl: String = "",
        val ownerId: String = ""
    ) {
        fun toDiecast(id: String) = Diecast(id, name, brand, scale, releaseYear, imageUrl, ownerId)
    }
}
