package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.setiadi0053.miniwheels.data.model.Diecast
import com.setiadi0053.miniwheels.ui.DiecastViewModel
import com.setiadi0053.miniwheels.ui.navigation.Screen
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme
import com.setiadi0053.miniwheels.util.NetworkResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DiecastViewModel
) {
    val diecastsState by viewModel.diecasts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MiniWheels") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddDiecast.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Diecast")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (diecastsState) {
                is NetworkResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NetworkResult.Success -> {
                    val list = (diecastsState as NetworkResult.Success<List<Diecast>>).data ?: emptyList()
                    if (list.isEmpty()) {
                        Text(
                            text = "Belum ada koleksi.",
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
                                DiecastItem(diecast = diecast)
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
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiecastItem(diecast: Diecast) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = diecast.imageUrl,
                contentDescription = diecast.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
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
        // Mocking behavior for preview is hard without a mock viewmodel, 
        // but we can show the item
        DiecastItem(
            diecast = Diecast("1", "Skyline GT-R", "Hot Wheels", "64", 2023, "", "user1")
        )
    }
}
