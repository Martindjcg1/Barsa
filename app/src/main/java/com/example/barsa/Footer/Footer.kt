package com.example.barsa.Footer

import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.barsa.data.retrofit.ui.UserViewModel

@Composable
fun Footer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    userViewModel: UserViewModel
) {
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    Log.d("MainBody", "$rol")
    NavigationBar {
        if (rol.equals("Administrador")) {
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
        else if(rol.equals("Produccion"))
        {
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
        else if (rol.equals("Inventarios"))
        {
            NavigationBarItem(
                icon = { Icon(Icons.Default.List, contentDescription = "Inventario") },
                label = { Text("Inventario") },
                selected = currentRoute == "inventario",
                onClick = { onNavigate("inventario") }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Usuario") },
                label = { Text("Usuario") },
                selected = currentRoute == "usuario",
                onClick = { onNavigate("usuario") }
            )
        }
    }
}