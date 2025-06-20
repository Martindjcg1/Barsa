package com.example.barsa.Body.Usuario

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barsa.Usuario.AddUserSection
import com.example.barsa.Usuario.AdminPanelSection
import com.example.barsa.Usuario.ChangePasswordSection

import com.example.barsa.Usuario.EditProfileSection
import com.example.barsa.Usuario.ManageUsersSection
import com.example.barsa.Usuario.PersonalInfoSection
import com.example.barsa.Usuario.UserDetailsSection
import com.example.barsa.Usuario.UserListSection
import com.example.barsa.Usuario.UserOptionsSection
import com.example.barsa.data.retrofit.ui.UserViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioBody(
    onNavigate: (String) -> Unit,
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    var showSection by remember { mutableStateOf<String?>(null) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Observar el estado del logout
    val logoutState by userViewModel.logoutState.collectAsState()

    // NUEVO: Observar la información del usuario
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    // Simular que el usuario actual es administrador
    val isAdmin = true

    // Definir colores de la mueblería
    val primaryBrown = Color(0xFF8B4513) // Marrón oscuro
    val lightBrown = Color(0xFFDEB887)   // Marrón claro
    val accentBrown = Color(0xFF654321)  // Marrón medio
    val goldAccent = Color(0xFFD4AF37)   // Dorado para acentos

    // NUEVO: Obtener información del usuario al cargar
    LaunchedEffect(Unit) {
        userViewModel.obtenerInfoUsuarioPersonal()
    }

    // Efecto para manejar el logout exitoso
    LaunchedEffect(logoutState) {
        if (logoutState is UserViewModel.LogoutState.Success) {
            Log.d("UsuarioBody", "Logout exitoso, navegando al login")
            showLogoutDialog = false
            onLogout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (showSection == null) {
            // Pantalla principal con scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Header with diagonal background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Diagonal background with gradient
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height * 0.3f)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(primaryBrown, accentBrown),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                    }

                    // Profile content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                tint = primaryBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SECCIÓN MODIFICADA: Mostrar información real del usuario
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Manejar los diferentes estados de la información del usuario
                            infoUsuarioResult?.let { result ->
                                result.onSuccess { userInfo ->
                                    // Mostrar información real del usuario
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${userInfo.nombre ?: ""} ${userInfo.apellidos ?: ""}".trim().ifEmpty { "Usuario" },
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = userInfo.email ?: "Sin email",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )

                                        // Mostrar rol del usuario
                                        userInfo.rol?.let { rol ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (rol.lowercase().contains("admin"))
                                                        Icons.Default.Star else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = goldAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = rol,
                                                    color = goldAccent,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                result.onFailure { error ->
                                    // Mostrar información de error
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Error al cargar información",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Toca para reintentar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            } ?: run {
                                // Estado de carga
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Cargando información...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de opciones de usuario
                UserOptionsSection(
                    primaryBrown = primaryBrown,
                    lightBrown = lightBrown,
                    onOptionSelected = { option -> showSection = option }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Panel de Administrador (solo visible para administradores)
                if (isAdmin) {
                    AdminPanelSection(
                        accentBrown = accentBrown,
                        goldAccent = goldAccent,
                        onOptionSelected = { option -> showSection = option }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de cerrar sesión
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentBrown
                    ),
                    enabled = logoutState !is UserViewModel.LogoutState.Loading
                ) {
                    if (logoutState is UserViewModel.LogoutState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrando sesión...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // Pantallas de sección
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Top Bar with Back Button
                TopAppBar(
                    title = { Text(showSection ?: "") },
                    navigationIcon = {
                        IconButton(onClick = {
                            showSection = null
                            selectedUserId = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (showSection in listOf("Agregar Usuario", "Lista de Usuarios", "Eliminar Usuario", "Detalles de Usuario"))
                            goldAccent else primaryBrown,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )

                // Contenido de la sección
                Box(modifier = Modifier.fillMaxSize()) {
                    when (showSection) {
                        "Información Personal" -> PersonalInfoSection(userViewModel,primaryBrown, lightBrown)
                        "Cambiar Datos" -> EditProfileSection(userViewModel,primaryBrown, lightBrown)
                        "Cambiar contraseña" -> ChangePasswordSection(userViewModel, primaryBrown, lightBrown)
                        "Agregar Usuario" -> AddUserSection(userViewModel, accentBrown, goldAccent)
                        "Lista de Usuarios" -> UserListSection(
                            userViewModel = userViewModel,
                            primaryColor = accentBrown,
                            accentColor = goldAccent
                        ) { user ->
                            // CAPTURAR EL ID DEL USUARIO SELECCIONADO CON LOGS
                            Log.d("UsuarioBody", "Usuario seleccionado: ${user.username}")
                            Log.d("UsuarioBody", "ID del usuario: ${user.id}")

                            if (user.id != null) {
                                selectedUserId = user.id
                                showSection = "Detalles de Usuario"
                                Log.d("UsuarioBody", "Navegando a detalles con ID: $selectedUserId")
                            } else {
                                Log.e("UsuarioBody", "Error: El usuario no tiene ID")
                                // Mostrar mensaje de error al usuario
                            }
                        }
                        "Detalles de Usuario" -> {
                            // VERIFICAR QUE TENEMOS UN ID DE USUARIO
                            selectedUserId?.let { userId ->
                                Log.d("UsuarioBody", "Mostrando detalles para usuario ID: $userId")
                                UserDetailsSection(
                                    userViewModel = userViewModel,
                                    userId = userId,
                                    primaryColor = accentBrown,
                                    accentColor = goldAccent
                                )
                            } ?: run {
                                // MOSTRAR ERROR SI NO HAY ID
                                Log.e("UsuarioBody", "Error: No hay ID de usuario seleccionado")
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Error: No se pudo cargar la información del usuario",
                                            color = Color.Red,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { showSection = "Lista de Usuarios" },
                                            colors = ButtonDefaults.buttonColors(containerColor = accentBrown)
                                        ) {
                                            Text("Volver a la lista")
                                        }
                                    }
                                }
                            }
                        }
                        "Gestionar Usuarios" -> ManageUsersSection(userViewModel, accentBrown, goldAccent)
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = {
                if (logoutState is UserViewModel.LogoutState.Error) {
                    Text("Error al cerrar sesión: ${(logoutState as UserViewModel.LogoutState.Error).message}\n\n¿Deseas continuar?")
                } else {
                    Text("¿Estás seguro que deseas cerrar sesión?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (logoutState is UserViewModel.LogoutState.Error) {
                            // Si hay error, cerrar sesión localmente
                            showLogoutDialog = false
                            onLogout()
                        } else {
                            // Ejecutar logout
                            userViewModel.logout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown),
                    enabled = logoutState !is UserViewModel.LogoutState.Loading
                ) {
                    Text(
                        if (logoutState is UserViewModel.LogoutState.Error) {
                            "Continuar"
                        } else {
                            "Sí, cerrar sesión"
                        }
                    )
                }
            },
            dismissButton = {
                if (logoutState !is UserViewModel.LogoutState.Loading) {
                    Button(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}
