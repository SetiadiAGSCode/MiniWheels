package com.setiadi0053.miniwheels.data.repository

import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.remote.ApiService
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DiecastRepository(private val apiService: ApiService) {

    fun getDiecasts(token: String): Flow<NetworkResult<List<Diecast>>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.getDiecasts("Bearer $token")
            if (response.isSuccessful) {
                emit(NetworkResult.Success(response.body() ?: emptyList()))
            } else {
                emit(NetworkResult.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Unknown Error"))
        }
    }

    fun addDiecast(
        token: String,
        name: RequestBody,
        brand: RequestBody,
        scale: RequestBody,
        year: RequestBody,
        image: MultipartBody.Part
    ): Flow<NetworkResult<Diecast>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.addDiecast("Bearer $token", name, brand, scale, year, image)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Unknown Error"))
        }
    }

    fun deleteDiecast(token: String, id: String): Flow<NetworkResult<Unit>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.deleteDiecast("Bearer $token", id)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(Unit))
            } else {
                emit(NetworkResult.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Unknown Error"))
        }
    }
}
