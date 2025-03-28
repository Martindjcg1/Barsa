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

        if (!cronometros.containsKey(folio)) {
            cronometros[folio] = tiempoInicial
            startTimes[folio] = System.currentTimeMillis() / 1000 // Timestamp en segundos
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

    fun stopCronometro(folio: Int): Long {
        val tiempoAcumulado = calculateCurrentTime(folio)
        cronometros[folio] = tiempoAcumulado // Actualiza el tiempo acumulado
        startTimes.remove(folio) // Elimina el timestamp de inicio
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

    fun getCronometroTiempo(folio: Int): Long {
        return calculateCurrentTime(folio)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    inner class CronometroBinder : Binder() {
        fun getService(): CronometroService = this@CronometroService
    }
}