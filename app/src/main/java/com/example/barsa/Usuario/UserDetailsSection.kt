package com.example.barsa.Usuario


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
import androidx.compose.ui.unit.dp
import com.example.barsa.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsSection(primaryColor: Color, accentColor: Color) {
    // Usuario de ejemplo para mostrar detalles
    val user = remember {
        UserProfile(
            first_name = "Carlos",
            last_name = "López",
            username = "carloslopez",
            email = "carlos@example.com",
            date_joined = "10/03/2023",
            role = "Administrador",
            active = true
        )
    }

    var nombre by remember { mutableStateOf(user.first_name ?: "") }
    var apellido by remember { mutableStateOf(user.last_name ?: "") }
    var usuario by remember { mutableStateOf(user.username ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var selectedRole by remember { mutableStateOf(user.role ?: "Inventarios") }
    var isActive by remember { mutableStateOf(user.active) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showActivationDialog by remember { mutableStateOf(false) }

    val roles = listOf("Inventarios", "Producción", "Administrador")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
                            user.role == "Administrador" -> accentColor.copy(alpha = 0.2f)
                            else -> primaryColor.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.first_name?.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = when {
                        !isActive -> Color.Gray
                        user.role == "Administrador" -> accentColor
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
                        // Simulación de guardado exitoso
                        successMessage = "Cambios guardados correctamente"
                        isEditing = false
                        // Limpiar campos de contraseña
                        password = ""
                        confirmPassword = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Guardar cambios"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios")
            }
        } else {
            // Modo visualización
            InfoField("Nombre", nombre, primaryColor)
            InfoField("Apellido", apellido, primaryColor)
            InfoField("Nombre de usuario", usuario, primaryColor)
            InfoField("Correo electrónico", email, primaryColor)
            InfoField("Rol", selectedRole, primaryColor)
            InfoField("Fecha de registro", user.date_joined ?: "N/A", primaryColor)
            InfoField("Estado", if (isActive) "Activo" else "Inactivo", primaryColor)

            Spacer(modifier = Modifier.height(24.dp))
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
                        isActive = !isActive
                        successMessage = if (isActive)
                            "Usuario activado correctamente"
                        else
                            "Usuario desactivado correctamente"
                        showActivationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color.Red else Color.Green
                    )
                ) {
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

