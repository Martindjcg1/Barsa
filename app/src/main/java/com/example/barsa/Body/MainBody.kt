package com.example.barsa.Body

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.ProduccionesScreen
import com.example.barsa.R

@Composable
fun MainBody(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Logo decorativo en el fondo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .alpha(0.1f), // Hace el logo transparente
            contentScale = ContentScale.Fit
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentRoute) {
                "inventario" -> {
                    Text(
                        text = "Pantalla de Inventario",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                "producciones" -> {
                    /*Text(
                        text = "Pantalla de Producciones",
                        style = MaterialTheme.typography.headlineMedium
                    )*/
                    // Pasar onNavigate para cambiar de ruta en la navegación
                    ProduccionesScreen(onNavigate)
                }
                "usuario" -> {
                    Text(
                        text = "Ventana de Usuario",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bienvenido a tu perfil de usuario",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Agregar la vista al mainbody
                "cronometro" -> { CronometroScreen() }
            }
        }
    }
}
