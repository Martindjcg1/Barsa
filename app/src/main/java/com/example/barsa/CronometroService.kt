package com.example.barsa

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class CronometroService : Service() {

    private val cronometros = mutableMapOf<Int, Int>() // Mapea Folio -> Tiempo
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val folio = intent?.getIntExtra("folio", -1) ?: return START_NOT_STICKY
        val tiempoInicial = intent.getIntExtra("tiempoInicial", 0)

        if (!cronometros.containsKey(folio)) {
            cronometros[folio] = tiempoInicial
            startCronometro(folio)
        }

        startForeground(1, createNotification("Cronómetros en ejecución..."))
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "cronometro_channel"
        val channelName = "Cronómetro en ejecución"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cronómetro en curso")
            .setContentText("El cronómetro está corriendo...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startCronometro(folio: Int) {
        serviceScope.launch {
            while (true) {
                delay(1000L)
                cronometros[folio] = cronometros[folio]?.plus(1) ?: 0
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val tiempoTotal = cronometros.values.sum()
        val notification = createNotification("Cronómetros activos: ${cronometros.size} | Tiempo total: $tiempoTotal s")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun createNotification(content: String): Notification {
        val channelId = "cronometro_channel"
        val channelName = "Cronómetro Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cronómetros corriendo")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}