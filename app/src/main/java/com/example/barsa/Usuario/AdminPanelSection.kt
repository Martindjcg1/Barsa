package com.example.barsa.Usuario


import android.util.Log
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
fun AdminPanelSection(
    accentBrown: Color,
    goldAccent: Color,
    onOptionSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(goldAccent.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = goldAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Panel de Administrador",
                    style = MaterialTheme.typography.titleLarge,
                    color = accentBrown,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = goldAccent.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Opciones de administrador
            AdminOptionCard(
                icon = Icons.Default.Add,
                text = "Agregar Usuario",
                description = "Crear nuevas cuentas de usuario",
                onClick = { onOptionSelected("Agregar Usuario") },
                accentColor = goldAccent,
                primaryColor = accentBrown
            )

            AdminOptionCard(
                icon = Icons.Default.List,
                text = "Lista de Usuarios",
                description = "Ver y gestionar usuarios existentes",
                onClick = { onOptionSelected("Lista de Usuarios") },
                accentColor = goldAccent,
                primaryColor = accentBrown
            )

            AdminOptionCard(
                icon = Icons.Default.Delete,
                text = "Eliminar Usuario",
                description = "Eliminar cuentas de usuario",
                onClick = { onOptionSelected("Eliminar Usuario") },
                accentColor = goldAccent,
                primaryColor = accentBrown
            )
        }
    }
}

@Composable
fun AdminOptionCard(
    icon: ImageVector,
    text: String,
    description: String,
    onClick: () -> Unit,
    accentColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ir a",
                tint = accentColor
            )
        }
    }

    // Log para verificar que cada opción se está renderizando
    Log.d("UsuarioBody", "Opción de administrador renderizada: $text")
}

