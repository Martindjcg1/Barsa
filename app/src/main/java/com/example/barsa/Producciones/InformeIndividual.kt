package com.example.barsa.Producciones

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.DetallePapeleta
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeIndividual(
    TipoId: String,
    Folio: Int,
    Fecha: String,
    Status: String,
    Etapa: String,
    onNavigate: (String) -> Unit,
    papeletaViewModel: PapeletaViewModel
) {
    val context = LocalContext.current
    val etapaState by papeletaViewModel.tiempoEtapasState.collectAsState()
    val tiempoDesdeDBA = papeletaViewModel.tiempoPorEtapa.collectAsState()
    val detencionState by papeletaViewModel.detencionesEtapaState.collectAsState()
    val detalle = papeletaViewModel.detalleActual.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Folio, Etapa) {
        //papeletaViewModel.resetTiempoEtapaState()
        papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
        papeletaViewModel.cargarDetencionesPorEtapa(Folio, Etapa)
    }

    LaunchedEffect(etapaState, detencionState) {
        (etapaState as? PapeletaViewModel.TiempoEtapaState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            //papeletaViewModel.resetTiempoEtapaState()
        }

        (detencionState as? PapeletaViewModel.DetencionesEtapaState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            //papeletaViewModel.resetDetencionesEtapaState()
        }
    }

    val tiempo = tiempoDesdeDBA.value

    if (etapaState is PapeletaViewModel.TiempoEtapaState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (etapaState is PapeletaViewModel.TiempoEtapaState.Error ||
        detencionState is PapeletaViewModel.DetencionesEtapaState.Error
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = {
                    onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //Text(text = "No se pudo cargar la información.", style = MaterialTheme.typography.bodyLarge, color = Color.Red)
                //Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    papeletaViewModel.resetTiempoEtapaState()
                    papeletaViewModel.resetDetencionesEtapaState()
                    papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                    papeletaViewModel.cargarDetencionesPorEtapa(Folio, Etapa)
                }) {
                    Text("Reintentar")
                }
            }
        }
        return
    }
    else if (tiempo == null)
    {
        TopAppBar(
            title = {Text("$Etapa", style = MaterialTheme.typography.titleMedium)},
            navigationIcon = {
                IconButton(onClick = {
                    onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            },
            actions = {
                if (detalle.isNotEmpty()) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .border(.6.dp, Color(0x11000000), shape = CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.detalles), contentDescription = "Ver detalles", tint = Color.Black, modifier = Modifier.background(Color.White).size(32.dp))
                    }
                }
            }
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
        {
            Text("Captura de tiempo sin iniciar")
        }
    }
    else
    {
        TopAppBar(
            title = {
                Text("$Etapa - ${if (tiempo.isFinished) "Finalizada" else "En curso"}", style = MaterialTheme.typography.titleMedium)
            },
            navigationIcon = {
                IconButton(onClick = {
                    onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            },
            actions = {
                if (detalle.isNotEmpty()) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .border(.6.dp, Color(0x11000000), shape = CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.detalles), contentDescription = "Ver detalles", tint = Color.Black, modifier = Modifier.background(Color.White).size(32.dp))
                    }
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    tiempo?.let {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InfoText("Folio", it.procesoFolio.toString())
                Divider()
                InfoText("", "Información del tiempo")
            }

            it.fechaInicio?.let { fechaInicio ->
                item { InfoText("Inicio", formatearFechaHoraLinda(fechaInicio)) }
            }

            it.fechaFin?.let { fechaFin ->
                item { InfoText("Finalización", formatearFechaHoraLinda(fechaFin)) }
            }

            item {
                InfoText("Tiempo total", formatearTiempo(it.tiempo))
            }

            if (detencionState is PapeletaViewModel.DetencionesEtapaState.Success) {
                val detenciones = (detencionState as PapeletaViewModel.DetencionesEtapaState.Success).lista

                if (detenciones.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Detenciones", style = MaterialTheme.typography.titleMedium)
                    }

                    items(detenciones) { detencion ->
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoText("Motivo", detencion.motivo)
                        InfoText("Fecha", formatearFechaHoraLinda(detencion.fecha))
                    }
                }
            }
        }
    }

    // Mostrar el diálogo si showDialog está activo
    if (showDialog) {
        DetallePapeletaDialog(
            detalles = detalle,
            onDismiss = { showDialog = false }
        )
    }
}


@Composable
fun InfoText(label: String, value: String) {
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


@SuppressLint("DefaultLocale")
fun formatearTiempo(segundos: Int): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    val secs = segundos % 60
    return String.format("%02d:%02d:%02d", horas, minutos, secs)
}

fun formatearFechaHoraLinda(fechaHora: String): String {
    return try {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        val fecha = formatoEntrada.parse(fechaHora)
        formatoSalida.format(fecha!!)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}
