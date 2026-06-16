package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.setiadi0053.miniwheels.ui.navigation.Screen
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Dashboard Content (Coming in Phase 3)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MiniWheelsTheme {
        DashboardScreen(rememberNavController())
    }
}
