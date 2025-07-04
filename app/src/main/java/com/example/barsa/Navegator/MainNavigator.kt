package com.example.barsa.Navegator

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barsa.Body.MainBody
import com.example.barsa.Footer.Footer
import com.example.barsa.Header.Header
import com.example.barsa.Header.ModernNotificationBox
import com.example.barsa.Login.LoginScreen
import com.example.barsa.Header.Notification

import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.EtapaSelector
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.NotificationViewModel
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
import kotlinx.coroutines.delay

@Composable
fun MainNavigator(
    tiemposViewModel: TiemposViewModel,
    userViewModel: UserViewModel,
    papeletaViewModel: PapeletaViewModel,
    inventoryViewModel: InventoryViewModel,
    notificationViewModel: NotificationViewModel
) {
    val navController = rememberNavController()
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    var currentRoute by remember { mutableStateOf("") }

    if (rol.equals("Administrador") || rol.equals("Inventarios") || rol.equals("SuperAdministrador")) {
        currentRoute = "inventario"
    } else if (rol.equals("Produccion")) {
        currentRoute = "producciones"
    }

    var showNotifications by remember { mutableStateOf(false) }

    // Estados de notificaciones desde el ViewModel
    val notificationsState by notificationViewModel.notificationsState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(userViewModel, onLoginClick = { username, password ->
                Log.d("LoginScreen", "${username}, ${password}")
                userViewModel.login(username, password)
            })

            // Escuchar el resultado del login
            val loginResult by userViewModel.loginState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(loginResult) {
                when (loginResult) {
                    is UserViewModel.LoginState.Success -> {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                            userViewModel.obtenerInfoUsuarioPersonal()
                            // Cargar notificaciones al hacer login
                            notificationViewModel.loadNotifications()
                        }
                    }

                    is UserViewModel.LoginState.Error -> {
                        val message = (loginResult as UserViewModel.LoginState.Error).message
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }

                    is UserViewModel.LoginState.Initial,
                    is UserViewModel.LoginState.Loading -> {
                        // No hacer nada
                    }
                }
            }
        }

        composable("main") {
            Scaffold(
                topBar = {
                    Header(
                        unreadCount = unreadCount,
                        onShowNotifications = { showNotifications = true }
                    )
                },
                bottomBar = {
                    Footer(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            currentRoute = route
                        },
                        userViewModel
                    )
                }
            ) { paddingValues ->
                MainBody(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                    },
                    modifier = Modifier.padding(paddingValues),
                    tiemposViewModel,
                    papeletaViewModel,
                    userViewModel,
                    inventoryViewModel,
                    onLogout = {
                        // Limpiar todos los estados antes de navegar
                        userViewModel.clearAllStates()
                        notificationViewModel.clearCache() // Limpiar cache de notificaciones
                        Log.d("MainNavigator", "Navegando al login desde MainNavigator")
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )

                if (showNotifications) {
                    ModernNotificationBox(
                        notificationsState = notificationsState,
                        unreadCount = unreadCount,
                        onDismissNotification = { notificationId ->
                            notificationViewModel.deleteNotification(notificationId)
                        },
                        onMarkAsRead = { notificationId ->
                            notificationViewModel.markAsRead(notificationId)
                        },
                        onCloseNotifications = { showNotifications = false },
                        onRefresh = { notificationViewModel.refreshNotifications() }
                    )
                }
            }
        }

        // Agregando ruta de la vista de cronometro
        composable(
            "cronometro/{TipoId}/{Folio}/{Fecha}/{Status}/{Etapa}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""
            val Etapa = backStackEntry.arguments?.getString("Etapa") ?: ""

            CronometroScreen(TipoId, Folio, Fecha, Status, Etapa, onNavigate = { route -> navController.navigate(route) }, tiemposViewModel, papeletaViewModel)
        }

        composable("selector/{TipoId}/{Folio}/{Fecha}/{Status}") { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""

            EtapaSelector(TipoId, Folio, Fecha, Status, { etapaSeleccionada ->
                navController.navigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
            }, onNavigate = { route -> navController.navigate(route) }, tiemposViewModel, papeletaViewModel, userViewModel)
        }
    }
}


/*
        composable("login") {
            LoginScreen(userViewModel,
                onLoginClick = { username, password ->
                    // Aquí puedes agregar la lógica de autenticación
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

         */