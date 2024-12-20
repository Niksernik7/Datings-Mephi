package com.example.datingsmephi

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder


@Composable
fun AuthorizationNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController = navController)
        }
        composable("confirm_screen") {
            ConfirmScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("login_screen") {
            LoginScreen(
                onClose = { navController.navigate("main_screen") },
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(
            route = "registration_screen/{user_data}",
            arguments = listOf(navArgument("user_data") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("user_data")?.let {
                URLDecoder.decode(it, "UTF-8") // Декодируем строку
            }
            val userDataForReg = json?.let {
                kotlinx.serialization.json.Json.decodeFromString<UserDataForRegistration>(it)
            }
            userDataForReg?.let {
                RegistrationScreen(it, navController)
            } ?: run {
                navController.popBackStack()
            }
        }

    }
}

