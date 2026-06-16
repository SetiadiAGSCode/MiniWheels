package com.setiadi0053.miniwheels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.data.remote.RetrofitClient
import com.setiadi0053.miniwheels.data.repository.AuthRepository
import com.setiadi0053.miniwheels.ui.AuthViewModel
import com.setiadi0053.miniwheels.ui.MainViewModel
import com.setiadi0053.miniwheels.ui.navigation.Screen
import com.setiadi0053.miniwheels.ui.screens.AddDiecastScreen
import com.setiadi0053.miniwheels.ui.screens.DashboardScreen
import com.setiadi0053.miniwheels.ui.screens.LoginScreen
import com.setiadi0053.miniwheels.ui.screens.ProfileScreen
import com.setiadi0053.miniwheels.ui.theme.MiniWheelsTheme
import com.setiadi0053.miniwheels.util.ConnectivityObserver
import com.setiadi0053.miniwheels.util.NetworkConnectivityObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userPrefs = UserPreferencesRepository(applicationContext)
        val connectivityObserver = NetworkConnectivityObserver(applicationContext)
        val authRepository = AuthRepository(RetrofitClient.apiService, userPrefs)

        setContent {
            MiniWheelsTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(userPrefs, connectivityObserver)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authRepository)
                )
                
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                val networkStatus by mainViewModel.networkStatus.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn != null) {
                            AppNavigation(
                                startDestination = if (isLoggedIn == true) Screen.Dashboard.route else Screen.Login.route,
                                authViewModel = authViewModel,
                                userPrefs = userPrefs
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        if (networkStatus != ConnectivityObserver.Status.Available) {
                            OfflineBanner()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    startDestination: String,
    authViewModel: AuthViewModel,
    userPrefs: UserPreferencesRepository
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            // Passing navController to Dashboard so it can navigate to Profile or Add
            DashboardScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                userPreferencesRepository = userPrefs,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AddDiecast.route) {
            AddDiecastScreen(navController)
        }
    }
}

@Composable
fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Koneksi internet terputus. Silakan periksa jaringan Anda.",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
