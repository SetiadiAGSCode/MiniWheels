package com.setiadi0053.miniwheels.data.repository

import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val userPreferences: UserPreferencesRepository,
) {
    // This is a placeholder for actual login logic with Google ID Token or custom API
    fun loginWithToken(token: String, name: String, email: String, photoUrl: String): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading())
        try {
            // Here you would normally send the Google token to your backend to get your own API token
            // For now, we simulate success and save to DataStore
            userPreferences.saveSession(token, name, email, photoUrl)
            emit(NetworkResult.Success(token))
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Login failed"))
        }
    }

    suspend fun logout() {
        userPreferences.clearSession()
    }
}
