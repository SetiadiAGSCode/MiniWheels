package com.setiadi0053.miniwheels.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.setiadi0053.miniwheels.R
import com.setiadi0053.miniwheels.ui.DiecastViewModel
import com.setiadi0053.miniwheels.util.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiecastScreen(
    navController: NavController,
    viewModel: DiecastViewModel,
    diecastId: String
) {
    val context = LocalContext.current
    val diecastsState by viewModel.diecasts.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()

    val diecast = remember(diecastsState) {
        (diecastsState as? NetworkResult.Success)?.data?.find { it.id == diecastId }
    }

    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(diecast) {
        diecast?.let {
            name = it.name
            brand = it.brand
            scale = it.scale
            year = it.releaseYear.toString()
            existingImageUrl = it.imageUrl
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(uploadStatus) {
        if (uploadStatus is NetworkResult.Success) {
            viewModel.resetUploadStatus()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_diecast_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (diecast == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (existingImageUrl.isNotEmpty()) {
                        val bitmap = remember(existingImageUrl) {
                            if (existingImageUrl.startsWith("data:image")) {
                                val base64String = existingImageUrl.substringAfter(",")
                                val bytes = Base64.decode(base64String, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } else null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {}
                    }
                }

                Button(onClick = { launcher.launch("image/*") }) {
                    Text(stringResource(R.string.select_image))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.diecast_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(stringResource(R.string.brand)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scale,
                    onValueChange = { scale = it },
                    label = { Text(stringResource(R.string.scale)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text(stringResource(R.string.year)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uploadStatus is NetworkResult.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            val imageBytes = imageUri?.let { uri ->
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            }
                            viewModel.updateDiecast(
                                diecastId, name, brand, scale, year.toIntOrNull() ?: 0, imageBytes
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && brand.isNotBlank() && scale.isNotBlank() && year.isNotBlank()
                    ) {
                        Text(stringResource(R.string.update))
                    }
                }

                if (uploadStatus is NetworkResult.Error) {
                    Text(
                        text = (uploadStatus as NetworkResult.Error).message ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
