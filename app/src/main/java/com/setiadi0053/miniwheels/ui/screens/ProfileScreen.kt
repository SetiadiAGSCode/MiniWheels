package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.ui.AuthViewModel
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userData by userPreferencesRepository.userData.collectAsState(initial = emptyMap())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Photo Profile (Point 2b: Circle shape)
        AsyncImage(
            model = userData["photo"] ?: "https://via.placeholder.com/150",
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name (Point 2b)
        Text(
            text = userData["name"] ?: "User Name",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Email (Point 2b)
        Text(
            text = userData["email"] ?: "user@example.com",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Logout Button (Point 2c)
        Button(
            onClick = {
                authViewModel.logout(context) {
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Logout", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MiniWheelsTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("John Doe", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("john.doe@example.com", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Logout", color = Color.White)
            }
        }
    }
}
