package com.example.barsa.Usuario


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
import androidx.compose.ui.unit.dp

// Reemplazar DeleteUserSection con ManageUsersSection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersSection(primaryColor: Color, accentColor: Color) {
    // Lista de usuarios de ejemplo
    val users = remember {
        mutableStateListOf(
            UserProfile("Juan", "Pérez", "juanperez", "juan@example.com", "15/01/2023", "Inventarios", true),
            UserProfile("Ana", "García", "anagarcia", "ana@example.com", "20/02/2023", "Producción", true),
            UserProfile("Carlos", "López", "carloslopez", "carlos@example.com", "10/03/2023", "Administrador", true),
            UserProfile("Laura", "Martínez", "lauramartinez", "laura@example.com", "05/04/2023", "Inventarios", false),
            UserProfile("Roberto", "Fernández", "robertof", "roberto@example.com", "12/05/2023", "Producción", true),
            UserProfile("María", "Rodríguez", "mariar", "maria@example.com", "18/06/2023", "Inventarios", false)
        )
    }

    var showActionDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf<List<UserProfile>>(users.toList()) }
    var showActiveOnly by remember { mutableStateOf(false) }
    var showInactiveOnly by remember { mutableStateOf(false) }

    // Función para actualizar la lista filtrada
    fun updateFilteredUsersList() {
        filteredUsers = users.filter { user ->
            // Filtrar por búsqueda
            val matchesQuery = searchQuery.isEmpty() ||
                    user.first_name?.contains(searchQuery, ignoreCase = true) == true ||
                    user.last_name?.contains(searchQuery, ignoreCase = true) == true ||
                    user.username?.contains(searchQuery, ignoreCase = true) == true ||
                    user.email?.contains(searchQuery, ignoreCase = true) == true ||
                    user.role?.contains(searchQuery, ignoreCase = true) == true

            // Filtrar por estado
            val matchesState = when {
                showActiveOnly -> user.active
                showInactiveOnly -> !user.active
                else -> true
            }

            matchesQuery && matchesState
        }
    }

    // Inicializar la lista filtrada
    LaunchedEffect(Unit) {
        updateFilteredUsersList()
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
                updateFilteredUsersList()
            },
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
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
                    updateFilteredUsersList()
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
                    updateFilteredUsersList()
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
                    updateFilteredUsersList()
                },
                label = { Text("Inactivos") },
                leadingIcon = if (showInactiveOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }

        successMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Green.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = Color.Green
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lista de usuarios
        if (filteredUsers.isEmpty()) {
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
                items(filteredUsers) { user ->
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
                        try {
                            // Activar/desactivar usuario de forma segura
                            selectedUser?.let { user ->
                                val index = users.indexOfFirst { it.username == user.username }
                                if (index != -1) {
                                    // Crear un nuevo objeto UserProfile con el estado actualizado
                                    val updatedUser = user.copy(active = !user.active)
                                    // Actualizar la lista mutable de forma segura
                                    users[index] = updatedUser

                                    // Mostrar mensaje de éxito
                                    successMessage = if (!user.active)
                                        "Usuario activado correctamente"
                                    else
                                        "Usuario desactivado correctamente"

                                    // Actualizar la lista filtrada
                                    updateFilteredUsersList()
                                }
                            }
                        } catch (e: Exception) {
                            // Manejar cualquier excepción que pueda ocurrir
                            successMessage = "Error: No se pudo actualizar el usuario"
                        } finally {
                            // Cerrar el diálogo
                            showActionDialog = false
                            selectedUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActivating) primaryColor else Color.Red
                    )
                ) {
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

private fun updateFilteredUsers(
    users: List<UserProfile>,
    query: String,
    showActiveOnly: Boolean,
    showInactiveOnly: Boolean,
    updateState: (List<UserProfile>) -> Unit
) {
    val filtered = users.filter { user ->
        // Filtrar por búsqueda
        val matchesQuery = query.isEmpty() ||
                user.first_name?.contains(query, ignoreCase = true) == true ||
                user.last_name?.contains(query, ignoreCase = true) == true ||
                user.username?.contains(query, ignoreCase = true) == true ||
                user.email?.contains(query, ignoreCase = true) == true ||
                user.role?.contains(query, ignoreCase = true) == true

        // Filtrar por estado
        val matchesState = when {
            showActiveOnly -> user.active
            showInactiveOnly -> !user.active
            else -> true
        }

        matchesQuery && matchesState
    }

    updateState(filtered)
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
                        text = "${user.first_name} ${user.last_name}",
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
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

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

