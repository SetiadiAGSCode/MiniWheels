package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MiniWheelsTheme {
        ProfileScreen()
    }
}
