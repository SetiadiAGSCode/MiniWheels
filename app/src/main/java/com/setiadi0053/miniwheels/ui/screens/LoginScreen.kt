package com.setiadi0053.miniwheels.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setiadi0053.miniwheels.ui.AuthViewModel
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme
import com.setiadi0053.miniwheels.util.NetworkResult

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is NetworkResult.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MiniWheels",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Your Ultimate Diecast Collection Manager",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        if (loginState is NetworkResult.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.signIn(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Sign in with Google")
            }
        }

        if (loginState is NetworkResult.Error) {
            Text(
                text = (loginState as NetworkResult.Error).message ?: "Error logging in",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
