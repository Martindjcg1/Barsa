package com.example.barsa.Producciones

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.barsa.CronometroService
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import com.example.barsa.data.room.TiemposViewModel
import com.example.barsa.data.room.local.Detencion
//import com.example.barsa.data.room.local.Proceso
import com.example.barsa.data.room.local.Tiempo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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

    // 🔄 Cargar datos cuando cambia el Folio
    LaunchedEffect(Unit) {
        papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
        papeletaViewModel.cargarUltimaDetencionActiva(Folio)
    }

    // ✅ Manejo de desactivación de detención
    LaunchedEffect(desactivacionState) {
        desactivacionState?.onSuccess { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
            papeletaViewModel.cargarUltimaDetencionActiva(Folio)
            papeletaViewModel.resetDesactivacionState()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetDesactivacionState()
        }
    }

    TopAppBar(
        title = { Text("Etapas", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = { onNavigate("producciones") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
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
                            if (esFinalizada) {
                                onNavigate("informeIndividual/${TipoId}°${Folio}°${Fecha}°${Status}°${etapa}")
                            } else if (esDisponible) {
                                onEtapaSeleccionada(etapa)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(2.dp, if (hayDetencionActiva) Color.Red else if (esFinalizada) Color.Green else if (esDisponible) MaterialTheme.colorScheme.primary else Color.Transparent),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    hayDetencionActiva -> Color(0xFFFCD7DB)
                                    esFinalizada -> MaterialTheme.colorScheme.secondaryContainer
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
                                        hayDetencionActiva -> Color.Red
                                        esFinalizada -> Color.Green
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
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate("informeFolio/${TipoId}°${Folio}°${Fecha}°${Status}") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Informe general de papeleta", color = Color.White)
                }

                if (hayDetencionActiva && rol == "Produccion") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Detalles de detención", color = Color.White)
                    }
                }
            }

            if (showDialog) {
                DetencionDialog(
                    detencion = detencion!!,
                    onClose = { showDialog = false },
                    onDesactivar = {
                        papeletaViewModel.desactivarDetencionTiempo(detencion.folio, detencion.etapa)
                       // papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
                        //papeletaViewModel.cargarUltimaDetencionActiva(Folio)
                    }
                )
            }
        }
    }
}


@Composable
fun DetencionDialog(
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
