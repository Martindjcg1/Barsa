package com.example.barsa.Body

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.barsa.Body.Inventory.InventoryScreen
import com.example.barsa.Body.Usuario.UsuarioBody

import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.EtapaSelector
import com.example.barsa.Producciones.ProduccionesScreen
import com.example.barsa.R
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel

import com.example.barsa.data.room.TiemposViewModel

@Composable
fun MainBody(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    tiemposViewModel: TiemposViewModel,
    papeletaViewModel: PapeletaViewModel,
    userViewModel: UserViewModel,
    onLogout: () -> Unit // NUEVO PARÁMETRO
) {
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    Log.d("MainBody", "$rol")
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
            if(rol.equals("Administrador") || rol.equals("SuperAdministrador")) {
                when {
                    currentRoute == "inventario" -> {
                        InventoryScreen(onNavigate)
                    }

                    currentRoute == "producciones" -> {
                        ProduccionesScreen(onNavigate, papeletaViewModel)
                    }

                    currentRoute == "usuario" -> {
                        UsuarioBody(onNavigate, userViewModel, onLogout = onLogout)
                    }

                    currentRoute.startsWith("cronometro/") -> {
                        val parts = currentRoute.removePrefix("cronometro/").split("°")
                        if (parts.size == 5) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            val Etapa = parts[4]
                            CronometroScreen(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                Etapa,
                                onNavigate,
                                tiemposViewModel,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos para el cronómetro")
                        }
                    }

                    currentRoute.startsWith("selector/") -> {
                        val parts = currentRoute.removePrefix("selector/").split("°")
                        if (parts.size == 4) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            EtapaSelector(TipoId, Folio, Fecha, Status, { etapaSeleccionada ->
                                onNavigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                            }, onNavigate, tiemposViewModel, papeletaViewModel, userViewModel)
                        } else {
                            Text("Error: Datos incompletos para el selector de etapa")
                        }
                    }
                }
            }
            else if(rol.equals("Produccion"))
            {
                when{
                currentRoute == "producciones" -> {
                    ProduccionesScreen(onNavigate, papeletaViewModel)
                }
                    currentRoute.startsWith("cronometro/") -> {
                        val parts = currentRoute.removePrefix("cronometro/").split("°")
                        if (parts.size == 5) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            val Etapa = parts[4]
                            CronometroScreen(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                Etapa,
                                onNavigate,
                                tiemposViewModel,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos para el cronómetro")
                        }
                    }

                    currentRoute.startsWith("selector/") -> {
                        val parts = currentRoute.removePrefix("selector/").split("°")
                        if (parts.size == 4) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            EtapaSelector(TipoId, Folio, Fecha, Status, { etapaSeleccionada ->
                                onNavigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                            }, onNavigate, tiemposViewModel, papeletaViewModel, userViewModel)
                        } else {
                            Text("Error: Datos incompletos para el selector de etapa")
                        }
                    }
                    currentRoute == "usuario" -> {
                        UsuarioBody(onNavigate, userViewModel, onLogout = onLogout)
                    }
                }
            }
            else if(rol.equals("Inventarios"))
            {
                when{
                    currentRoute == "inventario" -> {
                        InventoryScreen(onNavigate)
                    }
                    currentRoute == "usuario" -> {
                        UsuarioBody(onNavigate, userViewModel, onLogout = onLogout)
                    }
                }
            }
        }
    }
}
