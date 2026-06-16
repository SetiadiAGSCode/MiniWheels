package com.setiadi0053.miniwheels.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.setiadi0053.miniwheels.R
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.ui.DiecastViewModel
import com.setiadi0053.miniwheels.ui.navigation.Screen
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme
import com.setiadi0053.miniwheels.util.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DiecastViewModel,
    isLoggedIn: Boolean
) {
    val diecastsState by viewModel.diecasts.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Diecast?>(null) }

    // Dialog Konfirmasi (Point 4b)
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteDiecast(it.id) }
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isLoggedIn) {
                FloatingActionButton(onClick = { navController.navigate(Screen.AddDiecast.route) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Diecast")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (!isLoggedIn) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.welcome_message),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.login_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                when (diecastsState) {
                    is NetworkResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is NetworkResult.Success -> {
                        val list = (diecastsState as NetworkResult.Success<List<Diecast>>).data ?: emptyList()
                        if (list.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_collection),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(list) { diecast ->
                                    DiecastItem(
                                        diecast = diecast,
                                        onDeleteClick = { showDeleteDialog = diecast },
                                        onEditClick = {
                                            navController.navigate(Screen.EditDiecast.createRoute(diecast.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = (diecastsState as NetworkResult.Error).message ?: "Error load data")
                            Button(onClick = { viewModel.fetchDiecasts() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiecastItem(
    diecast: Diecast,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val imageModel = remember(diecast.imageUrl) {
        if (diecast.imageUrl.startsWith("data:image")) {
            try {
                val base64String = diecast.imageUrl.substringAfter(",")
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                diecast.imageUrl
            }
        } else {
            diecast.imageUrl
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imageModel,
                    contentDescription = diecast.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                // Delete Button (Point 4a)
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = diecast.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = diecast.brand, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "1:${diecast.scale}", fontSize = 12.sp)
                    Text(text = diecast.releaseYear.toString(), fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MiniWheelsTheme {
        DiecastItem(
            diecast = Diecast("1", "Skyline GT-R", "Hot Wheels", "64", 2023, "", "user1"),
            onDeleteClick = {},
            onEditClick = {}
        )
    }
}
