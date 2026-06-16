package com.setiadi0053.miniwheels.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.repository.DiecastRepository
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DiecastViewModel(
    private val repository: DiecastRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _diecasts = MutableStateFlow<NetworkResult<List<Diecast>>>(NetworkResult.Loading())
    val diecasts: StateFlow<NetworkResult<List<Diecast>>> = _diecasts.asStateFlow()

    private val _uploadStatus = MutableStateFlow<NetworkResult<Diecast>?>(null)
    val uploadStatus: StateFlow<NetworkResult<Diecast>?> = _uploadStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<NetworkResult<Unit>?>(null)
    val deleteStatus: StateFlow<NetworkResult<Unit>?> = _deleteStatus.asStateFlow()

    init {
        fetchDiecasts()
    }

    fun fetchDiecasts() {
        viewModelScope.launch {
            val token = userPrefs.userToken.first()
            if (token != null) {
                repository.getDiecasts(token).collect {
                    _diecasts.value = it
                }
            } else {
                _diecasts.value = NetworkResult.Error("Not authenticated")
            }
        }
    }

    fun addDiecast(
        name: String,
        brand: String,
        scale: String,
        year: Int,
        imageBytes: ByteArray,
        imageName: String
    ) {
        viewModelScope.launch {
            val token = userPrefs.userToken.first()
            if (token != null) {
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val brandPart = brand.toRequestBody("text/plain".toMediaTypeOrNull())
                val scalePart = scale.toRequestBody("text/plain".toMediaTypeOrNull())
                val yearPart = year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    imageName,
                    imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                )

                repository.addDiecast(token, namePart, brandPart, scalePart, yearPart, imagePart).collect { result ->
                    _uploadStatus.value = result
                    if (result is NetworkResult.Success) {
                        fetchDiecasts() // Auto-update list (Point 3e)
                    }
                }
            }
        }
    }

    fun deleteDiecast(id: String) {
        viewModelScope.launch {
            val token = userPrefs.userToken.first()
            if (token != null) {
                repository.deleteDiecast(token, id).collect { result ->
                    _deleteStatus.value = result
                    if (result is NetworkResult.Success) {
                        // Point 4c: Auto-vanishing list - update local state immediately
                        val currentList = (_diecasts.value as? NetworkResult.Success)?.data ?: emptyList()
                        _diecasts.value = NetworkResult.Success(currentList.filter { it.id != id })
                    }
                }
            }
        }
    }

    fun resetUploadStatus() {
        _uploadStatus.value = null
    }
    
    fun resetDeleteStatus() {
        _deleteStatus.value = null
    }

    class Factory(
        private val repository: DiecastRepository,
        private val userPrefs: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiecastViewModel(repository, userPrefs) as T
        }
    }
}
