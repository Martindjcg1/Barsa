package com.example.barsa

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.barsa.data.room.di.ServiceEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first


class CronometroService : Service() {
    private val cronometros = mutableMapOf<Pair<Int, String>, Long>()  // key: (id, etapa)
    private val startTimes = mutableMapOf<Pair<Int, String>, Long>()   // key: (id, etapa)
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val binder = CronometroBinder()

    private val tiemposRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        ).tiemposRepository()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CronometroService", "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PAUSAR_CRONOMETRO" -> {
                val id = intent.getIntExtra("id", -1)
                val folio = intent.getIntExtra("folio", -1)
                val etapa = intent.getStringExtra("etapa") ?: return START_NOT_STICKY
                Log.d("CronometroService", "Accion pausar cronometro para id=$id, folio=$folio, etapa=$etapa")

                if (id != -1) {
                    val tiempoFinal = stopCronometro(id, etapa)
                    serviceScope.launch {
                        tiemposRepository.updateTiempo(id, etapa, tiempoFinal.toInt())
                        tiemposRepository.updateIsRunning(id, false)
                        Log.d("CronometroService", "Guardado: id=$id, folio=$folio, etapa=$etapa, tiempo=$tiempoFinal s")
                    }.invokeOnCompletion {
                        if (startTimes.isEmpty()) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                            Log.d("CronometroService", "No hay mas cronometros activos, finalizar Foreground Servide")
                        }
                    }
                }
                return START_NOT_STICKY
            }

            else -> {
                val intent = intent ?: return START_NOT_STICKY

                val id = intent.getIntExtra("id", -1)
                val folio = intent.getIntExtra("folio", -1)
                val etapa = intent.getStringExtra("etapa") ?: return START_NOT_STICKY
                val tiempoInicial = intent.getLongExtra("tiempoInicial", 0L)

                val key = id to etapa
                cronometros[key] = tiempoInicial
                Log.d("CronometroService", "Iniciar cronometro para id=$id, folio=$folio, etapa=$etapa, key=$key")
                if (!isRunning(id, etapa)) {
                    startTimes[key] = System.currentTimeMillis() / 1000
                }

                if (startTimes.size == 1) {
                    startForeground(1, createGeneralNotification())
                }

                updateIndividualNotification(id, folio, etapa)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    inner class CronometroBinder : Binder() {
        fun getService(): CronometroService = this@CronometroService
    }

    // ------------------ Notificaciones ------------------

    private fun createGeneralNotification(): Notification {
        val channelId = "cronometro_channel"
        val channelName = "Cronómetros en ejecución"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cronómetros activos")
            .setContentText("La app está registrando tiempos de producción por etapa.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateIndividualNotification(id: Int, folio: Int, etapa: String) {
        val channelId = "cronometro_individual_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Cronómetro por etapa", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val pauseIntent = Intent(this, CronometroService::class.java).apply {
            action = "PAUSAR_CRONOMETRO"
            putExtra("id", id)
            putExtra("folio", folio)
            putExtra("etapa", etapa)
        }

        val requestCode = (id.toString() + etapa).hashCode()
        Log.d("CronometroService", "updateIndividualNotification hashCode $requestCode")
        val pausePendingIntent = PendingIntent.getService(
            this, requestCode, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Folio $folio - $etapa")
            .setContentText("Cronómetro en ejecución")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Pausar", pausePendingIntent)
            .build()

        notificationManager.notify(requestCode, notification)
    }

    // ------------------ Cronómetro ------------------

    fun isRunning(id: Int, etapa: String): Boolean = startTimes.containsKey(id to etapa)

    fun stopCronometro(id: Int, etapa: String): Long {
        val key = id to etapa
        val tiempoAcumulado = calculateCurrentTime(key)
        cronometros[key] = tiempoAcumulado
        Log.d("CronometroService", "StopCronometro id=$id, etapa=$etapa, key=$key")
        startTimes.remove(key)

        val notificationId = (id.toString() + etapa).hashCode()
        Log.d("CronometroService", "StopCronometro hashCode $notificationId")
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)
        return tiempoAcumulado
    }

    suspend fun stopCronometrosPorFolio(procesoFolio: Int) {
        val tiempos = tiemposRepository.getAllTiempoStream(procesoFolio).first() // ✅ solo obtiene una vez

        for (tiempo in tiempos) {
            val key = tiempo.id to tiempo.etapa

            if (key in cronometros) {
                val tiempoAcumulado = calculateCurrentTime(key)
                Log.d("CronometroService", "Start time de $key: ${startTimes[key]}")
                tiemposRepository.updateTiempo(tiempo.id, tiempo.etapa, tiempoAcumulado.toInt())
                tiemposRepository.updateIsRunning(tiempo.id, false)

                cronometros[key] = 0L
                startTimes.remove(key)
                Log.d("CronometroService", "StartTimes después de remover: $startTimes")

                val notificationId = (tiempo.id.toString() + tiempo.etapa).hashCode()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)

                Log.d("CronometroService", "StopCronometro id=${tiempo.id}, etapa=${tiempo.etapa}, tiempo=$tiempoAcumulado")
            }
        }

        if (getActiveEtapas().isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.d("CronometroService", "Servicio detenido porque no hay cronómetros activos")
        }
    }


    fun resetCronometro(id: Int, etapa: String): Long {
        val key = id to etapa
        cronometros[key] = 0L
        startTimes.remove(key)

        val notificationId = (id.toString() + etapa).hashCode()
        Log.d("CronometroService", "ResetCronometro hashCode $notificationId")
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)
        return 0L
    }

    private fun calculateCurrentTime(key: Pair<Int, String>): Long {
        val tiempoInicial = cronometros[key] ?: 0L
        val startTime = startTimes[key] ?: return tiempoInicial
        val tiempoActual = (System.currentTimeMillis() / 1000) - startTime
        return tiempoInicial + tiempoActual
    }

    fun getCronometroTiempo(id: Int, etapa: String): Long = calculateCurrentTime(id to etapa)

    fun getActiveEtapas(): List<Pair<Int, String>> = startTimes.keys.toList()
}
