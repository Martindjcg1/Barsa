package com.example.barsa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcesoDao {
    @Upsert
    suspend fun upsert(proceso: Proceso)
}

@Dao
interface TiempoDao {
    @Upsert
    suspend fun upsert(tiempo: Tiempo)

    @Delete
    suspend fun delete(tiempo: Tiempo)

    @Query("DELETE FROM tiempos WHERE id = :id AND procesoFolio = :folio AND etapa = :etapa")
    suspend fun deleteTiempoByFolioEtapa(id: Int, folio: Int, etapa: String)

    // Obtener un solo registro de tiempo por procesoFolio (flujo reactivo)
    @Query("SELECT * FROM tiempos WHERE procesoFolio = :procesoFolio AND etapa = :etapa LIMIT 1")
    fun getOneTiempo(procesoFolio: Int, etapa: String): Flow<Tiempo>

    // Obtener todos los tiempos de un proceso ordenados por fechaInicio
    @Query("SELECT * FROM tiempos WHERE procesoFolio = :procesoFolio ORDER BY fechaInicio ASC")
    fun getAllTiempo(procesoFolio: Int): Flow<List<Tiempo>>

    // Obtener el estado isRunning de un tiempo específico
    @Query("SELECT isRunning FROM tiempos WHERE id = :id")
    fun getIsRunning(id: Int): Flow<Boolean>

    // Actualizar el estado isRunning de un tiempo específico
    @Query("UPDATE tiempos SET isRunning = :isRunning WHERE id = :id")
    suspend fun updateIsRunning(id: Int, isRunning: Boolean)

    // Actualizar la duración total del tiempo en segundos
    @Query("UPDATE tiempos SET tiempo = :nuevoTiempo WHERE id = :id AND etapa = :etapa AND isFinished = false")
    suspend fun updateTiempo(id: Int, etapa: String, nuevoTiempo: Int)

    // Actualizar fechaFin de un tiempo específico
    @Query("UPDATE tiempos SET isFinished = :isFinished, fechaFin = :fechaFin WHERE id = :id")
    suspend fun finalizarTiempo(id: Int, isFinished: Boolean, fechaFin: Long)

    // Obtener el estado isFinished de un tiempo específico
    @Query("SELECT isFinished FROM tiempos WHERE id = :id")
    fun getIsFinished(id: Int): Flow<Boolean>
}

@Dao
interface DetencionDao {

    // Insertar o actualizar una detención
    @Upsert
    suspend fun upsert(detencion: Detencion)

    // Eliminar una detención específica
    @Delete
    suspend fun delete(detencion: Detencion)

    // Obtener una única detención por ID (flujo reactivo)
    @Query("SELECT * FROM detenciones WHERE id = :id LIMIT 1")
    fun getOne(id: Int): Flow<Detencion>

    // Obtener todas las detenciones ordenadas por fecha
    @Query("SELECT * FROM detenciones ORDER BY fecha ASC")
    fun getAll(): Flow<List<Detencion>>


    // Actualizar el estado de "activa" en una detención específica
    @Query("UPDATE detenciones SET activa = :activa WHERE id = :id")
    suspend fun setActiva(id: Int, activa: Boolean)

    // Obtener el estado "activa" de una detención específica (flujo reactivo)
    @Query("SELECT activa FROM detenciones WHERE id = :id")
    fun getActiva(id: Int): Flow<Boolean>
}
