package com.example.barsa.Navegator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barsa.Login.LoginScreen

@Composable
fun MainNavigator() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { username, password ->
                    // Aquí implementarías la lógica de autenticación
                    // y navegación a la siguiente pantalla
                }
            )
        }
        // Aquí puedes agregar más rutas según necesites
    }
}