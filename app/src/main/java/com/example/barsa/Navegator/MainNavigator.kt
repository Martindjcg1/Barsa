package com.example.barsa.Navegator

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.barsa.Network.NetworkMonitor

import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.EtapaSelector
import com.example.barsa.Producciones.InformeFolio
import com.example.barsa.Producciones.InformeIndividual
import com.example.barsa.Producciones.InformePeriodo
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.NotificationViewModel
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun MainNavigator(
    tiemposViewModel: TiemposViewModel,
    userViewModel: UserViewModel,
    papeletaViewModel: PapeletaViewModel,
    inventoryViewModel: InventoryViewModel,
    notificationViewModel: NotificationViewModel,
    networkMonitor: NetworkMonitor,
    intent: Intent
) {
    val navController = rememberNavController()
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    var currentRoute by remember { mutableStateOf("") }

    LaunchedEffect(intent?.action) {
        if (intent?.action == "ABRIR_CRONOMETRO") {
            val tipoId = intent.getStringExtra("tipoId") ?: ""
            val folio = intent.getIntExtra("folio", 0)
            val fecha = intent.getStringExtra("fecha") ?: ""
            val status = intent.getStringExtra("status") ?: ""
            val etapa = intent.getStringExtra("etapa") ?: ""
            val isRun = intent.getBooleanExtra("isRun", false)
            currentRoute = "cronometro/$tipoId°$folio°$fecha°$status°$etapa°$isRun"
        }
    }

    // Estados para manejar el auto-login
    val accessToken by userViewModel.tokenManager.accessTokenFlow.collectAsState(initial = "")
    var isCheckingToken by remember { mutableStateOf(true) }
    var shouldAutoLogin by remember { mutableStateOf(false) }

    LaunchedEffect(rol, intent) {
        if (intent?.action == "ABRIR_CRONOMETRO") {
            val tipoId = intent.getStringExtra("tipoId") ?: ""
            val folio = intent.getIntExtra("folio", 0)
            val fecha = intent.getStringExtra("fecha") ?: ""
            val status = intent.getStringExtra("status") ?: ""
            val etapa = intent.getStringExtra("etapa") ?: ""
            val isRun = intent.getBooleanExtra("isRun", false)
            currentRoute = "cronometro/$tipoId°$folio°$fecha°$status°$etapa°$isRun"
        } else {
            if (rol.equals("Administrador") || rol.equals("Inventarios") || rol.equals("SuperAdministrador")) {
                currentRoute = "inventario"
            } else if (rol.equals("Produccion")) {
                currentRoute = "producciones"
            }
        }
    }

    var showNotifications by remember { mutableStateOf(false) }

    // Estados de notificaciones desde el ViewModel
    val notificationsState by notificationViewModel.notificationsState.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Verificar token al iniciar la app
    LaunchedEffect(Unit) {
        Log.d("MainNavigator", "Iniciando verificación de token...")

        // Dar tiempo para que el TokenManager se inicialice
        delay(100)

        val currentToken = userViewModel.tokenManager.accessTokenFlow.firstOrNull()
        Log.d("MainNavigator", "Token actual: ${if (currentToken.isNullOrEmpty()) "VACÍO" else "PRESENTE (${currentToken.length} chars)"}")

        if (!currentToken.isNullOrEmpty()) {
            Log.d("MainNavigator", "Token encontrado, preparando auto-login")
            shouldAutoLogin = true

            // Cargar datos del usuario y notificaciones
            try {
                userViewModel.obtenerInfoUsuarioPersonal()
                notificationViewModel.loadNotifications()
                Log.d("MainNavigator", "Datos cargados exitosamente")
            } catch (e: Exception) {
                Log.e("MainNavigator", "Error al cargar datos: ${e.message}")
            }
        } else {
            Log.d("MainNavigator", "No hay token, mostrando login")
            shouldAutoLogin = false
        }

        isCheckingToken = false
    }

    // Mostrar loading mientras se verifica el token
    if (isCheckingToken) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF8B4513)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verificando sesión...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF8B4513)
                )
            }
        }
        return
    }

    // Determinar destino inicial basado en la verificación
    val startDestination = if (shouldAutoLogin) "main" else "login"

    Log.d("MainNavigator", "Navegando a: $startDestination")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(userViewModel, onLoginClick = { username, password ->
                Log.d("LoginScreen", "Intentando login: $username")
                userViewModel.login(username, password)
            })

            // Escuchar el resultado del login
            val loginResult by userViewModel.loginState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(loginResult) {
                when (loginResult) {
                    is UserViewModel.LoginState.Success -> {
                        Log.d("MainNavigator", "Login exitoso, navegando a main")
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                        userViewModel.obtenerInfoUsuarioPersonal()
                        notificationViewModel.loadNotifications()
                    }

                    is UserViewModel.LoginState.Error -> {
                        val message = (loginResult as UserViewModel.LoginState.Error).message
                        Log.e("MainNavigator", "Error en login: $message")
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
            // Verificar si llegamos aquí con token válido
            LaunchedEffect(Unit) {
                val currentToken = userViewModel.tokenManager.accessTokenFlow.firstOrNull()
                if (currentToken.isNullOrEmpty()) {
                    Log.w("MainNavigator", "En main sin token válido, redirigiendo a login")
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }

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
                        Log.d("MainNavigator", "Ejecutando logout...")
                        // Limpiar todos los estados antes de navegar
                        userViewModel.clearAllStates()
                        notificationViewModel.clearCache()

                        // Resetear estados locales
                        shouldAutoLogin = false

                        Log.d("MainNavigator", "Navegando al login desde logout")
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    networkMonitor
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
            "cronometro/{TipoId}/{Folio}/{Fecha}/{Status}/{Etapa}/{isRun}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""
            val Etapa = backStackEntry.arguments?.getString("Etapa") ?: ""
            val isRun = backStackEntry.arguments?.getString("isRun")?.toBooleanStrictOrNull() ?: false

            CronometroScreen(TipoId, Folio, Fecha, Status, Etapa, isRun, onNavigate = { route -> navController.navigate(route) }, tiemposViewModel, papeletaViewModel, userViewModel, networkMonitor)
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
            "informeFolio/{TipoId}/{Folio}/{Fecha}/{Status}"
        ) { backStackEntry ->
            val TipoId = backStackEntry.arguments?.getString("TipoId") ?: ""
            val Folio = backStackEntry.arguments?.getString("Folio")?.toIntOrNull() ?: 0
            val Fecha = backStackEntry.arguments?.getString("Fecha") ?: ""
            val Status = backStackEntry.arguments?.getString("Status") ?: ""

            InformeFolio(TipoId, Folio, Fecha, Status, onNavigate = { route -> navController.navigate(route) }, papeletaViewModel)
        }

        composable(
            "informePeriodo/{fechaInicio}/{fechaFin}"
        ) { backStackEntry ->
            val fechaInicio = backStackEntry.arguments?.getString("fechaInicio") ?: ""
            val fechaFin = backStackEntry.arguments?.getString("fechaFin") ?: ""

            InformePeriodo(fechaInicio, fechaFin, onNavigate = { route -> navController.navigate(route) }, papeletaViewModel)
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