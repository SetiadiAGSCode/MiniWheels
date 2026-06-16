package com.setiadi0053.miniwheels.ui

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.setiadi0053.miniwheels.data.repository.AuthRepository
import com.setiadi0053.miniwheels.util.Constants
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<NetworkResult<String>?>(null)
    val loginState: StateFlow<NetworkResult<String>?> = _loginState.asStateFlow()

    fun signIn(context: Context) {
        val credentialManager = CredentialManager.create(context)
        
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(Constants.GOOGLE_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    repository.loginWithToken(
                        token = googleIdTokenCredential.idToken,
                        name = googleIdTokenCredential.displayName ?: "User",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri.toString()
                    ).collect {
                        _loginState.value = it
                    }
                }
            } catch (e: GetCredentialException) {
                _loginState.value = NetworkResult.Error(e.message ?: "Sign in failed")
            } catch (e: Exception) {
                _loginState.value = NetworkResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            repository.logout()
            onComplete()
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
