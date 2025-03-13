package com.example.barsa.Usuario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UserOptionsSection(
    primaryBrown: Color,
    lightBrown: Color,
    onOptionSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = lightBrown.copy(alpha = 0.2f)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Opciones de Usuario",
                style = MaterialTheme.typography.titleLarge,
                color = primaryBrown,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Opciones de usuario
            ProfileOption(
                icon = Icons.Default.Person,
                text = "Información Personal",
                onClick = { onOptionSelected("Información Personal") },
                primaryColor = primaryBrown
            )
            ProfileOption(
                icon = Icons.Default.Edit,
                text = "Editar Perfil",
                onClick = { onOptionSelected("Cambiar Datos") },
                primaryColor = primaryBrown
            )
            ProfileOption(
                icon = Icons.Default.Lock,
                text = "Cambiar Contraseña",
                onClick = { onOptionSelected("Cambiar contraseña") },
                primaryColor = primaryBrown
            )
        }
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

