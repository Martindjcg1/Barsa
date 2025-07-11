package com.example.barsa.Producciones

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.barsa.Network.NetworkMonitor
import com.example.barsa.Network.NetworkStatus
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import com.example.barsa.data.room.TiemposViewModel
import com.example.barsa.data.room.local.Detencion
import com.example.barsa.data.room.local.Tiempo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    papeletaViewModel: PapeletaViewModel,
    networkMonitor: NetworkMonitor
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
    var time by rememberSaveable { mutableIntStateOf(tiempoDesdeDB?.tiempo ?: 0) }
    val isFinished = tiempoDesdeDB?.isFinished ?: false
    val tiempoId by tiemposViewModel.getTiempoId(Folio, Etapa).collectAsState(initial = null)
    val isRunningFlow = tiempoId?.let { tiemposViewModel.getIsRunning(it) } ?: flowOf(false)
    val isRunning by isRunningFlow.collectAsState(initial = false)
    val etapaState by papeletaViewModel.tiempoEtapasState.collectAsState()
    val networkStatus by networkMonitor.networkStatus.observeAsState(NetworkStatus.NoConnection)
    var showNoInternetDialog by rememberSaveable { mutableStateOf(false) }
    var isManualReset by remember { mutableStateOf(false) }

    LaunchedEffect (Folio, Etapa){
        papeletaViewModel.resetTiempoEtapaState()
        papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
    }

    val tiempoDesdeDBA = papeletaViewModel.tiempoPorEtapa.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val isRunningState by rememberUpdatedState(newValue = isRunning)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (isRunningState) {
                time++
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

    DisposableEffect(Unit) {
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    LaunchedEffect(cronometroService, isRunning) {
        if (!isManualReset) {
            val tiempoId = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
            val servicioActivo = cronometroService?.isRunning(tiempoId, Etapa) ?: false
            val tiempoRoom = tiempoDesdeDB?.tiempo ?: 0

            if (!servicioActivo && isRunning) {  // Usamos el valor actualizado
                val fechaActual = formatearFechaActual()
                Log.d("CronometroScreen", "⚠️ Cronómetro inconsistente detectado: $Folio - $Etapa")

                tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                papeletaViewModel.pausarTiempo(
                    PausarTiempoRequest(
                        folio = Folio,
                        etapa = Etapa,
                        tiempo = tiempoRoom,
                        fechaPausa = fechaActual
                    )
                )
            }
        }
    }

    LaunchedEffect(isFinished, isRunning, tiempoDesdeDBA.value) {
    //LaunchedEffect(tiempoDesdeDBA.value) {
        if (!isManualReset) {
            delay(200)
            val tiempoAccess = tiempoDesdeDBA.value?.tiempo ?: 0
            val tiempoRoom = tiempoDesdeDB?.tiempo ?: 0
            if ((isFinished || !isRunning) && time < tiempoAccess) {
                Log.d("CronometroScreen", "⏹ Terminado o detenido -> ACCESS: actualizando tiempo a $tiempoAccess")
                time = tiempoAccess
                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
            }
            else if( (isFinished || !isRunning) && (tiempoAccess == 0) && (tiempoRoom != tiempoAccess)) {
                Log.d("CronometroScreen", "🔄 Reinicio remoto detectado -> ACCESS menor: actualizando Room y UI a $tiempoAccess")
                time = tiempoAccess
                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, tiempoAccess)
            }
        }
    }


    LaunchedEffect(cronometroService, tiempoDesdeDB) {
        if (!isManualReset && cronometroService != null) {
            val tiempoIdActual = tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull()
            val tiempoDesdeServicio = tiempoIdActual?.let { id ->
                cronometroService?.getCronometroTiempo(id, Etapa)
            }

            val tiempoRoom = tiempoDesdeDB?.tiempo ?: 0

            val tiempoFinal = when {
                tiempoDesdeServicio != null && tiempoDesdeServicio > tiempoRoom -> {
                    Log.d("CronometroScreen", "⏱ Tiempo desde servicio: $tiempoDesdeServicio segundos")
                    tiemposViewModel.updateTiempoByFolio(Folio, Etapa, tiempoDesdeServicio.toInt())
                    tiempoDesdeServicio.toInt()
                }
                else -> tiempoRoom
            }

            time = tiempoFinal
        }
    }

    LaunchedEffect(etapaState) {
        (etapaState as? PapeletaViewModel.TiempoEtapaState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }

    val mensajeFinalizarTiempo by papeletaViewModel.finalizarTiempoResult.collectAsState()

    LaunchedEffect(mensajeFinalizarTiempo) {
        mensajeFinalizarTiempo?.onSuccess {
            onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
            papeletaViewModel.resetFinalizarTiempoResult()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetFinalizarTiempoResult()
        }
    }

    val reportarDetencion by papeletaViewModel.detencionTiempoResult.collectAsState()

    LaunchedEffect(reportarDetencion) {
        reportarDetencion?.onSuccess {
            onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
            papeletaViewModel.resetdetencionTiempoResult()
        }?.onFailure { error ->
            Toast.makeText(context, error.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
            papeletaViewModel.resetdetencionTiempoResult()
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

    Spacer(modifier = Modifier.height(16.dp))
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

    if (etapaState is PapeletaViewModel.TiempoEtapaState.Error) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick =
            {
                papeletaViewModel.resetTiempoEtapaState()
                papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
            })
            {
                Text("Reintentar")
            }
        }
        return
    }

    if (etapaState is PapeletaViewModel.TiempoEtapaState.Success) {
        val puedeEditar = tiempoDesdeDBA.value?.isFinished == true
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
                    if (networkStatus is NetworkStatus.ConnectedInternet) {
                        coroutineScope.launch {
                            // INICIAR
                            val newState = !isRunning
                            if (newState) {

                                // Verificar si el tiempo ya existe antes de hacer un `upsert`
                                val tiempoExiste = tiemposViewModel.checkIfTiempoExists(Folio, Etapa)
                                if (!tiempoExiste) {
                                    val tiempoAccess = tiempoDesdeDBA.value?.tiempo ?: 0
                                    val tiempoAInsertar = if (tiempoAccess > 0) tiempoAccess else 0
                                    Log.d(
                                        "CronometroScreen",
                                        "Registro en tiempos no existe, se crea uno"
                                    )
                                    tiemposViewModel.upsertTiempo(
                                        Tiempo(
                                            procesoFolio = Folio,
                                            etapa = Etapa,
                                            tiempo = tiempoAInsertar,
                                            fechaInicio = System.currentTimeMillis(),
                                            fechaFin = 0, // ✅ Valor por defecto
                                            isRunning = false,
                                            isFinished = false // ✅ Valor por defecto
                                        )
                                    )
                                    // Hacer el registro en ACCESS iniciar tiempo
                                    val fecha = formatearFechaActual()
                                    papeletaViewModel.iniciarTiempo(Folio, Etapa, fecha)
                                    delay(100)
                                    Log.d(
                                        "CronometroScreen",
                                        "iniciarTiempo -> Folio: $Folio, Etapa: $Etapa, Fecha: $fecha"
                                    )
                                    papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                                    Log.d(
                                        "CronometroScreen",
                                        "cargarTiempoPorEtapa -> Folio: $Folio, Etapa: $Etapa, Tiempo: $tiempoDesdeDBA"
                                    )
                                } else {
                                    Log.d("CronometroScreen", "Registro ya existe")
                                    tiempoDesdeDBA.value?.fechaInicio?.let {
                                        papeletaViewModel.iniciarTiempo(
                                            Folio,
                                            Etapa,
                                            it
                                        )
                                        Log.d(
                                            "CronometroScreen",
                                            "iniciarTiempo -> Folio: $Folio, Etapa: $Etapa, Fecha: $it"
                                        )
                                    }
                                }
                            }
                            // PAUSA
                            if (!newState) {
                                val tiempoIdNuevo =
                                    tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                                Log.d(
                                    "CronometroScreen",
                                    "Pausa, se actualiza el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time}"
                                )
                                val fecha = formatearFechaActual()
                                papeletaViewModel.pausarTiempo(PausarTiempoRequest(Folio, Etapa, time, fecha))
                                delay(200)
                                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                // Actualizar tiemmpo en ACCESS

                                delay(100)
                                cronometroService?.resetCronometro(tiempoIdNuevo, Etapa)
                                cronometroService?.removeFromSharedPreferences(tiempoIdNuevo, Etapa)
                                if (cronometroService?.getActiveEtapas()?.isEmpty() == true) {
                                    val intent = Intent(contexto, CronometroService::class.java)
                                    contexto.stopService(intent)
                                }
                                papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                            } else {
                                delay(100)
                                val tiempoIdNuevo =
                                    tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                                val intent = Intent(contexto, CronometroService::class.java).apply {
                                    putExtra("tipoId", TipoId)
                                    putExtra("folio", Folio)
                                    putExtra("fecha", Fecha)
                                    putExtra("status", Status)
                                    putExtra("etapa", Etapa)
                                    putExtra("tiempoInicial", time.toLong())
                                    putExtra("id", tiempoIdNuevo ?: -1)
                                    putExtra("fechaPausa", formatearFechaActual())
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
                    }
                    else
                    {
                        showNoInternetDialog = true
                    }
                    //}, enabled = !isFinished || !tiempoDesdeDBA.value?.isFinished!!) {
                }, enabled = !puedeEditar) {
                    Text(if (isRunning) "Pausar" else "Iniciar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mostrar dialog Reiniciar
                Button(onClick = { showDialog = true }, enabled = time != 0 && (!puedeEditar)) {
                    Text("Reiniciar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Boton para finalizar la captura de tiempo de una etapa
                Button(
                    onClick = {
                        if (networkStatus is NetworkStatus.ConnectedInternet) {
                            cronometroService?.let { service ->
                                scope.launch {
                                    // Lógica para terminar captura de tiempo de esa etapa
                                    Log.d(
                                        "CronometroScreen",
                                        "Boton finalizar -> ${Folio}, ${Etapa}, ${time}"
                                    )
                                    tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                    tiemposViewModel.finalizarTiempoByFolioEtapa(Folio, Etapa)
                                    tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)

                                    val tiempoIdNuevo =
                                        tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                                    service.resetCronometro(tiempoIdNuevo, Etapa)

                                    if (service.getActiveEtapas().isEmpty()) {
                                        val intent = Intent(contexto, CronometroService::class.java)
                                        contexto.stopService(intent)
                                    }
                                    // ACCESS
                                    papeletaViewModel.finalizarTiempo(
                                        Folio,
                                        Etapa,
                                        formatearFechaActual(),
                                        time
                                    )
                                    //papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                                    //onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                                }
                            } ?: run {
                                Log.d(
                                    "CronometroScreen",
                                    "Boton finalizar -> ${Folio}, ${Etapa}, ${time}"
                                )
                                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                tiemposViewModel.finalizarTiempoByFolioEtapa(Folio, Etapa)
                                tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                                // ACCESS
                                papeletaViewModel.finalizarTiempo(
                                    Folio,
                                    Etapa,
                                    formatearFechaActual(),
                                    time
                                )
                                //papeletaViewModel.cargarTiempoPorEtapa(Folio, Etapa)
                                //onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                            }
                        }
                        else
                        {
                            showNoInternetDialog = true
                        }
                    }, enabled = time == 0 || !isRunning
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
                            if (networkStatus is NetworkStatus.ConnectedInternet) {
                                scope.launch {
                                    isManualReset = true
                                    time = 0
                                    tiemposViewModel.updateIsRunningByFolio(Folio, Etapa, false)
                                    val tiempoIdNuevo =
                                        tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull() ?: -1
                                    Log.d("CronometroScreen", "Reiniciando $Folio, $Etapa, $time")
                                    tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                    delay(100)

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
                                    showDialog = false
                                    isManualReset = false
                                }
                            }
                            else
                            {
                                showNoInternetDialog = true
                            }

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
                    modifier = Modifier.padding(8.dp), enabled = !puedeEditar && time != 0 // Espaciado opcional
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
                                    if (networkStatus is NetworkStatus.ConnectedInternet) {
                                        coroutineScope.launch {
                                            // Lógica para detener el proceso
                                            val tiempoIdNuevo =
                                                tiemposViewModel.getTiempoId(Folio, Etapa).firstOrNull()
                                                    ?: -1
                                            if (Etapa == "Madera" || Etapa == "Producción") {
                                                Log.d(
                                                    "CronometroScreen",
                                                    "Pausa, se actualiza(n) el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time} debido a una detención"
                                                )
                                                cronometroService?.stopCronometrosPorFolio(
                                                    Folio,
                                                    formatearFechaActual()
                                                )
                                            } else {
                                                Log.d(
                                                    "CronometroScreen",
                                                    "Pausa, se actualiza el tiempo para folio: ${Folio} en la etapa ${Etapa} con un tiempo de ${time} debido a una detención"
                                                )
                                                tiemposViewModel.updateTiempoByFolio(Folio, Etapa, time)
                                                cronometroService?.resetCronometro(tiempoIdNuevo, Etapa)
                                                if (cronometroService?.getActiveEtapas()
                                                        ?.isEmpty() == true
                                                ) {
                                                    val intent =
                                                        Intent(contexto, CronometroService::class.java)
                                                    contexto.stopService(intent)
                                                }
                                                tiemposViewModel.updateIsRunningByFolio(
                                                    Folio,
                                                    Etapa,
                                                    false
                                                )
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
                                            papeletaViewModel.reportarDetencionTiempo(
                                                time,
                                                Etapa,
                                                Folio,
                                                formatearFechaActual(),
                                                reason
                                            )
                                            stopDialog = false
                                            //onNavigate("selector/${TipoId}°${Folio}°${Fecha}°${Status}")
                                        }
                                    }
                                    else{
                                        showNoInternetDialog = true
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
                if (showNoInternetDialog) {
                    AlertDialog(
                        onDismissRequest = { showNoInternetDialog = false },
                        title = { Text("Sin conexión a Internet") },
                        text = { Text("Para realizar esta acción, debes estar conectado a Internet.") },
                        confirmButton = {
                            Button(onClick = { showNoInternetDialog = false }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }

            }
        }
    }
}

fun formatearFechaActual(): String {
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formato.format(Date(System.currentTimeMillis()))
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
val hours = seconds / 3600
val minutes = (seconds % 3600) / 60
val remainingSeconds = seconds % 60
return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}
