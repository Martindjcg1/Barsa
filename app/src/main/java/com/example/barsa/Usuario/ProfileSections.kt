package com.example.barsa.Usuario


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.ui.UserViewModel

@Composable
fun PersonalInfoSection(primaryColor: Color, lightBrown: Color) {
    // Datos de ejemplo para mostrar
    val userProfile = UserFakeProfile(
        first_name = "Martin",
        last_name = "Castañeda",
        username = "martindjcg",
        email = "martindjcg@gmail.com",
        date_joined = "01/01/2023",
        role = "Administrador"
    )

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

        item { InfoField("Nombre", userProfile.first_name ?: "N/A", primaryColor) }
        item { InfoField("Apellido", userProfile.last_name ?: "N/A", primaryColor) }
        item { InfoField("Nombre de usuario", userProfile.username ?: "N/A", primaryColor) }
        item { InfoField("Correo Electrónico", userProfile.email ?: "N/A", primaryColor) }
        item { InfoField("Rol", userProfile.role ?: "N/A", primaryColor) }
        item { InfoField("Fecha de registro", userProfile.date_joined ?: "N/A", primaryColor) }
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
fun EditProfileSection(primaryColor: Color, lightBrown: Color) {
    // Datos fijos que no se pueden cambiar
    val nombre = "Martin"
    val apellido = "Castañeda"

    // Datos que se pueden editar
    var usuario by remember { mutableStateOf("martindjcg") }
    var email by remember { mutableStateOf("martindjcg@gmail.com") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

        // Mostrar nombre y apellido como campos de solo lectura
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Nombre",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryColor.copy(alpha = 0.7f)
            )
            Text(
                text = nombre,
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
                text = "Apellido",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryColor.copy(alpha = 0.7f)
            )
            Text(
                text = apellido,
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
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = lightBrown,
                focusedLabelColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = lightBrown,
                focusedLabelColor = primaryColor
            )
        )
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
                // Simulación de actualización exitosa
                successMessage = "Perfil actualizado correctamente"
            },
            modifier = Modifier.align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            )
        ) {
            Text("Guardar Cambios")
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
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