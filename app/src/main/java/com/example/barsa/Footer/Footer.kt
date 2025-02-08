package com.example.barsa.Footer

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person

@Composable
fun Footer(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Inventario") },
            label = { Text("Inventario") },
            selected = currentRoute == "inventario",
            onClick = { onNavigate("inventario") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Build, contentDescription = "Producciones") },
            label = { Text("Producciones") },
            selected = currentRoute == "producciones",
            onClick = { onNavigate("producciones") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Usuario") },
            label = { Text("Usuario") },
            selected = currentRoute == "usuario",
            onClick = { onNavigate("usuario") }
        )
    }
}