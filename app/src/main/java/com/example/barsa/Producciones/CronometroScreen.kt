package com.example.barsa.Producciones

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.barsa.data.TiemposViewModel
import com.example.barsa.data.local.Tiempo
import kotlinx.coroutines.delay


@Composable
fun CronometroScreen(TipoId: String, Folio: Int, Fecha: String, Status: String, tiemposViewModel: TiemposViewModel) {
    var time by rememberSaveable { mutableStateOf(0) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val tiempos by tiemposViewModel.tiempos.collectAsState()
    val tiempo = tiempos[Folio]

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
                if (!isRunning) {
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
}



@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
val hours = seconds / 3600
val minutes = (seconds % 3600) / 60
val remainingSeconds = seconds % 60
return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}