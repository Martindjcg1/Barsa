package com.example.barsa

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.barsa.data.repository.TiemposRepository
import com.example.barsa.di.ServiceEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import javax.inject.Inject

class CronometroService : Service() {

    private val cronometros = mutableMapOf<Int, Long>()
    private val startTimes = mutableMapOf<Int, Long>()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val binder = CronometroBinder()

    /*override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tipoId = intent?.getStringExtra("tipoId")
        val folio = intent?.getIntExtra("folio", -1) ?: return START_NOT_STICKY
        val fecha = intent.getStringExtra("fecha")
        val status = intent.getStringExtra("status")
        val tiempoInicial = intent.getLongExtra("tiempoInicial", 0L)

        Log.d("CronometroService", "Iniciando folio $folio con $tiempoInicial segundos")

        cronometros[folio] = tiempoInicial

        if (!isRunning(folio)) {
            startTimes[folio] = System.currentTimeMillis() / 1000
        }

        // Inicia Foreground Service con una notificación general
        if (startTimes.size == 1) { // Solo iniciar foreground si es el primer cronómetro
            startForeground(1, createGeneralNotification())
        }

        // Crea o actualiza la notificación individual
        updateIndividualNotification(folio)

        return START_STICKY
    }*/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CronometroService", "onStartCommand recibido con acción: ${intent?.action}")
        when (intent?.action) {
            "PAUSAR_CRONOMETRO" -> {
                val folio = intent.getIntExtra("folio", -1)
                Log.d("CronometroService", "Acción PAUSAR_CRONOMETRO para folio: $folio")
                /*if (folio != -1) {
                    val tiempoFinal = stopCronometro(folio)
                    Log.d("CronometroService", "Entrando al serviceScope.launch para folio ${folio} con un tiempo final de ${tiempoFinal}")
                    serviceScope.launch {
                        Log.d("CronometroService", "Entrando a UpdateTiempo")
                        tiemposRepository.updateTiempo(folio, tiempoFinal.toInt())
                        Log.d("CronometroService", "Entrando a UpdateRunning")
                        tiemposRepository.updateIsRunning(folio, false)
                        Log.d("CronometroService", "Folio $folio pausado y guardado ($tiempoFinal s)")
                    }
                }*/
                if (folio != -1) {
                    val tiempoFinal = stopCronometro(folio)
                    Log.d("CronometroService", "Entrando al serviceScope.launch para folio ${folio} con un tiempo final de ${tiempoFinal}")

                    val job = serviceScope.launch {
                        Log.d("CronometroService", "Entrando a UpdateTiempo")
                        tiemposRepository.updateTiempo(folio, tiempoFinal.toInt())
                        Log.d("CronometroService", "Entrando a UpdateRunning")
                        tiemposRepository.updateIsRunning(folio, false)
                        Log.d("CronometroService", "Folio $folio pausado y guardado ($tiempoFinal s)")
                    }

                    job.invokeOnCompletion {
                        if (startTimes.isEmpty()) {
                            Log.d("CronometroService", "No hay más cronómetros activos. Deteniendo servicio (post-persistencia).")
                            stopForeground(true)
                            stopSelf()
                        }
                    }
                }

                return START_NOT_STICKY
            }

            else -> {
                val tipoId = intent?.getStringExtra("tipoId")
                val folio = intent?.getIntExtra("folio", -1) ?: return START_NOT_STICKY
                val fecha = intent.getStringExtra("fecha")
                val status = intent.getStringExtra("status")
                val tiempoInicial = intent.getLongExtra("tiempoInicial", 0L)

                cronometros[folio] = tiempoInicial
                if (!isRunning(folio)) {
                    startTimes[folio] = System.currentTimeMillis() / 1000
                    Log.d("CronometroService", "StartTime registrado para folio $folio: ${startTimes[folio]}")
                }

                if (startTimes.size == 1) {
                    startForeground(1, createGeneralNotification())
                    Log.d("CronometroService", "Notificación general iniciada")
                }

                updateIndividualNotification(folio)
            }
        }

        return START_STICKY
    }


    private val tiemposRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        ).tiemposRepository()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CronometroService", "Repositorio cargado: $tiemposRepository")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun createGeneralNotification(): Notification {
        val channelId = "cronometro_channel"
        val channelName = "Cronómetros en ejecución"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cronómetros activos")
            .setContentText("La app está registrando tiempos de producción.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    /*private fun updateIndividualNotification(folio: Int) {
        val tiempoActual = calculateCurrentTime(folio)
        val channelId = "cronometro_individual_channel"
        val channelName = "Cronómetro por folio"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Folio $folio")
            .setContentText("Cronómetro en ejecución")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(folio, notification)
    }*/

    private fun updateIndividualNotification(folio: Int) {
        Log.d("CronometroService", "Actualizando notificación individual para folio $folio")
        val channelId = "cronometro_individual_channel"
        val channelName = "Cronómetro por folio"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val pauseIntent = Intent(this, CronometroService::class.java).apply {
            action = "PAUSAR_CRONOMETRO"
            putExtra("folio", folio)
        }
        val pausePendingIntent = PendingIntent.getService(
            this, folio, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Folio $folio")
            .setContentText("Cronómetro en ejecución")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(android.R.drawable.ic_media_pause, "Pausar", pausePendingIntent)
            .build()

        notificationManager.notify(folio, notification)
        Log.d("CronometroService", "Notificación individual mostrada para folio $folio")
    }


    fun isRunning(folio: Int): Boolean = startTimes.containsKey(folio)

    fun stopCronometro(folio: Int): Long {
        val tiempoAcumulado = calculateCurrentTime(folio)
        cronometros[folio] = tiempoAcumulado
        startTimes.remove(folio)

        Log.d("CronometroService", "Cronómetro detenido para folio $folio. Tiempo acumulado: $tiempoAcumulado")

        // Cancelar notificación individual
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(folio)

        // Si no hay cronómetros activos, detener el servicio y su notificación
        if (startTimes.isEmpty()) {
            //Log.d("CronometroService", "No hay más cronómetros activos. Deteniendo servicio.")
            //stopForeground(true)
            //stopSelf()
            //Log.d("CronometroService", "Último cronómetro, tiempo acumulado ${tiempoAcumulado}")
            return tiempoAcumulado
        }
        return tiempoAcumulado
    }

    fun resetCronometro(folio: Int): Long {
        cronometros[folio] = 0L
        startTimes.remove(folio)

        // Cancelar la notificación individual también
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(folio)

        return 0L
    }

    private fun calculateCurrentTime(folio: Int): Long {
        val tiempoInicial = cronometros[folio] ?: 0L
        val startTime = startTimes[folio] ?: return tiempoInicial
        val tiempoActual = (System.currentTimeMillis() / 1000) - startTime
        return tiempoInicial + tiempoActual
    }

    fun getCronometroTiempo(folio: Int): Long = calculateCurrentTime(folio)

    fun getActiveFolios(): List<Int> = startTimes.keys.toList()

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    inner class CronometroBinder : Binder() {
        fun getService(): CronometroService = this@CronometroService
    }
}
