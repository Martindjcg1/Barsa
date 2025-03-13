package com.example.barsa.Body.Usuario

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.barsa.Usuario.AddUserSection
import com.example.barsa.Usuario.AdminPanelSection
import com.example.barsa.Usuario.ChangePasswordSection
import com.example.barsa.Usuario.DeleteUserSection
import com.example.barsa.Usuario.EditProfileSection
import com.example.barsa.Usuario.PersonalInfoSection
import com.example.barsa.Usuario.UserDetailsSection
import com.example.barsa.Usuario.UserListSection
import com.example.barsa.Usuario.UserOptionsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioBody(onNavigate: (String) -> Unit) {
    var showSection by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Simular que el usuario actual es administrador
    val isAdmin = true

    // Definir colores de la mueblería
    val primaryBrown = Color(0xFF8B4513) // Marrón oscuro
    val lightBrown = Color(0xFFDEB887)   // Marrón claro
    val accentBrown = Color(0xFF654321)  // Marrón medio
    val goldAccent = Color(0xFFD4AF37)   // Dorado para acentos

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
                        // Texto con fondo semitransparente para mejor visibilidad
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Martin Castañeda",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "martindjcg@gmail.com",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = goldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Administrador",
                                            color = goldAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
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
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión")
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
                        IconButton(onClick = { showSection = null }) {
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
                        "Información Personal" -> PersonalInfoSection(primaryBrown, lightBrown)
                        "Cambiar Datos" -> EditProfileSection(primaryBrown, lightBrown)
                        "Cambiar contraseña" -> ChangePasswordSection(primaryBrown, lightBrown)
                        "Agregar Usuario" -> AddUserSection(accentBrown, goldAccent)
                        "Lista de Usuarios" -> UserListSection(accentBrown, goldAccent) { user ->
                            showSection = "Detalles de Usuario"
                        }
                        "Eliminar Usuario" -> DeleteUserSection(accentBrown, goldAccent)
                        "Detalles de Usuario" -> UserDetailsSection(accentBrown, goldAccent)
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
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onNavigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

