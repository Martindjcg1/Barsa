package com.example.barsa.Usuario



import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.ui.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserSection(
    userViewModel: UserViewModel,
    primaryColor: Color,
    accentColor: Color
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Inventarios") }
    var passwordVisible by remember { mutableStateOf(false) }

    val registerState by userViewModel.registerState.collectAsState()

    val roles = listOf("Inventarios", "Producción", "Administrador")
    var showRoleDropdown by remember { mutableStateOf(false) }

    // Validaciones locales
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para validar campos
    fun validateFields(): Boolean {
        return when {
            nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                errorMessage = "Todos los campos son obligatorios excepto el correo electrónico"
                false
            }
            password != confirmPassword -> {
                errorMessage = "Las contraseñas no coinciden"
                false
            }
            email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Debes ingresar un correo válido o dejarlo vacío"
                false
            }
            else -> {
                errorMessage = null
                true
            }
        }
    }

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
            modifier = Modifier.fillMaxWidth()
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

        // Mostrar estado del registro
        when (registerState) {
            is UserViewModel.RegisterState.Loading -> {
                CircularProgressIndicator(
                    color = primaryColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is UserViewModel.RegisterState.Success -> {
                Text(
                    text = "Usuario creado correctamente",
                    color = Color.Green,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is UserViewModel.RegisterState.Error -> {
                Text(
                    text = (registerState as UserViewModel.RegisterState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> {
                // Mostrar errores de validación local
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                if (validateFields()) {
                    userViewModel.register(
                        nombre = nombre,
                        apellidos = apellido.ifBlank { null },
                        nombreUsuario = usuario,
                        email = email.ifBlank { null },
                        password = password,
                        rolString = selectedRole
                    )
                }
            },
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            ),
            enabled = registerState !is UserViewModel.RegisterState.Loading
        ) {
            Text("Crear Usuario")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListSection(
    userViewModel: UserViewModel,
    primaryColor: Color,
    accentColor: Color,
    onUserClick: (UserProfile) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val getUsersState by userViewModel.getUsersState.collectAsState()

    // Cargar usuarios al inicializar
    LaunchedEffect(Unit) {
        Log.d("UserListSection", "Cargando lista de usuarios")
        userViewModel.getUsers()
    }

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
                // Aplicar búsqueda en tiempo real
                if (query.isNotBlank()) {
                    userViewModel.getUsers(
                        nombre = query,
                        nombreUsuario = query,
                        email = query
                    )
                } else {
                    userViewModel.getUsers()
                }
            },
            placeholder = { Text("Buscar usuario...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        userViewModel.getUsers()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                val users = (getUsersState as UserViewModel.GetUsersState.Success).users
                Log.d("UserListSection", "Usuarios cargados: ${users.size}")
                users.forEach { user ->
                    Log.d("UserListSection", "Usuario: ${user.username}, ID: ${user.id}")
                }

                if (users.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron usuarios",
                            color = Color.Gray
                        )
                    }
                } else {
                    // Lista de usuarios
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { user ->
                            UserCard(
                                user = user,
                                primaryColor = primaryColor,
                                accentColor = accentColor,
                                onClick = {
                                    Log.d("UserListSection", "Usuario clickeado: ${user.username}, ID: ${user.id}")
                                    onUserClick(user)
                                }
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
                        text = "Cargando usuarios...",
                        color = Color.Gray
                    )
                }
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
            // Avatar
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
            Column(modifier = Modifier.weight(1f)) {
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

                // Mostrar ID para debug
                Text(
                    text = "ID: ${user.id ?: "No ID"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Icono para ver detalles
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Ver detalles",
                tint = Color.Gray
            )
        }
    }
}
