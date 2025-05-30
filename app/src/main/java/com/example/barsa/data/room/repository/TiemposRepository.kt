/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.barsa.data.room.repository

import com.example.barsa.data.room.local.Detencion
import com.example.barsa.data.room.local.Proceso
import com.example.barsa.data.room.local.Tiempo
import kotlinx.coroutines.flow.Flow

interface TiemposRepository {
    // Métodos para procesos
    suspend fun upsertProceso(proceso: Proceso)

    // Métodos para tiempos
    suspend fun upsertTiempo(tiempo: Tiempo)
    suspend fun deleteTiempo(tiempo: Tiempo)
    suspend fun deleteTiempoByFolioEtapa(id: Int, folio: Int, etapa: String)
    fun getAllTiempoStream(procesoFolio: Int): Flow<List<Tiempo>>
    fun getOneTiempoStream(procesoFolio: Int, etapa: String): Flow<Tiempo?>
    suspend fun updateIsRunning(id: Int, isRunning: Boolean)
    suspend fun updateTiempo(id: Int, etapa: String, nuevoTiempo: Int)
    suspend fun finalizarTiempo(id: Int, isFinished: Boolean, fechaFin: Long)
    fun getIsRunningStream(id: Int): Flow<Boolean>
    fun getIsFinishedStream(id: Int): Flow<Boolean>

    // Métodos para detenciones
    suspend fun upsertDetencion(detencion: Detencion)
    suspend fun deleteDetencion(detencion: Detencion)
    fun getOneDetencionStream(tiempoId: Int, etapa: String): Flow<Detencion>
    fun getAllDetencionesStream(): Flow<List<Detencion>>
    suspend fun setActiva(id: Int, activa: Boolean)
    fun getActivaStream(id: Int): Flow<Boolean>
    fun getUltimaDetencionActiva(folioPapeleta: Int): Flow<Detencion?>
}
