package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiecastScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Diecast") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Add Diecast Form (Coming in Phase 3)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDiecastScreenPreview() {
    MiniWheelsTheme {
        AddDiecastScreen(rememberNavController())
    }
}
