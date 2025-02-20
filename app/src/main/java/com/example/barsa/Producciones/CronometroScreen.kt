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
import kotlinx.coroutines.delay

@Composable
fun CronometroScreen() {
    var time by rememberSaveable { mutableStateOf(0) } // Tiempo en segundos
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    //var currentProcess by rememberSaveable { mutableStateOf(1) }
    //val maxProcesses = 4

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            time++
        }
    }

    /*Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    )
    {
        when (currentProcess) {
            1 -> {
                Text(text = "Madera", style = MaterialTheme.typography.headlineLarge)
            }

            2 -> {
                Text(text = "Pintura", style = MaterialTheme.typography.headlineLarge)
            }

            3 -> {
                Text(text = "Tapicería", style = MaterialTheme.typography.headlineLarge)
            }

            4 -> {
                Text(text = "Empaque", style = MaterialTheme.typography.headlineLarge)
            }
        }
    }*/

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(time),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Pausar" else "Iniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { showDialog = true }, enabled = time != 0) {
                Text("Reiniciar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                /*if (currentProcess < maxProcesses) {
                    isRunning = false
                    time = 0
                    currentProcess++
                }*/

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