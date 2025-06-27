package com.example.barsa.Usuario


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.ui.UserViewModel

@Composable
fun PersonalInfoSection(
    userViewModel: UserViewModel, // Primero el ViewModel
    primaryColor: Color,
    lightBrown: Color
) {
    // Observar el resultado de la información del usuario
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    // Llamar a la función para obtener info cuando se carga el composable
    LaunchedEffect(Unit) {
        userViewModel.obtenerInfoUsuarioPersonal()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Información de tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Manejar el resultado usando tu lógica existente
        infoUsuarioResult?.let { result ->
            result.onSuccess { userInfo ->
                // Mostrar la información del usuario
                item { InfoField("Nombre", userInfo.nombre ?: "N/A", primaryColor) }
                item { InfoField("Apellidos", userInfo.apellidos ?: "N/A", primaryColor) }
                item { InfoField("Nombre de usuario", userInfo.nombreUsuario ?: "N/A", primaryColor) }
                item { InfoField("Correo Electrónico", userInfo.email ?: "N/A", primaryColor) }
                item { InfoField("Rol", userInfo.rol ?: "N/A", primaryColor) }
                // Si tienes más campos en UsuarioInfoResponse, agrégalos aquí
            }

            result.onFailure { error ->
                // Mostrar error
                item {
                    ErrorSection(
                        message = error.message ?: "Error desconocido",
                        primaryColor = primaryColor,
                        onRetry = { userViewModel.obtenerInfoUsuarioPersonal() }
                    )
                }
            }
        } ?: run {
            // Estado de carga inicial
            item {
                LoadingSection(primaryColor = primaryColor)
            }
        }
    }
}

@Composable
fun LoadingSection(primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando información...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ErrorSection(
    message: String,
    primaryColor: Color,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_dialog_alert),
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error al cargar información",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
fun InfoField(label: String, value: String, primaryColor: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = primaryColor.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = primaryColor.copy(alpha = 0.2f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSection(
    userViewModel: UserViewModel,
    primaryColor: Color,
    lightBrown: Color
) {
    // Observar estados
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()
    val updatePersonalInfoState by userViewModel.updatePersonalInfoState.collectAsState()

    // Estados locales para los campos editables
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Datos que se muestran como solo lectura
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    // Estados locales para mensajes
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar información del usuario al iniciar
    LaunchedEffect(Unit) {
        try {
            Log.d("EditProfileSection", "Cargando información inicial")
            userViewModel.obtenerInfoUsuarioPersonal()
        } catch (e: Exception) {
            Log.e("EditProfileSection", "Error al cargar información inicial", e)
        }
    }

    // Inicializar campos cuando se carga la información
    LaunchedEffect(infoUsuarioResult) {
        try {
            infoUsuarioResult?.onSuccess { userInfo ->
                // SIEMPRE actualizar los campos cuando llegue nueva información
                nombre = userInfo.nombre ?: ""
                apellidos = userInfo.apellidos ?: ""
                usuario = userInfo.nombreUsuario ?: ""
                email = userInfo.email ?: ""
                isInitialized = true
                Log.d("EditProfileSection", "Información actualizada: ${userInfo.nombreUsuario}, ${userInfo.email}")
            }
        } catch (e: Exception) {
            Log.e("EditProfileSection", "Error al inicializar campos", e)
        }
    }

    // Manejo del resultado de la actualización
    LaunchedEffect(updatePersonalInfoState) {
        try {
            when (updatePersonalInfoState) {
                is UserViewModel.UpdatePersonalInfoState.Success -> {
                    Log.d("EditProfileSection", "Actualización exitosa, recargando información")
                    successMessage = "Información actualizada correctamente"
                    errorMessage = null

                    // RECARGAR la información después de un pequeño delay
                    kotlinx.coroutines.delay(500) // Pequeño delay para que el servidor procese
                    userViewModel.obtenerInfoUsuarioPersonal()

                    // Limpiar mensaje después de 3 segundos
                    kotlinx.coroutines.delay(2500) // Total 3 segundos
                    successMessage = null
                }
                is UserViewModel.UpdatePersonalInfoState.Error -> {
                    Log.e("EditProfileSection", "Error en actualización: ${(updatePersonalInfoState as UserViewModel.UpdatePersonalInfoState.Error).message}")
                    errorMessage = (updatePersonalInfoState as UserViewModel.UpdatePersonalInfoState.Error).message
                    successMessage = null
                }
                is UserViewModel.UpdatePersonalInfoState.Idle -> {
                    // Limpiar mensajes cuando está idle
                    successMessage = null
                    errorMessage = null
                }
                else -> {
                    // No hacer nada para Loading
                }
            }
        } catch (e: Exception) {
            Log.e("EditProfileSection", "Error al manejar estado de actualización", e)
            errorMessage = "Error inesperado al procesar respuesta"
        }
    }

    // Limpiar estado al salir
    DisposableEffect(Unit) {
        onDispose {
            try {
                userViewModel.resetUpdatePersonalInfoState()
            } catch (e: Exception) {
                Log.e("EditProfileSection", "Error al limpiar estado", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Editar Perfil",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Actualiza tu nombre de usuario y correo electrónico.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (!isInitialized && infoUsuarioResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            // Campos de solo lectura - BLOQUEADOS
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Nombre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor.copy(alpha = 0.7f)
                )
                Text(
                    text = nombre.ifEmpty { "No disponible" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Divider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = primaryColor.copy(alpha = 0.2f)
                )
            }

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Apellidos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor.copy(alpha = 0.7f)
                )
                Text(
                    text = apellidos.ifEmpty { "No disponible" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Divider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = primaryColor.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campos editables
            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    try {
                        usuario = it
                        // Limpiar mensajes al editar
                        successMessage = null
                        errorMessage = null
                    } catch (e: Exception) {
                        Log.e("EditProfileSection", "Error al cambiar usuario", e)
                    }
                },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                enabled = updatePersonalInfoState !is UserViewModel.UpdatePersonalInfoState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = lightBrown,
                    focusedLabelColor = primaryColor
                ),
                isError = usuario.isBlank()
            )

            if (usuario.isBlank()) {
                Text(
                    text = "El nombre de usuario no puede estar vacío",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    try {
                        email = it
                        // Limpiar mensajes al editar
                        successMessage = null
                        errorMessage = null
                    } catch (e: Exception) {
                        Log.e("EditProfileSection", "Error al cambiar email", e)
                    }
                },
                label = { Text("Correo Electrónico (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = updatePersonalInfoState !is UserViewModel.UpdatePersonalInfoState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = lightBrown,
                    focusedLabelColor = primaryColor
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar mensajes
            successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = Color.Green,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            if (usuario.isNotBlank()) {
                                Log.d("EditProfileSection", "Iniciando actualización")
                                // Limpiar mensajes antes de empezar
                                successMessage = null
                                errorMessage = null

                                userViewModel.updatePersonalInfo(
                                    nombreUsuario = usuario.trim(),
                                    email = if (email.isBlank()) null else email.trim()
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("EditProfileSection", "Error al actualizar información", e)
                            errorMessage = "Error al procesar la solicitud"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    enabled = updatePersonalInfoState !is UserViewModel.UpdatePersonalInfoState.Loading &&
                            usuario.isNotBlank()
                ) {
                    if (updatePersonalInfoState is UserViewModel.UpdatePersonalInfoState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardando...")
                    } else {
                        Text("Guardar Cambios")
                    }
                }

                // NUEVO: Botón para recargar información manualmente
                OutlinedButton(
                    onClick = {
                        try {
                            Log.d("EditProfileSection", "Recargando información manualmente")
                            userViewModel.obtenerInfoUsuarioPersonal()
                        } catch (e: Exception) {
                            Log.e("EditProfileSection", "Error al recargar información", e)
                        }
                    },
                    modifier = Modifier.weight(0.5f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    enabled = updatePersonalInfoState !is UserViewModel.UpdatePersonalInfoState.Loading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSection(
    userViewModel: UserViewModel,
    primaryColor: Color,
    lightBrown: Color
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val changePasswordState by userViewModel.changePasswordState.collectAsState()

    // Validaciones locales
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para validar contraseña
    fun isPasswordValid(password: String): Boolean {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
    }

    // Función para validar campos
    fun validateFields(): Boolean {
        return when {
            currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                errorMessage = "Todos los campos son obligatorios"
                false
            }
            newPassword != confirmPassword -> {
                errorMessage = "Las contraseñas no coinciden"
                false
            }
            !isPasswordValid(newPassword) -> {
                errorMessage = "La nueva contraseña no cumple con los requisitos de seguridad"
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
            text = "Cambiar contraseña",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cambia la contraseña de tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Contraseña actual") },
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = lightBrown,
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = lightBrown,
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password requirements
        Text(
            text = "La contraseña debe cumplir con los siguientes criterios:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
            Text("• Mínimo 8 caracteres", style = MaterialTheme.typography.bodySmall)
            Text("• Al menos una letra mayúscula", style = MaterialTheme.typography.bodySmall)
            Text("• Al menos un número", style = MaterialTheme.typography.bodySmall)
            Text("• Al menos un carácter especial (como @, #, $)", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))

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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = lightBrown,
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar estado del cambio de contraseña
        when (changePasswordState) {
            is UserViewModel.ChangePasswordState.Loading -> {
                CircularProgressIndicator(
                    color = primaryColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is UserViewModel.ChangePasswordState.Success -> {
                Text(
                    text = "Contraseña actualizada correctamente",
                    color = Color.Green,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is UserViewModel.ChangePasswordState.Error -> {
                Text(
                    text = (changePasswordState as UserViewModel.ChangePasswordState.Error).message,
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
                    userViewModel.changePassword(currentPassword, newPassword)
                }
            },
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            ),
            enabled = changePasswordState !is UserViewModel.ChangePasswordState.Loading
        ) {
            Text("Cambiar Contraseña")
        }
    }
}