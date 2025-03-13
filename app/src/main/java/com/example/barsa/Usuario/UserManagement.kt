package com.example.barsa.Usuario



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.barsa.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserSection(primaryColor: Color, accentColor: Color) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Inventarios") }
    var passwordVisible by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roles = listOf("Inventarios", "Producción", "Administrador")
    var showRoleDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Agregar Nuevo Usuario",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Completa el formulario para crear un nuevo usuario.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Campos para el nuevo usuario
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de rol
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = { },
                label = { Text("Rol") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showRoleDropdown = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar rol"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                    focusedLabelColor = primaryColor
                )
            )

            DropdownMenu(
                expanded = showRoleDropdown,
                onDismissRequest = { showRoleDropdown = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            selectedRole = role
                            showRoleDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        successMessage?.let {
            Text(
                text = it,
                color = Color.Green,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    errorMessage = "Todos los campos son obligatorios excepto el correo electrónico"
                    successMessage = null
                } else if (password != confirmPassword) {
                    errorMessage = "Las contraseñas no coinciden"
                    successMessage = null
                } else {
                    // Simulación de creación de usuario exitosa
                    successMessage = "Usuario creado correctamente"
                    errorMessage = null
                    // Limpiar campos
                    nombre = ""
                    apellido = ""
                    usuario = ""
                    email = ""
                    password = ""
                    confirmPassword = ""
                    selectedRole = "Inventarios"
                }
            },
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            Text("Crear Usuario")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListSection(primaryColor: Color, accentColor: Color, onUserClick: (UserProfile) -> Unit) {
    // Lista de usuarios de ejemplo
    val users = remember {
        listOf(
            UserProfile("Juan", "Pérez", "juanperez", "juan@example.com", "15/01/2023", "Inventarios"),
            UserProfile("Ana", "García", "anagarcia", "ana@example.com", "20/02/2023", "Producción"),
            UserProfile("Carlos", "López", "carloslopez", "carlos@example.com", "10/03/2023", "Administrador"),
            UserProfile("Laura", "Martínez", "lauramartinez", "laura@example.com", "05/04/2023", "Inventarios"),
            UserProfile("Roberto", "Fernández", "robertof", "roberto@example.com", "12/05/2023", "Producción"),
            UserProfile("María", "Rodríguez", "mariar", "maria@example.com", "18/06/2023", "Inventarios"),
            UserProfile("Pedro", "Sánchez", "pedros", "pedro@example.com", "22/07/2023", "Administrador")
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(users) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Lista de Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                filteredUsers = if (query.isEmpty()) {
                    users
                } else {
                    users.filter { user ->
                        user.first_name?.contains(query, ignoreCase = true) == true ||
                                user.last_name?.contains(query, ignoreCase = true) == true ||
                                user.username?.contains(query, ignoreCase = true) == true ||
                                user.email?.contains(query, ignoreCase = true) == true ||
                                user.role?.contains(query, ignoreCase = true) == true
                    }
                }
            },
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de usuarios
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredUsers) { user ->
                UserCard(
                    user = user,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    onClick = { onUserClick(user) }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: UserProfile,
    primaryColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        when (user.role) {
                            "Administrador" -> accentColor.copy(alpha = 0.2f)
                            else -> primaryColor.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.first_name?.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = when (user.role) {
                        "Administrador" -> accentColor
                        else -> primaryColor
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.first_name} ${user.last_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                            "Administrador" -> Icons.Default.AccountBox
                            "Producción" -> Icons.Default.Build
                            else -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = when (user.role) {
                            "Administrador" -> accentColor
                            else -> primaryColor
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.role ?: "Sin rol",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (user.role) {
                            "Administrador" -> accentColor
                            else -> primaryColor
                        }
                    )
                }
            }

            // Icono para ver detalles
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Ver detalles",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun DeleteUserSection(primaryColor: Color, accentColor: Color) {
    // Lista de usuarios de ejemplo
    val users = remember {
        mutableStateListOf(
            UserProfile("Juan", "Pérez", "juanperez", "juan@example.com", "15/01/2023", "Inventarios"),
            UserProfile("Ana", "García", "anagarcia", "ana@example.com", "20/02/2023", "Producción"),
            UserProfile("Carlos", "López", "carloslopez", "carlos@example.com", "10/03/2023", "Administrador"),
            UserProfile("Laura", "Martínez", "lauramartinez", "laura@example.com", "05/04/2023", "Inventarios")
        )
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Eliminar Usuario",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Selecciona un usuario para eliminarlo del sistema.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

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
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(users) { user ->
                DeleteUserListItem(
                    user = user,
                    onClick = {
                        userToDelete = user
                        showDeleteDialog = true
                    },
                    primaryColor = primaryColor,
                    accentColor = accentColor
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar usuario
    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                userToDelete = null
            },
            title = { Text("Eliminar Usuario") },
            text = {
                Text("¿Estás seguro que deseas eliminar al usuario ${userToDelete?.first_name} ${userToDelete?.last_name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Eliminar usuario
                        userToDelete?.let { user ->
                            users.remove(user)
                            successMessage = "Usuario eliminado correctamente"
                        }
                        showDeleteDialog = false
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        userToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DeleteUserListItem(
    user: UserProfile,
    onClick: () -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.first_name?.firstOrNull()?.toString() ?: "?",
                    color = primaryColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.first_name} ${user.last_name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Rol: ${user.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when(user.role) {
                        "Administrador" -> accentColor
                        else -> primaryColor
                    }
                )
            }

            IconButton(
                onClick = onClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar"
                )
            }
        }
    }
}

