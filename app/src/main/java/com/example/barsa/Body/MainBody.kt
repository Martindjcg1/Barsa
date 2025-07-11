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
import com.example.barsa.Network.NetworkMonitor

import com.example.barsa.Producciones.CronometroScreen
import com.example.barsa.Producciones.EtapaSelector
import com.example.barsa.Producciones.InformeFolio
import com.example.barsa.Producciones.InformeIndividual
import com.example.barsa.Producciones.InformePeriodo
import com.example.barsa.Producciones.ProduccionesScreen
import com.example.barsa.R
import com.example.barsa.data.retrofit.ui.InventoryViewModel
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
    inventoryViewModel: InventoryViewModel,
    onLogout: () -> Unit, // NUEVO PARÁMETRO
    networkMonitor: NetworkMonitor
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
                        InventoryScreen(onNavigate, inventoryViewModel, userViewModel)
                    }

                    currentRoute == "producciones" -> {
                        ProduccionesScreen(onNavigate, papeletaViewModel, userViewModel)
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
                                papeletaViewModel,
                                networkMonitor
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
                                if(rol.equals("Administrador") || rol.equals("SuperAdministrador")) {
                                    onNavigate("informeIndividual/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                                }
                                else
                                {
                                    onNavigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                                }
                            }, onNavigate, tiemposViewModel, papeletaViewModel, userViewModel)
                        } else {
                            Text("Error: Datos incompletos para el selector de etapa")
                        }
                    }

                    currentRoute.startsWith("informeIndividual/") -> {
                        val parts = currentRoute.removePrefix("informeIndividual/").split("°")
                        if (parts.size == 5) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            val Etapa = parts[4]
                            InformeIndividual(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                Etapa,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }

                    /*currentRoute.startsWith("informeFolio/") -> {
                        val parts = currentRoute.removePrefix("informeFolio/").split("°")
                        if (parts.size == 5) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            val Etapa = parts[4]
                            InformeFolio(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                Etapa,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }*/
                    currentRoute.startsWith("informeFolio/") -> {
                        val parts = currentRoute.removePrefix("informeFolio/").split("°")
                        //if (parts.size == 5) {
                        if (parts.size == 4) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            //val Etapa = parts[4]
                            InformeFolio(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                //Etapa,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }

                    currentRoute.startsWith("informePeriodo/") -> {
                        val parts = currentRoute.removePrefix("informePeriodo/").split("°")
                        //if (parts.size == 5) {
                        if (parts.size == 2) {
                            val fechaInicio = parts[0]
                            val fechaFin = parts[1]
                            InformePeriodo(
                                fechaInicio,
                                fechaFin,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }

                }

            }
            else if(rol.equals("Produccion"))
            {
                when{
                currentRoute == "producciones" -> {
                    ProduccionesScreen(onNavigate, papeletaViewModel, userViewModel)
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
                                papeletaViewModel,
                                networkMonitor
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
                                if(rol.equals("Administrador") || rol.equals("SuperAdministrador")) {
                                    onNavigate("informeIndividual/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                                }
                                else
                                {
                                    onNavigate("cronometro/$TipoId°$Folio°$Fecha°$Status°$etapaSeleccionada")
                                }
                            }, onNavigate, tiemposViewModel, papeletaViewModel, userViewModel)
                        } else {
                            Text("Error: Datos incompletos para el selector de etapa")
                        }
                    }

                    currentRoute.startsWith("informeIndividual/") -> {
                        val parts = currentRoute.removePrefix("informeIndividual/").split("°")
                        if (parts.size == 5) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            val Etapa = parts[4]
                            InformeIndividual(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                Etapa,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }

                    currentRoute.startsWith("informeFolio/") -> {
                        val parts = currentRoute.removePrefix("informeFolio/").split("°")
                        //if (parts.size == 5) {
                        if (parts.size == 4) {
                            val TipoId = parts[0]
                            val Folio = parts[1].toIntOrNull() ?: 0
                            val Fecha = parts[2]
                            val Status = parts[3]
                            //val Etapa = parts[4]
                            InformeFolio(
                                TipoId,
                                Folio,
                                Fecha,
                                Status,
                                //Etapa,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
                        }
                    }

                    currentRoute.startsWith("informePeriodo/") -> {
                        val parts = currentRoute.removePrefix("informePeriodo/").split("°")
                        //if (parts.size == 5) {
                        if (parts.size == 2) {
                            val fechaInicio = parts[0]
                            val fechaFin = parts[1]
                            InformePeriodo(
                                fechaInicio,
                                fechaFin,
                                onNavigate,
                                papeletaViewModel
                            )
                        } else {
                            Text("Error: Datos incompletos")
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
                        InventoryScreen(onNavigate, inventoryViewModel, userViewModel)
                    }
                    currentRoute == "usuario" -> {
                        UsuarioBody(onNavigate, userViewModel, onLogout = onLogout)
                    }
                }
            }
        }
    }
}
