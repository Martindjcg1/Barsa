package com.example.barsa.Usuario


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.ui.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersSection(
    userViewModel: UserViewModel,
    primaryColor: Color,
    accentColor: Color
) {
    var showActionDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showActiveOnly by remember { mutableStateOf(false) }
    var showInactiveOnly by remember { mutableStateOf(false) }

    // Estados para manejar la carga de usuarios
    val getUsersState by userViewModel.getUsersState.collectAsState()

    // NUEVO: Observar el estado de toggleUserStatus
    val toggleUserStatusState by userViewModel.toggleUserStatusState.collectAsState()

    // Observar información del usuario actual para filtrado
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    // Cargar usuarios al inicializar
    LaunchedEffect(Unit) {
        Log.d("ManageUsersSection", "Initializing - calling getUsers")
        userViewModel.getUsers()
        // También cargar información del usuario actual
        userViewModel.obtenerInfoUsuarioPersonal()
    }

    // NUEVO: Manejo del resultado de activar/desactivar usuario
    LaunchedEffect(toggleUserStatusState) {
        when (toggleUserStatusState) {
            is UserViewModel.ToggleUserStatusState.Success -> {
                Log.d("ManageUsersSection", "Estado del usuario cambiado exitosamente")
                val wasActivating = selectedUser?.active == false
                successMessage = if (wasActivating)
                    "Usuario activado correctamente"
                else
                    "Usuario desactivado correctamente"

                // MEJORADO: Recargar la lista de usuarios de forma más directa
                kotlinx.coroutines.delay(500) // Pequeño delay para que el servidor procese
                userViewModel.getUsers() // Recargar todos los usuarios

                // Cerrar el diálogo
                showActionDialog = false
                selectedUser = null

                // Limpiar mensaje después de 3 segundos
                kotlinx.coroutines.delay(2500) // Total 3 segundos
                successMessage = null
            }
            is UserViewModel.ToggleUserStatusState.Error -> {
                Log.e("ManageUsersSection", "Error al cambiar estado: ${(toggleUserStatusState as UserViewModel.ToggleUserStatusState.Error).message}")
                successMessage = "Error: ${(toggleUserStatusState as UserViewModel.ToggleUserStatusState.Error).message}"

                // Cerrar el diálogo en caso de error
                showActionDialog = false
                selectedUser = null
            }
            else -> {}
        }
    }

    // Función para aplicar filtros
    fun applyFilters() {
        Log.d("ManageUsersSection", "Applying filters - searchQuery: '$searchQuery', showActiveOnly: $showActiveOnly, showInactiveOnly: $showInactiveOnly")

        val estado = when {
            showActiveOnly -> "activo"
            showInactiveOnly -> "inactivo"
            else -> null
        }

        val searchTerm = if (searchQuery.isNotBlank()) searchQuery else null

        userViewModel.getUsers(
            nombre = searchTerm,
            nombreUsuario = searchTerm,
            email = searchTerm,
            estado = estado
        )
    }

    // Aplicar filtros cuando cambien los estados
    LaunchedEffect(showActiveOnly, showInactiveOnly) {
        applyFilters()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestionar Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Activa o desactiva cuentas de usuario según sea necesario.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
            },
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        applyFilters()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
            )
        )

        // Filtros de estado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showActiveOnly && !showInactiveOnly,
                onClick = {
                    showActiveOnly = false
                    showInactiveOnly = false
                },
                label = { Text("Todos") },
                leadingIcon = if (!showActiveOnly && !showInactiveOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )

            FilterChip(
                selected = showActiveOnly,
                onClick = {
                    showActiveOnly = !showActiveOnly
                    if (showActiveOnly) showInactiveOnly = false
                },
                label = { Text("Activos") },
                leadingIcon = if (showActiveOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )

            FilterChip(
                selected = showInactiveOnly,
                onClick = {
                    showInactiveOnly = !showInactiveOnly
                    if (showInactiveOnly) showActiveOnly = false
                },
                label = { Text("Inactivos") },
                leadingIcon = if (showInactiveOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }

        // Botón de búsqueda
        Button(
            onClick = { applyFilters() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buscar")
        }

        // Mensaje de éxito
        successMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (it.contains("Error"))
                        Color.Red.copy(alpha = 0.1f)
                    else
                        Color.Green.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (it.contains("Error"))
                            Icons.Default.Delete
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (it.contains("Error")) Color.Red else Color.Green
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = if (it.contains("Error")) Color.Red else Color.Green
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Contenido basado en el estado del API
        when (getUsersState) {
            is UserViewModel.GetUsersState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            is UserViewModel.GetUsersState.Success -> {
                val allUsers = (getUsersState as UserViewModel.GetUsersState.Success).users

                // Filtrar al usuario actual y aplicar filtros de rol
                val users = allUsers.filter { user ->
                    val currentUserInfo = infoUsuarioResult?.getOrNull()
                    val currentUserName = currentUserInfo?.nombreUsuario
                    val currentUserRole = currentUserInfo?.rol

                    // Filtrar al usuario actual
                    val isNotCurrentUser = user.username != currentUserName

                    // Si el usuario actual es Administrador, no mostrar SuperAdministradores
                    val canViewUser = if (currentUserRole?.lowercase() == "administrador") {
                        user.role?.lowercase() != "superadministrador"
                    } else {
                        true // SuperAdministrador puede ver todos los roles
                    }

                    isNotCurrentUser && canViewUser
                }

                Log.d("ManageUsersSection", "Usuarios totales: ${allUsers.size}, Usuarios filtrados: ${users.size}, Rol actual: ${infoUsuarioResult?.getOrNull()?.rol}")
                users.forEach { user ->
                    Log.d("ManageUsersSection", "Usuario: ${user.username}, ID: ${user.id}")
                }

                if (users.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron usuarios con los filtros actuales",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(users) { user ->
                            UserManagementItem(
                                user = user,
                                onClick = {
                                    selectedUser = user
                                    showActionDialog = true
                                },
                                primaryColor = primaryColor,
                                accentColor = accentColor
                            )
                        }
                    }
                }
            }
            is UserViewModel.GetUsersState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${(getUsersState as UserViewModel.GetUsersState.Error).message}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { userViewModel.getUsers() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is UserViewModel.GetUsersState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Presiona buscar para cargar usuarios",
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // NUEVO: Limpiar estado al salir
    DisposableEffect(Unit) {
        onDispose {
            userViewModel.resetToggleUserStatusState()
        }
    }

    // Diálogo de confirmación para activar/desactivar usuario
    if (showActionDialog && selectedUser != null) {
        val isActivating = !selectedUser!!.active
        AlertDialog(
            onDismissRequest = {
                showActionDialog = false
                selectedUser = null
            },
            title = { Text(if (isActivating) "Activar Usuario" else "Desactivar Usuario") },
            text = {
                Text(
                    if (isActivating)
                        "¿Estás seguro que deseas activar al usuario ${selectedUser?.first_name} ${selectedUser?.last_name}?"
                    else
                        "¿Estás seguro que deseas desactivar al usuario ${selectedUser?.first_name} ${selectedUser?.last_name}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // IMPLEMENTADO: Llamada real al API para activar/desactivar usuario
                        selectedUser?.id?.let { userId ->
                            Log.d("ManageUsersSection", "Llamando toggleUserStatus para usuario ID: $userId")
                            userViewModel.toggleUserStatus(userId)
                        } ?: run {
                            Log.e("ManageUsersSection", "Error: Usuario seleccionado no tiene ID")
                            successMessage = "Error: No se pudo obtener el ID del usuario"
                            showActionDialog = false
                            selectedUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActivating) primaryColor else Color.Red
                    ),
                    enabled = toggleUserStatusState !is UserViewModel.ToggleUserStatusState.Loading
                ) {
                    if (toggleUserStatusState is UserViewModel.ToggleUserStatusState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isActivating) "Activar" else "Desactivar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showActionDialog = false
                        selectedUser = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserManagementItem(
    user: UserProfile,
    onClick: () -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.active) Color.White else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !user.active -> Color.Gray.copy(alpha = 0.2f)
                            user.role == "Administrador" -> accentColor.copy(alpha = 0.2f)
                            else -> primaryColor.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.first_name?.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = when {
                        !user.active -> Color.Gray
                        user.role == "Administrador" -> accentColor
                        else -> primaryColor
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${user.first_name ?: ""} ${user.last_name ?: ""}".trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!user.active) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Inactivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "@${user.username ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                user.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = when (user.role) {
                            "Administrador" -> Icons.Default.Person
                            "Producción" -> Icons.Default.Build
                            else -> Icons.Default.List
                        },
                        contentDescription = null,
                        tint = when {
                            !user.active -> Color.Gray
                            user.role == "Administrador" -> accentColor
                            else -> primaryColor
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.role ?: "Sin rol",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            !user.active -> Color.Gray
                            user.role == "Administrador" -> accentColor
                            else -> primaryColor
                        }
                    )
                }
            }

            // Botón para activar/desactivar
            IconButton(
                onClick = onClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (user.active) Color.Red else Color.Green
                )
            ) {
                Icon(
                    imageVector = if (user.active) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = if (user.active) "Desactivar" else "Activar"
                )
            }
        }
    }
}
