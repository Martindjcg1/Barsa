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
import com.example.barsa.Login.LoginScreen
import com.example.barsa.Header.Notification
import com.example.barsa.Header.NotificationBox
import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.EtapaSelector
import com.example.barsa.Producciones.InformeFolio
import com.example.barsa.Producciones.InformeIndividual
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
import kotlinx.coroutines.delay

@Composable
fun MainNavigator(
    tiemposViewModel: TiemposViewModel,
    userViewModel: UserViewModel,
    papeletaViewModel: PapeletaViewModel,
    inventoryViewModel: InventoryViewModel
) {
    val navController = rememberNavController()
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    var currentRoute by remember { mutableStateOf("") }




    if (rol.equals("Administrador") || rol.equals("Inventarios") || rol.equals("SuperAdministrador"))
    {
        currentRoute = "inventario"
    }
    else if (rol.equals("Produccion"))
    {
        currentRoute = "producciones"
    }
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
                        }

                    }

                    is UserViewModel.LoginState.Error -> {
                        val message = (loginResult as UserViewModel.LoginState.Error).message
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }

                    // NO HACER NADA EN Initial Y Loading
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
                        notifications = notifications,
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
                    inventoryViewModel, // Pasar el InventoryViewModel aquí
                    // PASAR LA FUNCIÓN DE LOGOUT AL MAINBODY
                    onLogout = {
                        // LIMPIAR TODOS LOS ESTADOS ANTES DE NAVEGAR
                        userViewModel.clearAllStates()
                        Log.d("MainNavigator", "Navegando al login desde MainNavigator")
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
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
            "cronometro/{TipoId}/{Folio}/{Fecha}/{Status}/{Etapa}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""
            val Etapa = backStackEntry.arguments?.getString("Etapa") ?: ""

            CronometroScreen(TipoId, Folio, Fecha, Status, Etapa,onNavigate = { route -> navController.navigate(route) } ,tiemposViewModel, papeletaViewModel)
        }

        composable("selector/{TipoId}/{Folio}/{Fecha}/{Status}") { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""

            EtapaSelector(TipoId, Folio, Fecha, Status, { etapaSeleccionada ->
                navController.navigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
            }, onNavigate = { route -> navController.navigate(route) },tiemposViewModel, papeletaViewModel, userViewModel)
        }

        composable(
            "informeIndividual/{TipoId}/{Folio}/{Fecha}/{Status}/{Etapa}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""
            val Etapa = backStackEntry.arguments?.getString("Etapa") ?: ""

            InformeIndividual(TipoId, Folio, Fecha, Status, Etapa, onNavigate = { route -> navController.navigate(route) }, papeletaViewModel)
        }

        composable(
            //"informeFolio/{TipoId}/{Folio}/{Fecha}/{Status}/{Etapa}"
            "informeFolio/{TipoId}/{Folio}/{Fecha}/{Status}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""
            //val Etapa = backStackEntry.arguments?.getString("Etapa") ?: ""

            //InformeFolio(TipoId, Folio, Fecha, Status, Etapa, onNavigate = { route -> navController.navigate(route) }, papeletaViewModel)
            InformeFolio(TipoId, Folio, Fecha, Status, onNavigate = { route -> navController.navigate(route) }, papeletaViewModel)

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