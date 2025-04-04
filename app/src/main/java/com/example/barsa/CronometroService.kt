package com.example.barsa

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class CronometroService : Service() {

    private val cronometros = mutableMapOf<Int, Long>() // Mapea Folio -> Tiempo (en segundos)
    private val startTimes = mutableMapOf<Int, Long>() // Mapea Folio -> Timestamp de inicio
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val binder = CronometroBinder()

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val folio = intent?.getIntExtra("folio", -1) ?: return START_NOT_STICKY
        val tiempoInicial = intent.getLongExtra("tiempoInicial", 0L)

        // Siempre actualiza el tiempo almacenado
        cronometros[folio] = tiempoInicial

        // Solo establece el tiempo de inicio si no está corriendo
        if (!isRunning(folio)) {
            startTimes[folio] = System.currentTimeMillis() / 1000
        }

        startForeground(1, createNotification("Cronómetros en ejecución..."))

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun createNotification(content: String): Notification {
        val channelId = "cronometro_channel"
        val channelName = "Cronómetro en ejecución"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cronómetros corriendo")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    fun isRunning(folio: Int): Boolean {
        return startTimes.containsKey(folio)
    }

    fun stopCronometro(folio: Int): Long {
        val tiempoAcumulado = calculateCurrentTime(folio)
        cronometros[folio] = tiempoAcumulado // Guarda el tiempo acumulado
        startTimes.remove(folio) // Elimina el tiempo de inicio (pausa el cronómetro)

        // 🔥 Verificar si todos los cronómetros están pausados
        if (startTimes.isEmpty()) {
            stopForeground(true) // Deja de ser un Foreground Service
            stopSelf() // Mata el servicio solo si no hay cronómetros activos
        } else {
            updateNotification() // Actualiza la notificación con los cronómetros restantes
        }

        return tiempoAcumulado
    }


    fun resetCronometro(folio: Int): Long {
        val tiempoAcumulado = calculateCurrentTime(folio)
        cronometros[folio] = 0L // Reinicia el tiempo acumulado del folio
        startTimes.remove(folio) // Elimina el timestamp de inicio
        return 0L // Devuelve 0 como resultado
    }

    private fun calculateCurrentTime(folio: Int): Long {
        val tiempoInicial = cronometros[folio] ?: 0L
        val startTime = startTimes[folio] ?: return tiempoInicial
        val tiempoActual = (System.currentTimeMillis() / 1000) - startTime
        return tiempoInicial + tiempoActual
    }

    private fun updateNotification() {
        val numCronometros = startTimes.size
        val content = if (numCronometros > 0) {
            "$numCronometros cronómetro(s) en ejecución"
        } else {
            "Todos los cronómetros pausados"
        }

        val notification = createNotification(content)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification) // Actualiza la notificación sin detener el servicio
    }


    fun getCronometroTiempo(folio: Int): Long {
        return calculateCurrentTime(folio)
    }

    fun getActiveFolios(): List<Int> {
        return startTimes.keys.toList()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    inner class CronometroBinder : Binder() {
        fun getService(): CronometroService = this@CronometroService
    }
}