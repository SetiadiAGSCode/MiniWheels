package com.setiadi0053.miniwheels.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.repository.DiecastRepository
import com.setiadi0053.miniwheels.util.ImageUtils
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiecastViewModel(
    private val repository: DiecastRepository,
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    private val _diecasts = MutableStateFlow<NetworkResult<List<Diecast>>>(NetworkResult.Loading())
    val diecasts: StateFlow<NetworkResult<List<Diecast>>> = _diecasts.asStateFlow()

    private val _uploadStatus = MutableStateFlow<NetworkResult<Diecast>?>(null)
    val uploadStatus: StateFlow<NetworkResult<Diecast>?> = _uploadStatus.asStateFlow()

    init {
        observeUserAndFetchData()
    }

    /**
     * Point 1c: Observe the logged-in user and fetch only their data.
     */
    private fun observeUserAndFetchData() {
        viewModelScope.launch {
            userPrefs.userToken.collect { token ->
                if (token != null) {
                    // Use the token (UID) to fetch data
                    fetchDiecasts(token)
                } else {
                    _diecasts.value = NetworkResult.Error("Not authenticated")
                }
            }
        }
    }

    fun fetchDiecasts(userId: String? = null) {
        viewModelScope.launch {
            val id = userId ?: userPrefs.userToken.first()
            if (id != null) {
                repository.getDiecasts(id).collect {
                    _diecasts.value = it
                }
            } else {
                _diecasts.value = NetworkResult.Error("Not authenticated")
            }
        }
    }

    /**
     * Point 3b: Save data using the actual user ID from preferences.
     */
    fun addDiecast(
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageBytes: ByteArray,
    ) {
        viewModelScope.launch {
            _uploadStatus.value = NetworkResult.Loading()
            
            try {
                val userId = userPrefs.userToken.first()
                if (userId == null) {
                    _uploadStatus.value = NetworkResult.Error("User not logged in")
                    return@launch
                }

                val base64Image = withContext(Dispatchers.IO) {
                    ImageUtils.compressAndEncodeToBase64(imageBytes)
                }

                if (base64Image.length > (1024 * 1024)) {
                     _uploadStatus.value = NetworkResult.Error("Image too large")
                     return@launch
                }
                
                // Use actual userId instead of placeholder
                val result = repository.addDiecast(name, brand, scale, year, base64Image, userId)
                _uploadStatus.value = result
            } catch (e: Exception) {
                _uploadStatus.value = NetworkResult.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteDiecast(id: String) {
        viewModelScope.launch {
            repository.deleteDiecast(id)
        }
    }

    fun resetUploadStatus() {
        _uploadStatus.value = null
    }

    fun updateDiecast(
        id: String,
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageBytes: ByteArray? = null
    ) {
        viewModelScope.launch {
            _uploadStatus.value = NetworkResult.Loading()
            try {
                var base64Image: String? = null
                if (imageBytes != null) {
                    base64Image = withContext(Dispatchers.IO) {
                        ImageUtils.compressAndEncodeToBase64(imageBytes)
                    }
                    if (base64Image!!.length > 1024 * 1024) {
                        _uploadStatus.value = NetworkResult.Error("Image too large")
                        return@launch
                    }
                }

                val userId = userPrefs.userToken.first() ?: return@launch
                
                val result = repository.updateDiecast(
                    id, name, brand, scale, year, base64Image, userId
                )
                if (result is NetworkResult.Success) {
                    // Using uploadStatus for feedback
                    _uploadStatus.value = NetworkResult.Success(
                        Diecast(id, name, brand, scale, year, base64Image ?: "", userId)
                    )
                } else {
                    _uploadStatus.value = NetworkResult.Error(result.message ?: "Update failed")
                }
            } catch (e: Exception) {
                _uploadStatus.value = NetworkResult.Error("Update error: ${e.message}")
            }
        }
    }

    class Factory(
        private val repository: DiecastRepository,
        private val userPrefs: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DiecastViewModel(repository, userPrefs) as T
        }
    }
}
