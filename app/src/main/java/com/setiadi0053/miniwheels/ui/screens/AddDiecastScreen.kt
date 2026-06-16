package com.setiadi0053.miniwheels.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.setiadi0053.miniwheels.ui.DiecastViewModel
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme
import com.setiadi0053.miniwheels.util.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiecastScreen(
    navController: NavController,
    viewModel: DiecastViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val uploadStatus by viewModel.uploadStatus.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Sanity Check (Point 3b)
    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && brand.isNotBlank() && scale.isNotBlank() && 
            year.isNotBlank() && imageUri != null
        }
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
                title = { Text("Tambah Koleksi Baru") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Diecast") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Merek") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = scale,
                    onValueChange = { scale = it },
                    label = { Text("Skala (misal: 64)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Tahun Rilis") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    OutlinedButton(onClick = { launcher.launch("image/*") }) {
                        Text("Pilih Foto Diecast")
                    }
                }
            }

            if (imageUri != null) {
                TextButton(onClick = { launcher.launch("image/*") }) {
                    Text("Ganti Foto")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uploadStatus is NetworkResult.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val bytes = context.contentResolver.openInputStream(imageUri!!)?.readBytes()
                        if (bytes != null) {
                            viewModel.addDiecast(
                                name = name,
                                brand = brand,
                                scale = scale,
                                year = year.toIntOrNull() ?: 0,
                                imageBytes = bytes
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid // Point 3b: Validation handling
                ) {
                    Text("Simpan Koleksi")
                }
            }

            if (uploadStatus is NetworkResult.Error) {
                Text(
                    text = (uploadStatus as NetworkResult.Error).message ?: "Upload gagal",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDiecastScreenPreview() {
    MiniWheelsTheme {
        // Preview placeholder logic
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Nama Diecast") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("Simpan Koleksi")
            }
        }
    }
}
