package com.example.barsa.Navegator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barsa.Body.MainBody
import com.example.barsa.Footer.Footer
import com.example.barsa.Header.Header
import com.example.barsa.Login.LoginScreen
import com.example.barsa.Header.Notification
import com.example.barsa.Header.NotificationBox
import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.data.TiemposViewModel

@Composable
fun MainNavigator(tiemposViewModel: TiemposViewModel) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("inventario") }
    var showNotifications by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(
        listOf(
            Notification(1, "Nuevo pedido recibido", Color.Green),
            Notification(2, "Stock bajo en madera de roble", Color(0xFFFFA500)),
            Notification(3, "Retraso en entrega de suministros", Color.Red),
            Notification(4, "Recordatorio: Mantenimiento programado", Color.Blue),
            Notification(5, "Nueva colección de muebles disponible", Color.Green),
            Notification(6, "Actualización de precios pendiente", Color(0xFFFFA500)),
            Notification(7, "Solicitud de cotización recibida", Color.Blue),
            Notification(8, "Recordatorio: Reunión de equipo", Color.Green)
        )
    ) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { username, password ->
                    // Aquí puedes agregar la lógica de autenticación
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            Scaffold(
                topBar = {
                    Header(
                        notifications = notifications,
                        onShowNotifications = { showNotifications = true }
                    )
                },
                bottomBar = {
                    Footer(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            currentRoute = route
                        }
                    )
                }
            ) { paddingValues ->
                MainBody(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                    },
                    modifier = Modifier.padding(paddingValues),
                    tiemposViewModel
                )

                if (showNotifications) {
                    NotificationBox(
                        notifications = notifications,
                        onDismissNotification = { id ->
                            notifications = notifications.filter { it.id != id }
                        },
                        onCloseNotifications = { showNotifications = false }
                    )
                }
            }
        }

        // Agregando ruta de la vista de cronometro
        //composable("cronometro") { CronometroScreen() }
        composable(
            "cronometro/{TipoId}/{Folio}/{Fecha}/{Status}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""

            CronometroScreen(TipoId, Folio, Fecha, Status, tiemposViewModel)
        }
    }
}