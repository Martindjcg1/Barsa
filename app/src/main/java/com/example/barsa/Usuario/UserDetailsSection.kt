package com.example.barsa.Usuario


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.barsa.data.retrofit.ui.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsSection(
    userViewModel: UserViewModel,
    userId: String,
    primaryColor: Color,
    accentColor: Color
) {
    val getUserDetailState by userViewModel.getUserDetailState.collectAsState()
    val updateUserState by userViewModel.updateUserState.collectAsState()
    val toggleUserStatusState by userViewModel.toggleUserStatusState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Inventarios") }
    var isActive by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showActivationDialog by remember { mutableStateOf(false) }

    val roles = listOf("Inventarios", "Producción", "Administrador")

    // Cargar información del usuario al inicializar
    LaunchedEffect(userId) {
        userViewModel.getUserDetail(userId)
    }

    // Actualizar campos cuando se carga la información del usuario
    LaunchedEffect(getUserDetailState) {
        if (getUserDetailState is UserViewModel.GetUserDetailState.Success) {
            val user = (getUserDetailState as UserViewModel.GetUserDetailState.Success).user
            nombre = user.nombre
            apellido = user.apellidos ?: ""
            usuario = user.nombreUsuario
            email = user.email ?: ""
            selectedRole = user.rol
            isActive = user.estado
        }
    }

    // Manejo del resultado de la actualización de usuario
    LaunchedEffect(updateUserState) {
        when (updateUserState) {
            is UserViewModel.UpdateUserState.Success -> {
                Log.d("UserDetailsSection", "Usuario actualizado exitosamente")
                successMessage = "Usuario actualizado correctamente"
                isEditing = false
                // Limpiar campos de contraseña
                password = ""
                confirmPassword = ""

                // Limpiar mensaje después de 3 segundos
                kotlinx.coroutines.delay(3000)
                successMessage = null
            }
            is UserViewModel.UpdateUserState.Error -> {
                Log.e("UserDetailsSection", "Error en actualización: ${(updateUserState as UserViewModel.UpdateUserState.Error).message}")
                successMessage = "Error: ${(updateUserState as UserViewModel.UpdateUserState.Error).message}"
            }
            else -> {}
        }
    }

    // Manejo del resultado de activar/desactivar usuario
    LaunchedEffect(toggleUserStatusState) {
        when (toggleUserStatusState) {
            is UserViewModel.ToggleUserStatusState.Success -> {
                Log.d("UserDetailsSection", "Estado del usuario cambiado exitosamente")
                successMessage = if (isActive) "Usuario desactivado correctamente" else "Usuario activado correctamente"

                // Limpiar mensaje después de 3 segundos
                kotlinx.coroutines.delay(3000)
                successMessage = null
            }
            is UserViewModel.ToggleUserStatusState.Error -> {
                Log.e("UserDetailsSection", "Error al cambiar estado: ${(toggleUserStatusState as UserViewModel.ToggleUserStatusState.Error).message}")
                successMessage = "Error: ${(toggleUserStatusState as UserViewModel.ToggleUserStatusState.Error).message}"
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when (getUserDetailState) {
            is UserViewModel.GetUserDetailState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            is UserViewModel.GetUserDetailState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${(getUserDetailState as UserViewModel.GetUserDetailState.Error).message}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { userViewModel.getUserDetail(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is UserViewModel.GetUserDetailState.Success -> {
                val user = (getUserDetailState as UserViewModel.GetUserDetailState.Success).user

                // Encabezado con avatar y nombre
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar grande
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isActive -> Color.Gray.copy(alpha = 0.2f)
                                    user.rol == "Administrador" -> accentColor.copy(alpha = 0.2f)
                                    else -> primaryColor.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.nombre.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = when {
                                !isActive -> Color.Gray
                                user.rol == "Administrador" -> accentColor
                                else -> primaryColor
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detalles del Usuario",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isActive) {
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
                            text = "Información completa y edición",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Botón para activar/desactivar
                    Button(
                        onClick = { showActivationDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.Red else Color.Green
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = if (isActive) "Desactivar usuario" else "Activar usuario"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isActive) "Desactivar" else "Activar")
                    }

                    // Botón para alternar modo edición
                    Button(
                        onClick = { isEditing = !isEditing },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) Color.Gray else primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Cancelar edición" else "Editar usuario"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEditing) "Cancelar" else "Editar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje de éxito
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

                // Campos de información/edición
                if (isEditing) {
                    // Modo edición
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
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
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
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
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Campos de contraseña (nuevos)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva contraseña") },
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
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar nueva contraseña") },
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
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
                        )
                    )

                    if (password.isNotEmpty() || confirmPassword.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Password requirements
                        Text(
                            text = "La contraseña debe cumplir con los siguientes criterios:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            Text("• Mínimo 8 caracteres", style = MaterialTheme.typography.bodySmall)
                            Text("• Al menos una letra mayúscula", style = MaterialTheme.typography.bodySmall)
                            Text("• Al menos un número", style = MaterialTheme.typography.bodySmall)
                            Text("• Al menos un carácter especial (como @, #, $)", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                unfocusedBorderColor = accentColor.copy(alpha = 0.5f)
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

                    // Botón para guardar cambios
                    Button(
                        onClick = {
                            // Validar contraseñas si se han ingresado
                            if ((password.isNotEmpty() || confirmPassword.isNotEmpty()) && password != confirmPassword) {
                                successMessage = "Error: Las contraseñas no coinciden"
                            } else {
                                // Llamar al API para actualizar usuario
                                userViewModel.updateUser(
                                    userId = userId,
                                    nombre = nombre.takeIf { it.isNotBlank() },
                                    apellidos = apellido.takeIf { it.isNotBlank() },
                                    nombreUsuario = usuario.takeIf { it.isNotBlank() },
                                    email = email.takeIf { it.isNotBlank() },
                                    password = password.takeIf { it.isNotBlank() },
                                    rol = selectedRole,
                                    estado = null // No cambiar el estado desde aquí
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = updateUserState !is UserViewModel.UpdateUserState.Loading
                    ) {
                        if (updateUserState is UserViewModel.UpdateUserState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardando...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Guardar cambios"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cambios")
                        }
                    }
                } else {
                    // Modo visualización
                    UserInfoField("Nombre", nombre, primaryColor)
                    UserInfoField("Apellido", apellido, primaryColor)
                    UserInfoField("Nombre de usuario", usuario, primaryColor)
                    UserInfoField("Correo electrónico", email, primaryColor)
                    UserInfoField("Rol", selectedRole, primaryColor)
                    UserInfoField("ID de usuario", user.id, primaryColor)
                    UserInfoField("Estado", if (isActive) "Activo" else "Inactivo", primaryColor)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            is UserViewModel.GetUserDetailState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cargando información del usuario...",
                        color = Color.Gray
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            userViewModel.resetUpdateUserState()
            userViewModel.resetToggleUserStatusState()
        }
    }

    // Diálogo de confirmación para activar/desactivar usuario
    if (showActivationDialog) {
        AlertDialog(
            onDismissRequest = { showActivationDialog = false },
            title = { Text(if (isActive) "Desactivar Usuario" else "Activar Usuario") },
            text = {
                Text(
                    if (isActive)
                        "¿Estás seguro que deseas desactivar al usuario ${nombre} ${apellido}? El usuario no podrá acceder al sistema mientras esté desactivado."
                    else
                        "¿Estás seguro que deseas activar al usuario ${nombre} ${apellido}? El usuario podrá acceder al sistema nuevamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Llamar al API específico para cambiar el estado del usuario
                        userViewModel.toggleUserStatus(userId)
                        showActivationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color.Red else Color.Green
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
                    Text(if (isActive) "Desactivar" else "Activar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showActivationDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserInfoField(label: String, value: String, primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = primaryColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value.ifEmpty { "No especificado" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}
