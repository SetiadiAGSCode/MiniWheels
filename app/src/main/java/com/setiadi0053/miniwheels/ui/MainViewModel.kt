package com.setiadi0053.miniwheels.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: UserPreferencesRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _networkStatus = MutableStateFlow<ConnectivityObserver.Status>(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus.asStateFlow()

    init {
        checkLoginStatus()
        observeNetwork()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            repository.userToken.collect { token ->
                _isLoggedIn.value = !token.isNullOrEmpty()
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _networkStatus.value = status
            }
        }
    }

    class Factory(
        private val repository: UserPreferencesRepository,
        private val connectivityObserver: ConnectivityObserver
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, connectivityObserver) as T
        }
    }
}
