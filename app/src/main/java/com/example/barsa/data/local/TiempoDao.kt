package com.example.barsa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TiempoDao {
    @Upsert
    suspend fun upsert(tiempo: Tiempo)

    @Delete
    suspend fun delete(tiempo: Tiempo)

    @Query("SELECT * from tiempos WHERE folio = :folio")
    fun getOne(folio: Int): Flow<Tiempo>

    @Query("SELECT * from tiempos ORDER BY fecha ASC")
    fun getAll(): Flow<List<Tiempo>>

    @Query("UPDATE tiempos SET isRunning = :isRunning WHERE folio = :folio")
    suspend fun updateIsRunning(folio: Int, isRunning: Boolean)

    @Query("UPDATE tiempos SET tiempo = :nuevoTiempo WHERE folio = :folio")
    suspend fun updateTiempo(folio: Int, nuevoTiempo: Int)

    @Query("SELECT isRunning FROM tiempos WHERE folio = :folio")
    suspend fun getIsRunning(folio: Int): Boolean
}