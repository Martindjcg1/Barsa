package com.example.barsa.Producciones

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.barsa.CronometroService
import com.example.barsa.MainActivity
import com.example.barsa.R
import com.example.barsa.data.TiemposViewModel
import com.example.barsa.data.local.Tiempo
import kotlinx.coroutines.delay
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


@Composable
fun CronometroScreen(TipoId: String, Folio: Int, Fecha: String, Status: String, tiemposViewModel: TiemposViewModel) {
    var time by rememberSaveable { mutableStateOf(0) }
    //var isRunning by rememberSaveable { mutableStateOf(false) }
    //val isRunningMap by tiemposViewModel.isRunningMap.collectAsState()
    //val isRunning = isRunningMap[Folio] ?: false // Obtener estado específico del folio
    var isRunning by rememberSaveable { mutableStateOf(false) }

    // Usamos la función getIsRunning para obtener el valor de isRunning
    LaunchedEffect(Folio) {
        tiemposViewModel.getIsRunning(Folio) { running ->
            isRunning = running
        }
    }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var currentProcess by rememberSaveable { mutableStateOf(1) }
    val maxProcesses = 4
    var stopDialog by rememberSaveable { mutableStateOf(false) }
    var reason by rememberSaveable { mutableStateOf("") }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val processNames = listOf("Madera", "Pintura", "Tapicería", "Empaque")
    val tiempos by tiemposViewModel.tiempos.collectAsState()
    val tiempo = tiempos[Folio]
    val contexto = LocalContext.current

    // Cambiar a "remember" para almacenar la referencia del servicio
    var cronometroService: CronometroService? by remember { mutableStateOf(null) }
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                cronometroService = (binder as CronometroService.CronometroBinder).getService()
                cronometroService?.let { service ->
                    val tiempoActual = service.getCronometroTiempo(Folio)

                    if (cronometroService?.isRunning(Folio) == true && tiempoActual > 0) {
                        // Solo sincroniza si el servicio está corriendo
                        tiemposViewModel.checkIfFolioExists(Folio) { exists ->
                            //val isRunning = tiemposViewModel.isRunningMap.value[Folio] ?: false
                            if (exists) {
                                // Si existe, actualizamos el tiempo
                                tiemposViewModel.updateTiempo(Folio, tiempoActual.toInt())
                            } else {
                                // Si no existe, hacemos un upsert para crear un nuevo registro
                                tiemposViewModel.upsertTiempo(
                                    Tiempo(
                                        tipoId = TipoId,
                                        folio = Folio,
                                        fecha = Fecha,
                                        status = Status,
                                        tiempo = tiempoActual.toInt(),
                                        isRunning = isRunning
                                    )
                                )
                            }
                            time = tiempoActual.toInt()

                            //isRunning = true // Asegurar que el cronómetro sigue corriendo
                            if (service.isRunning(Folio)) {
                                tiemposViewModel.updateIsRunning(Folio, true)
                            } else {
                                tiemposViewModel.updateIsRunning(Folio, false)
                            }
                        }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                cronometroService = null
            }
        }
    }

    // Bind del servicio
    LaunchedEffect(Unit) {
        val intent = Intent(contexto, CronometroService::class.java)
        contexto.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // Unbind del servicio
    DisposableEffect(Unit) {
        onDispose {
            contexto.unbindService(serviceConnection)
        }
    }

    // Cargar el tiempo almacenado en Room
    LaunchedEffect(Unit) {
        tiemposViewModel.fetchTiempo(Folio)
    }

    // Si el servicio no está corriendo, usar el tiempo guardado en Room
    LaunchedEffect(tiempo) {
        if (cronometroService == null || cronometroService?.getCronometroTiempo(Folio)?.toInt() == 0) {
            time = tiempo?.tiempo ?: 0
        }
    }

    LaunchedEffect(isRunning) {
        if(isRunning){
            while (isRunning) {
                delay(1000L)
                time++
            }
        }

    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = processNames[currentProcess - 1],
            style = MaterialTheme.typography.headlineMedium
        )
    }

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
            val tiempoDesdeDB by tiemposViewModel.getTiempoStream(Folio).collectAsState(initial = null)

            // ▶⏸ Botón Iniciar/Pausar
            Button(onClick = {
                //isRunning = !isRunning
                val newState = !isRunning
                isRunning = newState
                tiemposViewModel.updateIsRunning(Folio, newState)

                if (newState) {
                    tiemposViewModel.fetchTiempo(Folio)
                    val tiempoBase = tiempoDesdeDB?.tiempo ?: 0
                    val intent = Intent(contexto, CronometroService::class.java).apply {
                        putExtra("tipoId", TipoId)
                        putExtra("folio", Folio)
                        putExtra("fecha", Fecha)
                        putExtra("status", Status)
                        putExtra("tiempoInicial", tiempoBase.toLong())
                    }
                    contexto.startService(intent) // Inicia el servicio
                    Log.d("CronometroScreen", "Iniciando folio $Folio desde $tiempoBase segundos")
                    tiemposViewModel.checkIfFolioExists(Folio) { exists ->
                        if (!exists) {
                            // Si no existe, hacemos un upsert para crear un nuevo registro
                            tiemposViewModel.upsertTiempo(
                                Tiempo(
                                    tipoId = TipoId,
                                    folio = Folio,
                                    fecha = Fecha,
                                    status = Status,
                                    tiempo = 0,
                                    isRunning = isRunning
                                )
                            )
                        }
                    }
                } else {
                    cronometroService?.let { service ->
                            val tiempoFinal = service.stopCronometro(Folio)
                            tiemposViewModel.checkIfFolioExists(Folio) { exists ->
                                //val isRunning = tiemposViewModel.isRunningMap.value[Folio] ?: false
                                if (exists) {
                                    // Si existe, actualizamos el tiempo
                                    tiemposViewModel.updateTiempo(Folio, tiempoFinal.toInt())
                                } else {
                                    // Si no existe, hacemos un upsert para crear un nuevo registro
                                    tiemposViewModel.upsertTiempo(
                                        Tiempo(
                                            tipoId = TipoId,
                                            folio = Folio,
                                            fecha = Fecha,
                                            status = Status,
                                            tiempo = tiempoFinal.toInt(),
                                            isRunning = isRunning
                                        )
                                    )
                                }
                            }
                            //tiemposViewModel.upsertTiempo(Tiempo(tipoId = TipoId, folio = Folio, fecha = Fecha, status = Status, tiempo = tiempoFinal.toInt()))
                    }
                }
            }) {
                Text(if (isRunning) "Pausar" else "Iniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 🔄 Botón Reiniciar
            Button(onClick = { showDialog = true }, enabled = time != 0) {
                Text("Reiniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ➡ Botón Avanzar proceso
            Button(
                onClick = {/*
                    if (currentProcess < maxProcesses) {
                        isRunning = false
                        val nuevoTiempo = Tiempo(
                            tipoId = TipoId,
                            folio = Folio,
                            fecha = Fecha,
                            status = Status,
                            tiempo = time,
                            currentProcess = currentProcess + 1
                        )
                        tiemposViewModel.upsertTiempo(nuevoTiempo)
                        time = 0
                        currentProcess++
                    }*/
                    //time = 0
                    //if (currentProcess < maxProcesses)
                      //  currentProcess += 1
                },
                //enabled = currentProcess <= maxProcesses
            ) {
                Text(if (currentProcess == 4) "Terminar" else "Siguiente")
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
                        //isRunning = false
                        tiemposViewModel.updateIsRunning(Folio, false)

                        // Eliminar tiempo de Room
                        tiemposViewModel.deleteTiempo(Folio)

                        // Detener el cronómetro de este folio en el servicio
                        cronometroService?.let { service ->
                            service.resetCronometro(Folio)

                            // Solo si no quedan otros cronómetros activos, detenemos el servicio
                            if (service.getActiveFolios().isEmpty()) {
                                val intent = Intent(contexto, CronometroService::class.java)
                                contexto.stopService(intent)
                            }
                        }

                        // Resetear variables locales
                        time = 0
                        isRunning = false
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
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = { stopDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.padding(8.dp) // Espaciado opcional
            ) {
                Text("Detener")
            }

            if (stopDialog) {
                reason = ""
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
                                // Lógica para detener el proceso
                                // tiemposViewModel.setIsRunning(Folio, false)
                                // Guardar motivo si es necesario
                                stopDialog = false
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