package com.example.barsa

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.barsa.data.retrofit.di.ServiceEntryPointA
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.room.di.ServiceEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first


class CronometroService : Service() {
    private val cronometros = mutableMapOf<Pair<Int, String>, Long>()
    private val startTimes = mutableMapOf<Pair<Int, String>, Long>()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val binder = CronometroBinder()
    private val checkpointJobs = mutableMapOf<Pair<Int, String>, Job>()

    private val tiemposRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPoint::class.java
        ).tiemposRepository()
    }

    private val tiemposRepositoryA by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            ServiceEntryPointA::class.java
        ).papeletasRepository()
    }

    override fun onCreate() {
        super.onCreate()
        /*val prefs = getSharedPreferences("cronometros_prefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all

        allEntries.forEach { entry ->
            val keyParts = entry.key.split("_")
            if (keyParts.size >= 2 && entry.key.endsWith("_tiempo")) {
                val id = keyParts[0].toIntOrNull() ?: return@forEach
                val etapa = keyParts[1]
                val tiempoGuardado = entry.value as? Long ?: 0L
                val key = id to etapa
                cronometros[key] = tiempoGuardado

                val isRunning = prefs.getBoolean("${id}_${etapa}_isRunning", false)
                if (isRunning) {
                    startTimes[key] = System.currentTimeMillis() / 1000
                    startCheckpointJob(id, etapa)
                }
            }
        }*/

        if (startTimes.isNotEmpty()) {
            startForeground(1, createGeneralNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PAUSAR_CRONOMETRO" -> {
                val id = intent.getIntExtra("id", -1)
                val folio = intent.getIntExtra("folio", -1)
                val etapa = intent.getStringExtra("etapa") ?: return START_NOT_STICKY
                val fechaPausa = intent.getStringExtra("fechaPausa") ?: return START_NOT_STICKY

                if (id != -1) {
                    val tiempoFinal = stopCronometro(id, etapa)

                    serviceScope.launch {
                        tiemposRepository.updateTiempo(id, etapa, tiempoFinal.toInt())
                        tiemposRepository.updateIsRunning(id, false)
                        tiemposRepositoryA.pausarTiempo(PausarTiempoRequest(folio, etapa, tiempoFinal.toInt(), fechaPausa))
                        removeFromSharedPreferences(id, etapa)
                    }.invokeOnCompletion {
                        if (startTimes.isEmpty()) {
                            clearAllCronometros()
                        }
                    }
                }
                return START_NOT_STICKY
            }

            else -> {
                val id = intent?.getIntExtra("id", -1) ?: return START_NOT_STICKY
                val folio = intent.getIntExtra("folio", -1)
                val etapa = intent.getStringExtra("etapa") ?: return START_NOT_STICKY
                val tiempoInicial = intent.getLongExtra("tiempoInicial", 0L)

                val key = id to etapa
                    cronometros[key] = tiempoInicial

                if (!isRunning(id, etapa)) {
                    startTimes[key] = System.currentTimeMillis() / 1000
                    //saveToSharedPreferences(id, etapa, cronometros[key] ?: 0L)
                    startCheckpointJob(id, etapa)
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
        clearAllCronometros()
        super.onDestroy()
    }

    inner class CronometroBinder : Binder() {
        fun getService(): CronometroService = this@CronometroService
    }

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

    private fun saveToSharedPreferences(id: Int, etapa: String, tiempo: Long) {
        val prefs = getSharedPreferences("cronometros_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("${id}_${etapa}_tiempo", tiempo)
            .putLong("${id}_${etapa}_lastUpdate", System.currentTimeMillis())
            .putBoolean("${id}_${etapa}_isRunning", isRunning(id, etapa))
            .apply()
    }

    fun removeFromSharedPreferences(id: Int, etapa: String) {
        val prefs = getSharedPreferences("cronometros_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("${id}_${etapa}_tiempo")
            .remove("${id}_${etapa}_lastUpdate")
            .remove("${id}_${etapa}_isRunning")
            .apply()
    }

    private fun startCheckpointJob(id: Int, etapa: String) {
        val key = id to etapa
        if (!checkpointJobs.containsKey(key)) {
            val job = serviceScope.launch {
                while (isRunning(id, etapa)) {
                    delay(10_000L)
                    val tiempoActual = calculateCurrentTime(key)
                    tiemposRepository.updateTiempo(id, etapa, tiempoActual.toInt())
                   // saveToSharedPreferences(id, etapa, tiempoActual)
                }
            }
            checkpointJobs[key] = job
        }
    }

    private fun clearAllCronometros() {
        getSharedPreferences("cronometros_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun calculateCurrentTime(key: Pair<Int, String>): Long {
        val tiempoInicial = cronometros[key] ?: 0L
        val startTime = startTimes[key] ?: return tiempoInicial
        return tiempoInicial + (System.currentTimeMillis() / 1000 - startTime)
    }

    fun getCronometroTiempo(id: Int, etapa: String): Long = calculateCurrentTime(id to etapa)

    fun getActiveEtapas(): List<Pair<Int, String>> = startTimes.keys.toList()

    fun isRunning(id: Int, etapa: String): Boolean = startTimes.containsKey(id to etapa)

    fun stopCronometro(id: Int, etapa: String): Long {
        val key = id to etapa
        val tiempoAcumulado = calculateCurrentTime(key)
        cronometros[key] = tiempoAcumulado

        checkpointJobs[key]?.cancel()
        checkpointJobs.remove(key)
        startTimes.remove(key)

        val notificationId = (id.toString() + etapa).hashCode()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)

        return tiempoAcumulado
    }

    fun resetCronometro(id: Int, etapa: String): Long {
        val key = id to etapa
        cronometros[key] = 0L
        startTimes.remove(key)
        checkpointJobs[key]?.cancel()
        checkpointJobs.remove(key)

        val notificationId = (id.toString() + etapa).hashCode()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)

        removeFromSharedPreferences(id, etapa)

        return 0L
    }

    suspend fun stopCronometrosPorFolio(procesoFolio: Int, fechaPausa: String) {
        val tiempos = tiemposRepository.getAllTiempoStream(procesoFolio).first() // Obtenemos los tiempos una sola vez

        for (tiempo in tiempos) {
            val key = tiempo.id to tiempo.etapa

            if (key in cronometros) {
                val tiempoAcumulado = calculateCurrentTime(key)

                // Actualiza en Room y Access
                tiemposRepository.updateTiempo(tiempo.id, tiempo.etapa, tiempoAcumulado.toInt())
                //saveToSharedPreferences(tiempo.id, tiempo.etapa, tiempoAcumulado)
                tiemposRepository.updateIsRunning(tiempo.id, false)
                tiemposRepositoryA.pausarTiempo(
                    PausarTiempoRequest(tiempo.procesoFolio, tiempo.etapa, tiempoAcumulado.toInt(), fechaPausa)
                )

                // Limpieza en memoria
                cronometros.remove(key)
                startTimes.remove(key)
                checkpointJobs[key]?.cancel()
                checkpointJobs.remove(key)

                // Limpieza en disco
                removeFromSharedPreferences(tiempo.id, tiempo.etapa)

                // Cancelar notificación individual
                val notificationId = (tiempo.id.toString() + tiempo.etapa).hashCode()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationId)

                Log.d("CronometroService", "StopCronometro id=${tiempo.id}, etapa=${tiempo.etapa}, tiempo=$tiempoAcumulado")
            }
        }

        // Si ya no queda ningún cronómetro activo, limpiar
        if (getActiveEtapas().isEmpty()) {
            clearAllCronometros()
            Log.d("CronometroService", "Servicio detenido porque no hay cronómetros activos")
        }
    }
}