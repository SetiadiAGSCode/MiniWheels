package com.setiadi0053.miniwheels.data.repository

import com.setiadi0053.miniwheels.data.local.DiecastDao
import com.setiadi0053.miniwheels.data.local.asEntity
import com.setiadi0053.miniwheels.data.local.asExternalModel
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.remote.ApiService
import com.setiadi0053.miniwheels.util.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DiecastRepository(
    private val apiService: ApiService,
    private val diecastDao: DiecastDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("diecasts")

    /**
     * Point 3a: Combined Firestore with Room (Offline-first)
     */
    fun getDiecasts(userId: String): Flow<NetworkResult<List<Diecast>>> = flow {
        emit(NetworkResult.Loading())
        
        try {
            val snapshot = collection.whereEqualTo("ownerId", userId).get().await()
            val networkData = snapshot.documents.map { doc ->
                Diecast(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    brand = doc.getString("brand") ?: "",
                    scale = doc.getString("scale") ?: "",
                    releaseYear = doc.getLong("releaseYear")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    ownerId = doc.getString("ownerId") ?: ""
                )
            }
            diecastDao.clearAll(userId)
            diecastDao.insertDiecasts(networkData.map { it.asEntity() })
        } catch (_: Exception) {
            // Network error - we just rely on local data
        }
        
        // Emit final flow from Room which will react to future database changes
        emitAll(diecastDao.getDiecasts(userId).map { entities ->
            NetworkResult.Success(entities.map { it.asExternalModel() })
        })
    }

    suspend fun addDiecast(
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageSource: String, // This is Base64
        ownerId: String
    ): NetworkResult<Diecast> {
        return try {
            val docRef = collection.document()
            val diecast = Diecast(docRef.id, name, brand, scale, year, imageSource, ownerId)
            
            docRef.set(diecast).await()
            
            diecastDao.insertDiecasts(listOf(diecast.asEntity()))
            NetworkResult.Success(diecast)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add diecast")
        }
    }

    suspend fun deleteDiecast(id: String): NetworkResult<Unit> {
        return try {
            collection.document(id).delete().await()
            diecastDao.deleteDiecast(id)
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
        imageSource: String? = null,
        ownerId: String
    ): NetworkResult<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "brand" to brand,
                "scale" to scale,
                "releaseYear" to year,
                "ownerId" to ownerId
            )
            imageSource?.let { updates["imageUrl"] = it }

            collection.document(id).update(updates).await()
            
            // Note: In a real app we'd update Room here too
            // For now, let the refresh logic handle it or update manually
            val updatedDiecast = Diecast(
                id, name, brand, scale, year, imageSource ?: "", ownerId
            )
            diecastDao.insertDiecasts(listOf(updatedDiecast.asEntity()))
            
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update")
        }
    }
}
