package com.setiadi0053.miniwheels.data.repository

import com.setiadi0053.miniwheels.data.local.DiecastDao
import com.setiadi0053.miniwheels.data.local.asEntity
import com.setiadi0053.miniwheels.data.local.asExternalModel
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.remote.ApiService
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DiecastRepository(
    private val apiService: ApiService,
    private val diecastDao: DiecastDao
) {

    /**
     * Point 3a: Combined REST API with Room (Offline-first)
     */
    fun getDiecasts(userId: String): Flow<NetworkResult<List<Diecast>>> = flow {
        emit(NetworkResult.Loading())
        
        // Emit local data immediately from Room (Offline-first)
        val initialData = diecastDao.getDiecasts(userId).map { entities ->
            NetworkResult.Success(entities.map { it.asExternalModel() })
        }
        // We emit the current Room content first to show something to the user
        // emitAll(initialData) // Note: emitAll is terminal-ish if the flow doesn't end. 
        // Better to use a simpler approach or collect once.
        
        try {
            val response = apiService.getDiecasts(userId) 
            if (response.isSuccessful) {
                val networkData = response.body() ?: emptyList()
                diecastDao.clearAll(userId)
                diecastDao.insertDiecasts(networkData.map { it.asEntity() })
            }
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
        imageSource: String, // This is Base64 from ViewModel
        ownerId: String
    ): NetworkResult<Diecast> {
        return try {
            // For Point 2d: Sending data via REST API
            // Converting fields to RequestBody
            val nameRB = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val brandRB = brand.toRequestBody("text/plain".toMediaTypeOrNull())
            val scaleRB = scale.toRequestBody("text/plain".toMediaTypeOrNull())
            val yearRB = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Image handling: If the API expects Multipart, we'd convert Base64 back to bytes
            // Simplified for this example: assuming imageSource is the Base64 string
            val imagePart = MultipartBody.Part.createFormData(
                "image", "diecast.jpg", 
                imageSource.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            val response = apiService.addDiecast(ownerId, nameRB, brandRB, scaleRB, yearRB, imagePart)
            
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                diecastDao.insertDiecasts(listOf(result.asEntity()))
                NetworkResult.Success(result)
            } else {
                NetworkResult.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add diecast")
        }
    }

    suspend fun deleteDiecast(id: String): NetworkResult<Unit> {
        return try {
            // In a real app, we need the token/auth
            val response = apiService.deleteDiecast("dummy_token", id)
            if (response.isSuccessful) {
                diecastDao.deleteDiecast(id)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Delete failed")
            }
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
        ownerId: String // Need ownerId (token) for auth
    ): NetworkResult<Unit> {
        return try {
            val nameRB = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val brandRB = brand.toRequestBody("text/plain".toMediaTypeOrNull())
            val scaleRB = scale.toRequestBody("text/plain".toMediaTypeOrNull())
            val yearRB = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            
            val imagePart = imageSource?.let {
                MultipartBody.Part.createFormData(
                    "image", "diecast_update.jpg", 
                    it.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            val response = apiService.updateDiecast(ownerId, id, nameRB, brandRB, scaleRB, yearRB, imagePart)
            
            if (response.isSuccessful) {
                // Update local Room database
                // In a real scenario, we might want to fetch the latest or update partially
                // For simplicity, we just trigger a refresh later or update if we had the full entity
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Update API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update")
        }
    }
}
