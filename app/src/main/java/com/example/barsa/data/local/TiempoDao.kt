package com.example.barsa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TiempoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tiempo: Tiempo)

    @Update
    suspend fun update(tiempo: Tiempo)

    @Delete
    suspend fun delete(tiempo: Tiempo)

    @Query("SELECT * from tiempos WHERE folio = :folio")
    fun getOne(folio: String): Flow<Tiempo>

    @Query("SELECT * from tiempos ORDER BY fecha ASC")
    fun getAll(): Flow<List<Tiempo>>
}