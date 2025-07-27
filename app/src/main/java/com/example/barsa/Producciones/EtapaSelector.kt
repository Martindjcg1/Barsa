package com.example.barsa.Producciones

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
//import com.example.barsa.data.room.local.Detencion
//import com.example.barsa.data.room.local.Proceso
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtapaSelector(
    TipoId: String,
    Folio: Int,
    Fecha: String,
    Status: String,
    onEtapaSeleccionada: (String) -> Unit,
    onNavigate: (String) -> Unit,
    tiemposViewModel: TiemposViewModel,
    papeletaViewModel: PapeletaViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    val etapaState by papeletaViewModel.etapasState.collectAsState()
    val desactivacionState by papeletaViewModel.desactivacionState.collectAsState()
    val nombreUsuario by userViewModel.tokenManager.accessNombreUsuario.collectAsState(initial = "")

    val tiemposEnEjecucion by papeletaViewModel.tiemposEnEjecucion.collectAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }
    val rol by userViewModel.tokenManager.accessRol.collectAsState(initial = "")
    var isLoading by rememberSaveable { mutableStateOf(false) }

    // 🔄 Cargar datos cuando cambia el Folio
    LaunchedEffect(Unit) {
        papeletaViewModel.resetEtapasFinDis()
        papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
        papeletaViewModel.cargarUltimaDetencionActiva(Folio)
    }

    // ✅ Manejo de desactivación de detención
    LaunchedEffect(desactivacionState) {
        desactivacionState?.onSuccess { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetEtapasFinDis()
            papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
            papeletaViewModel.cargarUltimaDetencionActiva(Folio)
            papeletaViewModel.resetDesactivacionState()
            isLoading = false
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetDesactivacionState()
            isLoading = false
        }
    }

    TopAppBar(
        title = { Text("Etapas - $Folio", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = { onNavigate("producciones") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
        }
    )

    when (etapaState) {
        is PapeletaViewModel.EtapasState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PapeletaViewModel.EtapasState.Error -> {
            val errorMessage = (etapaState as PapeletaViewModel.EtapasState.Error).message
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    papeletaViewModel.resetEtapasState()
                    papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
                    papeletaViewModel.cargarUltimaDetencionActiva(Folio)
                }) {
                    Text("Reintentar")
                }
            }
        }

        is PapeletaViewModel.EtapasState.Success -> {
            val uiState by combine(
                papeletaViewModel.etapasFinalizadas,
                papeletaViewModel.etapasDisponibles,
                papeletaViewModel.ultimaDetencion
            ) { finalizadas, disponibles, detencion ->
                Triple(finalizadas, disponibles, detencion)
            }.collectAsState(initial = Triple(emptySet(), emptyList(), null))
            val (etapasFinalizadas, etapasDisponibles, detencion) = uiState
            val todasLasEtapas = listOf("Madera", "Producción", "Pintura", "Armado", "Tapiceria", "Empaque")
            val hayDetencionActiva = detencion?.activa == true

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(todasLasEtapas) { etapa ->
                        val esFinalizada = etapa in etapasFinalizadas
                        val esDisponible = etapa in etapasDisponibles
                        val tiempoActivo = tiemposEnEjecucion.firstOrNull { it.etapa == etapa }
                        val estaOcupadaPorOtro = (tiempoActivo != null) && (nombreUsuario != tiempoActivo.usuario)
                        val estaDeshabilitada = hayDetencionActiva || (!esFinalizada && !esDisponible) || estaOcupadaPorOtro

                        val onClickAction = {
                            if (esFinalizada || rol.equals("Administrador") || rol.equals("SuperAdministrador")) {
                                onNavigate("informeIndividual/${TipoId}°${Folio}°${Fecha}°${Status}°${etapa}")
                            } else if (esDisponible) {
                                val otraEtapa = when (etapa) {
                                    "Madera" -> "Producción"
                                    "Producción" -> "Madera"
                                    else -> null
                                }
                                val otraEtapaOcupada = otraEtapa?.let { alterna ->
                                    val t = tiemposEnEjecucion.firstOrNull { it.etapa == alterna }
                                    t != null && t.usuario != nombreUsuario
                                } ?: false

                                val isRun = otraEtapaOcupada.toString()

                                onNavigate("cronometro/${TipoId}°${Folio}°${Fecha}°${Status}°${etapa}°${isRun}")
                            }
                        }


                        Card(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(2.dp, if (hayDetencionActiva) Color(0xFFD91616) else if (esFinalizada) Color(0xFF237023) else if (esDisponible) MaterialTheme.colorScheme.primary else Color.Transparent),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    hayDetencionActiva -> Color(0xFFFFFFFF)
                                    tiempoActivo?.isRunning == true -> Color(0xFFFFEDEB)
                                    esFinalizada -> Color(0xFFFFFFFF)
                                    esDisponible -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !estaDeshabilitada, onClick = onClickAction)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = when {
                                        hayDetencionActiva -> Color(0xFFD91616)
                                        esFinalizada -> Color(0xFF237023)
                                        esDisponible -> MaterialTheme.colorScheme.primary
                                        else -> Color.Gray
                                    }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = etapa,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (esDisponible && !hayDetencionActiva) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (hayDetencionActiva || esFinalizada || esDisponible) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                                )

                                tiempoActivo?.let {
                                    if (nombreUsuario != it.usuario) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("En uso por: ${it.usuario}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    else
                                    {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Activo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if(rol.equals("Administrador") || rol.equals("SuperAdministrador")) {
                    IconButton(
                        onClick = { onNavigate("informeFolio/${TipoId}°${Folio}°${Fecha}°${Status}") },
                        modifier = Modifier
                            .border(.6.dp, Color(0x11000000), shape = CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.barras),
                            contentDescription = "Informe por Folio",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                if (hayDetencionActiva) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        enabled = !isLoading
                    ) {
                        Text("Detalles de detención", color = Color.White)
                    }
                }
            }

            if (showDialog) {
                DetencionDialog(
                    rol = rol,
                    detencion = detencion!!,
                    onClose = { showDialog = false },
                    onDesactivar = {
                        papeletaViewModel.desactivarDetencionTiempo(detencion.folio, detencion.etapa)
                       // papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
                        //papeletaViewModel.cargarUltimaDetencionActiva(Folio)
                        isLoading = true
                    }
                )
            }
        }
    }
}


@Composable
fun DetencionDialog(
    rol: String?,
    detencion: DetencionRemota,
    onClose: () -> Unit,
    onDesactivar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detención activa",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Red
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetalleDetencionItem("Folio Papeleta", detencion.folio.toString())
                DetalleDetencionItem("Etapa", detencion.etapa)
                DetalleDetencionItem("Motivo", detencion.motivo)
                DetalleDetencionItem("Fecha", detencion.fecha)
                DetalleDetencionItem("Estado", if (detencion.activa) "Activa" else "Inactiva")
            }
        },
        confirmButton = {
            if (rol == "Produccion") {
                Button(
                    onClick = {
                        onDesactivar()
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desactivar", color = Color.White)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onClose,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp
    )
}

@Composable
fun DetalleDetencionItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}


// Función para formatear fecha
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
