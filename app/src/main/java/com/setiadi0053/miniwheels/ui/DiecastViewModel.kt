package com.setiadi0053.miniwheels.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.data.repository.DiecastRepository
import com.setiadi0053.miniwheels.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
            repository.getDiecasts().collect {
                _diecasts.value = it
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
            _uploadStatus.value = NetworkResult.Loading()
            
            try {
                // 1. Compress and convert to Base64 on an IO thread
                val base64Image = withContext(Dispatchers.IO) {
                    compressAndEncodeToBase64(imageBytes)
                }

                if (base64Image.length > 1024 * 1024) { // 1MB limit check
                     _uploadStatus.value = NetworkResult.Error("Image too large even after compression")
                     return@launch
                }

                val ownerId = "placeholder_user"
                
                // 2. Save document with Base64 string directly to Firestore
                val result = repository.addDiecast(name, brand, scale, year, base64Image, ownerId)
                _uploadStatus.value = result
            } catch (e: Exception) {
                _uploadStatus.value = NetworkResult.Error("Processing error: ${e.message}")
            }
        }
    }

    private fun compressAndEncodeToBase64(imageBytes: ByteArray): String {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        // Scale down if the image is very large to save space/bandwidth
        val maxSize = 800 
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()
        
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        val outputStream = ByteArrayOutputStream()
        // High compression (60%) to stay well under the 1MB Firestore limit
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val compressedBytes = outputStream.toByteArray()
        
        return "data:image/jpeg;base64," + Base64.encodeToString(compressedBytes, Base64.DEFAULT)
    }

    fun deleteDiecast(id: String) {
        viewModelScope.launch {
            _deleteStatus.value = NetworkResult.Loading()
            val result = repository.deleteDiecast(id)
            _deleteStatus.value = result
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
            @Suppress("UNCHECKED_CAST")
            return DiecastViewModel(repository, userPrefs) as T
        }
    }
}
