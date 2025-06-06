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
import com.example.barsa.data.room.TiemposViewModel
import com.example.barsa.data.room.local.Detencion
import com.example.barsa.data.room.local.Proceso
import com.example.barsa.data.room.local.Tiempo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
@Composable
fun CronometroScreen(TipoId: String, Folio: Int, Fecha: String, Status: String, tiemposViewModel: TiemposViewModel) {

var time by rememberSaveable { mutableStateOf(0) }
var isRunning by rememberSaveable { mutableStateOf(false) }
var showDialog by rememberSaveable { mutableStateOf(false) }
val tiempos by tiemposViewModel.tiempos.collectAsState()
val tiempo = tiempos[Folio]
val context = LocalContext.current

LaunchedEffect(Unit) {
    tiemposViewModel.fetchTiempo(Folio)
}

LaunchedEffect(tiempo) {
    time = tiempo?.tiempo ?: 0
}

LaunchedEffect(isRunning) {
    while (isRunning) {
        delay(1000L)
        time++
    }
}

Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = formatTime(time),
        style = MaterialTheme.typography.headlineLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row {
        Button(onClick = {
            isRunning = !isRunning
            if (isRunning) {
                //context.startCronometroService(Folio, time)
            }
            else
            {
                val tiempo = Tiempo(tipoId = TipoId, folio = Folio, fecha = Fecha, status = Status, tiempo = time)
                tiemposViewModel.upsertTiempo(tiempo)
            }
        }) {
            Text(if (isRunning) "Pausar" else "Iniciar")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = { showDialog = true }, enabled = time != 0) {
            Text("Reiniciar")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = {
            isRunning = false
            val tiempo = Tiempo(tipoId = TipoId, folio = Folio, fecha = Fecha, status = Status, tiempo = time)
            tiemposViewModel.upsertTiempo(tiempo)
        }) {
            Text("Terminar")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmación") },
            text = { Text("¿Estás seguro que quieres reiniciar?") },
            confirmButton = {
                Button(onClick = {
                    isRunning = false
                    tiemposViewModel.deleteTiempo(Folio)
                    time = 0
                    showDialog = false
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
}*/
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
    papeletaViewModel: PapeletaViewModel
) {
    val context = LocalContext.current
    val etapaState by papeletaViewModel.etapasState.collectAsState()
    val desactivacionState by papeletaViewModel.desactivacionState.collectAsState()

    LaunchedEffect(Folio) {
        papeletaViewModel.cargarInfoEtapasPorFolio(Folio)
    }

    LaunchedEffect(Folio) {
        papeletaViewModel.cargarUltimaDetencionActiva(Folio)
    }

    LaunchedEffect(etapaState) {
        (etapaState as? PapeletaViewModel.EtapasState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            papeletaViewModel.resetEtapasState()
        }
    }

    LaunchedEffect(desactivacionState) {
        desactivacionState?.onSuccess { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetDesactivacionState()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetDesactivacionState()
        }
    }

    val todasLasEtapas = listOf("Madera", "Producción", "Pintura", "Tapiceria", "Empaque")

    val uiState by combine(
        //tiemposViewModel.getEtapasFinalizadas(Folio),
        //tiemposViewModel.getEtapaDisponible(Folio),
        papeletaViewModel.etapasFinalizadas,
        papeletaViewModel.etapasDisponibles,
        papeletaViewModel.ultimaDetencion
    ) { finalizadas, disponibles, detencion ->
        Triple(finalizadas, disponibles, detencion)
    }.collectAsState(initial = Triple(emptySet(), emptyList(), null))

    val etapasFinalizadas = uiState.first
    val etapaDisponible = uiState.second
    val detencion = uiState.third
    val hayDetencionActiva = detencion?.activa == true
    var showDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Etapas", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            IconButton(onClick = { onNavigate("producciones") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*when (etapaState){
            is PapeletaViewModel.EtapasState.Error -> {
                val message = (etapaState as PapeletaViewModel.EtapasState.Error).message
                LaunchedEffect(message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
            PapeletaViewModel.EtapasState.Loading -> {}
            is PapeletaViewModel.EtapasState.Success -> {}
        }*/
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(todasLasEtapas) { etapa ->
                val esFinalizada = etapa in etapasFinalizadas
                val esDisponible = etapa in etapaDisponible
                val estaDeshabilitada = hayDetencionActiva || (!esFinalizada && !esDisponible)

                val onClickAction = { if (esDisponible || esFinalizada) onEtapaSeleccionada(etapa) }

                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(2.dp, if (hayDetencionActiva) Color.Red else if (esFinalizada) Color.Green else if (esDisponible) MaterialTheme.colorScheme.primary else Color.Transparent),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            hayDetencionActiva -> Color(0xFFFCD7DB) // Rojo claro
                            esFinalizada -> MaterialTheme.colorScheme.secondaryContainer
                            esDisponible -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
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
                            color = when {
                                hayDetencionActiva -> MaterialTheme.colorScheme.onPrimaryContainer
                                esFinalizada || esDisponible -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
        }

        // Botón "Detalles de detención"
        if (hayDetencionActiva) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Detalles de detención", color = Color.White)
            }
        }
    }

    // Diálogo para mostrar detalles de la detención
    if (showDialog) {
        DetencionDialog(
            detencion = detencion!!,
            onClose = { showDialog = false },
            onDesactivar = {
                //tiemposViewModel.setDetencionActiva(detencion.id, false)
                Log.d("Boton desactivar","folio ${detencion.folio}, etapa: ${detencion.etapa}")
                papeletaViewModel.desactivarDetencionTiempo(detencion.folio, detencion.etapa)
                Log.d("Boton desactivar","id: ${detencion.id}, false")
            }
        )
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronometroScreen(
    TipoId: String,
    Folio: Int,
    Fecha: String,
    Status: String,
    Etapa: String,
    onNavigate: (String) -> Unit,
    tiemposViewModel: TiemposViewModel,
    papeletaViewModel: PapeletaViewModel
) {

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var stopDialog by rememberSaveable { mutableStateOf(false) }
    var reason by rememberSaveable { mutableStateOf("") }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    // 📌 Obtener tiempo almacenado en Room
    val tiempoDesdeDB by tiemposViewModel.getTiempoStream(Folio, Etapa).collectAsState(initial = null)
    val tiempoBase = tiempoDesdeDB?.tiempo ?: 0
    var time by rememberSaveable { mutableStateOf(tiempoDesdeDB?.tiempo ?: 0) }
    val isFinished = tiempoDesdeDB?.isFinished ?: false
    val tiempoId by tiemposViewModel.getTiempoId(Folio, Etapa).collectAsState(initial = null)
    val isRunningFlow = tiempoId?.let { tiemposViewModel.getIsRunning(it) } ?: flowOf(false)
    val isRunning by isRunningFlow.collectAsState(initial = false)

    // View Model
    LaunchedEffect (Folio, Etapa){
        papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
    }

    val tiempoDesdeDBA = papeletaViewModel.tiempoPorEtapa.collectAsState()

    // ✅ Usar `rememberCoroutineScope()` para evitar bloqueos
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRunning) {
        if (isRunning) {
            coroutineScope.launch {
                while (isRunning) {
                    delay(1000L)
                    if(!isRunning)
                    {
                        break
                    }
                    else
                    {
                    time++
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    var cronometroService by remember { mutableStateOf<CronometroService?>(null) }

    val serviceIntent = remember {
        Intent(context, CronometroService::class.java)
    }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val b = binder as? CronometroService.CronometroBinder
                cronometroService = b?.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cronometroService = null
            }
        }
    }

    LaunchedEffect(cronometroService) {
        cronometroService?.let { service ->
            val tiempoIdActual = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: return@let
            val tiempoDesdeServicio = service.getCronometroTiempo(tiempoIdActual, Etapa)

            if (tiempoDesdeServicio > (tiempoDesdeDB?.tiempo ?: 0)) {
                Log.d("CronometroScreen", "⏱ Sincronizando tiempo desde servicio: $tiempoDesdeServicio segundos")
                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, tiempoDesdeServicio.toInt())
                time = tiempoDesdeServicio.toInt()
            }
        }
    }


    DisposableEffect(Unit) {
        //ContextCompat.startForegroundService(context, serviceIntent)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(serviceConnection)
        }
    }


    LaunchedEffect(tiempoDesdeDB) {
        tiempoDesdeDB?.let { tiempoGuardado ->
            time = tiempoGuardado.tiempo // ✅ Mantiene el valor correcto al abrir la pantalla
        }
    }

    TopAppBar(
        title = { Text(
            Etapa,
            style = MaterialTheme.typography.titleMedium,
        ) },
        navigationIcon = {
            IconButton(onClick = { onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}") }) { // O la ruta deseada
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
        }
    )

   /* Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(Etapa)
    }*/

    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(time),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                coroutineScope.launch {
                    // INICIAR
                    val newState = !isRunning
                    if(newState) {
                        // Verificar si el proceso existe antes de hacer un `upsert`
                        val procesoExiste = tiemposViewModel.checkIfProcesoExists(Folio)
                        if (!procesoExiste) {
                            Log.d("CronometroScreen", "El proceso no existe, se crea uno nuevo")
                            tiemposViewModel.upsertProceso(
                                Proceso(
                                    folio = Folio,
                                    tipoId = TipoId,
                                    fecha = System.currentTimeMillis(),
                                    status = Status
                                )
                            )
                        }

                        // Verificar si el tiempo ya existe antes de hacer un `upsert`
                        val tiempoExiste = tiemposViewModel.checkIfTiempoExists(Folio, Etapa)
                        if (!tiempoExiste) {
                            Log.d("CronometroScreen", "Registro en tiempos no existe, se crea uno")
                            tiemposViewModel.upsertTiempo(
                                Tiempo(
                                    procesoFolio = Folio,
                                    etapa = Etapa,
                                    tiempo = 0,
                                    fechaInicio = System.currentTimeMillis(),
                                    fechaFin = 0, // ✅ Valor por defecto
                                    isRunning = newState,
                                    isFinished = false // ✅ Valor por defecto
                                )
                            )
                            // Hacer el registro en ACCESS iniciar tiempo
                            val fecha = formatearFechaActual()
                            papeletaViewModel.iniciarTiempo(Folio, Etapa, fecha)
                            Log.d("CronometroScreen", "iniciarTiempo -> Folio: $Folio, Etapa: $Etapa, Fecha: $fecha")
                            papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                            Log.d("CronometroScreen", "cargarTiempoPorEtapa -> Folio: $Folio, Etapa: $Etapa, Tiempo: $tiempoDesdeDBA")
                        } else {
                            Log.d("CronometroScreen", "Registro ya existe")
                            tiempoDesdeDBA.value?.fechaInicio?.let {
                                papeletaViewModel.iniciarTiempo(
                                    Folio,
                                    Etapa,
                                    it
                                )
                                Log.d("CronometroScreen", "iniciarTiempo -> Folio: $Folio, Etapa: $Etapa, Fecha: $it")
                            }
                        }
                    }
                    // PAUSA
                    if (!newState) {
                        val tiempoIdNuevo = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                        Log.d("CronometroScreen","Pausa, se actualiza el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time}")
                        tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                        // Actualizar tiemmpo en ACCESS
                        papeletaViewModel.pausarTiempo(PausarTiempoRequest(Folio, Etapa, time))
                        cronometroService?.resetCronometro(tiempoIdNuevo, Etapa)
                        if (cronometroService?.getActiveEtapas()?.isEmpty() == true) {
                            val intent = Intent(contexto, CronometroService::class.java)
                            contexto.stopService(intent)
                        }
                    }else {
                        delay(100)
                        val tiempoIdNuevo = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                        val intent = Intent(contexto, CronometroService::class.java).apply {
                            putExtra("tipoId", TipoId)
                            putExtra("folio", Folio)
                            putExtra("fecha", Fecha)
                            putExtra("status", Status)
                            putExtra("etapa", Etapa)
                            putExtra("tiempoInicial", tiempoBase.toLong())
                            putExtra("id", tiempoIdNuevo ?: -1)
                        }
                        ContextCompat.startForegroundService(contexto, intent)
                        Log.d(
                            "CronometroScreen",
                            "⏱ Servicio iniciado para $Folio etapa $Etapa desde $tiempoBase segundos"
                        )
                    }
                    // ✅ Cambiar el estado `isRunning`
                    tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, newState)
                }
            }, enabled = !isFinished) {
                Text(if (isRunning) "Pausar" else "Iniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Mostrar dialog Reiniciar
            Button(onClick = { showDialog = true }, enabled = time != 0 && !isFinished) {
                Text("Reiniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Boton para finalizar la captura de tiempo de una etapa
            Button(
                onClick = {
                    cronometroService?.let { service ->
                        scope.launch {
                            // Lógica para terminar captura de tiempo de esa etapa
                            Log.d("CronometroScreen", "Boton finalizar -> ${Folio}, ${Etapa}, ${time}")
                            tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                            tiemposViewModel.finalizarTiempoByFolioEtapa(Folio, Etapa)
                            tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)

                            val tiempoIdNuevo = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                            service.resetCronometro(tiempoIdNuevo, Etapa)

                            if (service.getActiveEtapas().isEmpty()) {
                                val intent = Intent(contexto, CronometroService::class.java)
                                contexto.stopService(intent)
                            }
                            // ACCESS
                            papeletaViewModel.finalizarTiempo(Folio, Etapa, formatearFechaActual(), time)
                            papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                        }
                    } ?: run {
                        Log.d("CronometroScreen", "Boton finalizar -> ${Folio}, ${Etapa}, ${time}")
                        tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                        tiemposViewModel.finalizarTiempoByFolioEtapa(Folio, Etapa)
                        tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                        // ACCESS
                        papeletaViewModel.finalizarTiempo(Folio, Etapa, formatearFechaActual(), time)
                        papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                    }

                }, enabled = !isFinished
            ) {
                // Logica del if
                Text("Finalizar")
            }
        }
        // 🔔 Confirmación para reiniciar
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirmación") },
                text = { Text("¿Estás seguro que quieres reiniciar?") },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                            val tiempoIdNuevo = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                            tiemposViewModel.updateTiempoByFolio(Folio, Etapa,0)

                            // Reiniciar en Access
                            papeletaViewModel.reiniciarTiempo(Folio, Etapa)
                            papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)

                            cronometroService?.let { service ->
                                service.resetCronometro(tiempoIdNuevo, Etapa)

                                if (service.getActiveEtapas().isEmpty()) {
                                    val intent = Intent(contexto, CronometroService::class.java)
                                    contexto.stopService(intent)
                                }
                            }

                            time = 0
                            showDialog = false
                        }}) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = {reason = ""
                    stopDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.padding(8.dp), enabled = !isFinished && time != 0 // Espaciado opcional
            ) {
                Text("Detener")
            }
            if (stopDialog) {
                AlertDialog(
                    onDismissRequest = { stopDialog = false },
                    title = {
                        Text(
                            text = "¿Deseas detener este proceso?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Esta acción detendrá el proceso en curso. Por favor, proporciona un motivo.",
                                    fontSize = 14.sp
                                )
                            }

                            Divider()

                            Text("Fecha: ${formatearFechaActual()}", style = MaterialTheme.typography.bodySmall)
                            Text("Folio: $Folio", style = MaterialTheme.typography.bodySmall)

                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("Motivo") },
                                placeholder = { Text("Ingresa el motivo de detención") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = false,
                                maxLines = 3
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Lógica para detener el proceso
                                    val tiempoIdNuevo = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                                    /*
                                    Log.d(
                                        "CronometroScreen",
                                        "Pausa, se actualiza el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time} debido a una detención"
                                    )
                                    tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                    cronometroService?.resetCronometro(tiempoIdNuevo, Etapa)
                                    if (cronometroService?.getActiveEtapas()?.isEmpty() == true) {
                                        val intent = Intent(contexto, CronometroService::class.java)
                                        contexto.stopService(intent)
                                    }
                                    tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)*/
                                    if(Etapa == "Madera" || Etapa == "Producción") {
                                        Log.d(
                                            "CronometroScreen",
                                            "Pausa, se actualiza(n) el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time} debido a una detención"
                                        )
                                        cronometroService?.stopCronometrosPorFolio(Folio)
                                    }
                                    else
                                    {
                                        Log.d(
                                            "CronometroScreen",
                                            "Pausa, se actualiza el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time} debido a una detención"
                                        )
                                        tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                        cronometroService?.resetCronometro(tiempoIdNuevo, Etapa)
                                        if (cronometroService?.getActiveEtapas()?.isEmpty() == true) {
                                            val intent = Intent(contexto, CronometroService::class.java)
                                            contexto.stopService(intent)
                                        }
                                        tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                                    }
                                    // Crear objeto Detencion
                                    val detencion = Detencion(
                                        tiempoId = tiempoIdNuevo, // Asegúrate de tener este valor en tu scope
                                        folioPapeleta = Folio,
                                        etapa = Etapa, // También necesitas saber en qué etapa estás
                                        motivo = reason,
                                        fecha = System.currentTimeMillis(),
                                        activa = true
                                    )
                                    // Guardar en base de datos
                                    tiemposViewModel.upsertDetencion(detencion)
                                    // ACCESS
                                    papeletaViewModel.reportarDetencionTiempo(time, Etapa, Folio, formatearFechaActual(), reason)
                                    stopDialog = false
                                    onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                                }
                            },
                            enabled = reason.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Detener", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { stopDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )


            }
        }
    }
}
/*
@Composable
fun CronometroScreen(
    TipoId: String,
    Folio: Int,
    Fecha: String,
    Status: String,
    Etapa: String,
    tiemposViewModel: TiemposViewModel
) {
    val contexto = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var stopDialog by rememberSaveable { mutableStateOf(false) }
    var reason by rememberSaveable { mutableStateOf("") }

    // Obtener tiempo almacenado en Room
    val tiempoDesdeDB by tiemposViewModel.getTiempoStream(Folio, Etapa).collectAsState(initial = null)
    val tiempoBase = tiempoDesdeDB?.tiempo ?: 0

    var acumulado by rememberSaveable { mutableStateOf(tiempoBase) }
    var startTimestamp by rememberSaveable { mutableStateOf<Long?>(null) }

    val tiempoId by tiemposViewModel.getTiempoId(Folio, Etapa).collectAsState(initial = null)
    val isRunningFlow = tiempoId?.let { tiemposViewModel.getIsRunning(it) } ?: flowOf(false)
    val isRunning by isRunningFlow.collectAsState(initial = false)

    // Actualizar acumulado cuando cambie DB
    LaunchedEffect(tiempoDesdeDB) {
        tiempoDesdeDB?.let { acumulado = it.tiempo }
    }

    // Tiempo visible en pantalla
    val elapsedTime by produceState(initialValue = acumulado, key1 = isRunning, key2 = startTimestamp) {
        while (true) {
            value = if (isRunning && startTimestamp != null) {
                acumulado + ((System.currentTimeMillis() - startTimestamp!!) / 1000).toInt()
            } else {
                acumulado
            }
            delay(1000L)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(Etapa)

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(elapsedTime),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // INICIAR / PAUSAR
                Button(onClick = {
                    coroutineScope.launch {
                        val newState = !isRunning

                        val procesoExiste = tiemposViewModel.checkIfProcesoExists(Folio)
                        if (!procesoExiste) {
                            tiemposViewModel.upsertProceso(
                                Proceso(folio = Folio, tipoId = TipoId, fecha = System.currentTimeMillis(), status = Status)
                            )
                        }

                        val tiempoExiste = tiemposViewModel.checkIfTiempoExists(Folio, Etapa)
                        if (!tiempoExiste) {
                            tiemposViewModel.upsertTiempo(
                                Tiempo(
                                    procesoFolio = Folio,
                                    etapa = Etapa,
                                    tiempo = 0,
                                    fechaInicio = System.currentTimeMillis(),
                                    fechaFin = 0,
                                    isRunning = newState,
                                    isFinished = false
                                )
                            )
                        }

                        if (newState) {
                            startTimestamp = System.currentTimeMillis()
                        } else {
                            val nuevoTiempo = elapsedTime
                            tiemposViewModel.updateTiempoByFolio(Folio, Etapa, nuevoTiempo)
                            acumulado = nuevoTiempo
                            startTimestamp = null
                        }

                        tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, newState)
                    }
                }) {
                    Text(if (isRunning) "Pausar" else "Iniciar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // REINICIAR
                Button(onClick = { showDialog = true }, enabled = elapsedTime != 0) {
                    Text("Reiniciar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // FINALIZAR
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val nuevoTiempo = if (isRunning && startTimestamp != null) {
                                acumulado + ((System.currentTimeMillis() - startTimestamp!!) / 1000).toInt()
                            } else {
                                acumulado
                            }

                            tiemposViewModel.updateTiempoByFolio(Folio, Etapa, nuevoTiempo)
                            tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                            tiemposViewModel.finalizarTiempoByFolioEtapa(Folio, Etapa)

                            acumulado = nuevoTiempo
                            startTimestamp = null
                        }
                    }
                ) {
                    Text("Finalizar")
                }
            }

            // 🔔 DIALOGO REINICIAR
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmación") },
                    text = { Text("¿Estás seguro que quieres reiniciar?") },
                    confirmButton = {
                        Button(onClick = {
                            tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                            tiemposViewModel.deleteTiempoByFolioEtapa(Folio, Etapa)
                            acumulado = 0
                            startTimestamp = null
                            showDialog = false
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            // 🔴 BOTÓN DETENER
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End) {
                Button(
                    onClick = { stopDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Detener")
                }

                if (stopDialog) {
                    reason = ""
                    AlertDialog(
                        onDismissRequest = { stopDialog = false },
                        title = { Text("¿Deseas detener este proceso?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Esta acción detendrá el proceso en curso. Por favor, proporciona un motivo.", fontSize = 14.sp)
                                }
                                Divider()
                                Text("Fecha: $currentDate", style = MaterialTheme.typography.bodySmall)
                                Text("Folio: $Folio", style = MaterialTheme.typography.bodySmall)

                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { reason = it },
                                    label = { Text("Motivo") },
                                    placeholder = { Text("Ingresa el motivo de detención") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    stopDialog = false
                                    // guardar motivo si lo deseas
                                },
                                enabled = reason.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Detener", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { stopDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
        }
    }
}
*/

fun formatearFechaActual(): String {
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formato.format(Date(System.currentTimeMillis()))
}

fun formatearFecha(fecha: Long): String {
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formato.format(Date(fecha))
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
val hours = seconds / 3600
val minutes = (seconds % 3600) / 60
val remainingSeconds = seconds % 60
return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

/*
fun Context.startCronometroService(folio: Int, tiempoInicial: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si no tiene el permiso, solicítalo
            (this as? MainActivity)?.requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
            return // No inicies el servicio hasta que el usuario acepte el permiso
        }
    }

    // Si ya tiene permisos, inicia el servicio
    val intent = Intent(this, CronometroService::class.java).apply {
        putExtra("folio", folio)
        putExtra("tiempoInicial", tiempoInicial)
    }
    startForegroundService(intent)
}
*/