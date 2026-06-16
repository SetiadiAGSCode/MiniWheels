package com.setiadi0053.miniwheels.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object AddDiecast : Screen("add_diecast")
}
