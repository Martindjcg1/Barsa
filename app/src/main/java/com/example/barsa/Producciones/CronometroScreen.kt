package com.example.barsa.Producciones

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
    var isRunning by rememberSaveable { mutableStateOf(false) }
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

                    if (tiempoActual > 0) {
                        // Solo sincroniza si el servicio está corriendo
                        tiemposViewModel.upsertTiempo(
                            Tiempo(tipoId = TipoId, folio = Folio, fecha = Fecha, status = Status, tiempo = tiempoActual.toInt())
                        )
                        time = tiempoActual.toInt()
                        isRunning = true // Asegurar que el cronómetro sigue corriendo
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
        while (isRunning) {
            delay(1000L)
            time++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.background(Color.White)
            .padding(16.dp),
       // verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // 🏭 Mostrar el proceso actual
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
        // ⏳ Mostrar el tiempo
        Text(
            text = formatTime(time),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            // ▶⏸ Botón Iniciar/Pausar
            Button(onClick = {
                isRunning = !isRunning
                if (isRunning) {
                    val intent = Intent(contexto, CronometroService::class.java).apply {
                        putExtra("folio", Folio)
                        putExtra("tiempoInicial", time)
                    }
                    contexto.startService(intent) // Inicia el servicio
                } else {
                    cronometroService?.let { service ->
                        val tiempoFinal = service.stopCronometro(Folio)
                        tiemposViewModel.upsertTiempo(
                            Tiempo(tipoId = TipoId, folio = Folio, fecha = Fecha, status = Status, tiempo = tiempoFinal.toInt())
                        )
                        val intent = Intent(contexto, CronometroService::class.java)
                        contexto.stopService(intent) // Detén el servicio
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
                        isRunning = false

                        // Eliminar tiempo de Room
                        tiemposViewModel.deleteTiempo(Folio)

                        // Resetear variables locales
                        time = 0
                        // currentProcess = 1 // (Si usas esto, descoméntalo)

                        // Detener el servicio si está en ejecución
                        cronometroService?.let { service ->
                            service.resetCronometro(Folio) // Reiniciar cronómetro en el servicio
                            val intent = Intent(contexto, CronometroService::class.java)
                            contexto.stopService(intent) // Detiene el Foreground Service
                        }

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
                AlertDialog(
                    onDismissRequest = { stopDialog = false },
                    title = { Text("Detener Proceso") },
                    text = {
                        Column {
                            Text("Fecha: $currentDate")
                            Text("Folio: $Folio")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            stopDialog = false
                            isRunning = false
                            // Agrega tu lógica para manejar el proceso detenido
                        }) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { stopDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
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




@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
val hours = seconds / 3600
val minutes = (seconds % 3600) / 60
val remainingSeconds = seconds % 60
return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}