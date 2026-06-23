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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.setiadi0053.miniwheels.data.local.AppDatabase
import com.setiadi0053.miniwheels.data.local.UserPreferencesRepository
import com.setiadi0053.miniwheels.data.repository.AuthRepository
import com.setiadi0053.miniwheels.data.repository.DiecastRepository
import com.setiadi0053.miniwheels.ui.AuthViewModel
import com.setiadi0053.miniwheels.ui.DiecastViewModel
import com.setiadi0053.miniwheels.ui.MainViewModel
import com.setiadi0053.miniwheels.ui.navigation.Screen
import com.setiadi0053.miniwheels.ui.screens.AddDiecastScreen
import com.setiadi0053.miniwheels.ui.screens.DashboardScreen
import com.setiadi0053.miniwheels.ui.screens.EditDiecastScreen
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
        val authRepository = AuthRepository(userPrefs)
        val database = AppDatabase.getDatabase(applicationContext)
        val diecastRepository = DiecastRepository(database.diecastDao())

        setContent {
            MiniWheelsTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(userPrefs, connectivityObserver),
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authRepository),
                )
                val diecastViewModel: DiecastViewModel = viewModel(
                    factory = DiecastViewModel.Factory(diecastRepository, userPrefs),
                )
                
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                val networkStatus by mainViewModel.networkStatus.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn != null) {
                            AppNavigation(
                                isLoggedIn = isLoggedIn == true,
                                authViewModel = authViewModel,
                                diecastViewModel = diecastViewModel,
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
    isLoggedIn: Boolean,
    authViewModel: AuthViewModel,
    diecastViewModel: DiecastViewModel,
    userPrefs: UserPreferencesRepository
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
            ) {
                diecastViewModel.fetchDiecasts() // Load data after login
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, diecastViewModel, isLoggedIn)
        }
        composable(Screen.Profile.route) {
            if (isLoggedIn) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    userPreferencesRepository = userPrefs
                ) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                ) {
                    diecastViewModel.fetchDiecasts()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                }
            }
        }
        composable(Screen.AddDiecast.route) {
            if (isLoggedIn) {
                AddDiecastScreen(navController, diecastViewModel)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            }
        }
        composable(
            route = Screen.EditDiecast.route,
            arguments = listOf(navArgument("diecastId") { type = NavType.StringType })
        ) { backStackEntry ->
            val diecastId = backStackEntry.arguments?.getString("diecastId") ?: ""
            if (isLoggedIn) {
                EditDiecastScreen(navController, diecastViewModel, diecastId)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            }
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
