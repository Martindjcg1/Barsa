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

package com.example.barsa.data.repository

import com.example.barsa.data.local.Detencion
import com.example.barsa.data.local.DetencionDao
import com.example.barsa.data.local.Proceso
import com.example.barsa.data.local.ProcesoDao
import com.example.barsa.data.local.Tiempo
import com.example.barsa.data.local.TiempoDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineTiemposRepository @Inject constructor(
    private val procesoDao: ProcesoDao,
    private val tiempoDao: TiempoDao,
    private val detencionDao: DetencionDao
) : TiemposRepository {

    // Métodos para procesos
    override suspend fun upsertProceso(proceso: Proceso) = procesoDao.upsert(proceso)

    // Métodos para tiempos
    override suspend fun upsertTiempo(tiempo: Tiempo) = tiempoDao.upsert(tiempo)
    override suspend fun deleteTiempo(tiempo: Tiempo) = tiempoDao.delete(tiempo)
    override suspend fun deleteTiempoByFolioEtapa(id: Int, folio: Int, etapa: String) = tiempoDao.deleteTiempoByFolioEtapa(id, folio, etapa)
    override fun getAllTiempoStream(procesoFolio: Int): Flow<List<Tiempo>> = tiempoDao.getAllTiempo(procesoFolio)
    override fun getOneTiempoStream(procesoFolio: Int, etapa: String): Flow<Tiempo?> = tiempoDao.getOneTiempo(procesoFolio, etapa)
    override suspend fun updateIsRunning(id: Int, isRunning: Boolean) = tiempoDao.updateIsRunning(id, isRunning)
    override suspend fun updateTiempo(id: Int, etapa: String, nuevoTiempo: Int) = tiempoDao.updateTiempo(id, etapa, nuevoTiempo)
    override suspend fun finalizarTiempo(id: Int, isFinished: Boolean, fechaFin: Long) = tiempoDao.finalizarTiempo(id, isFinished, fechaFin)
    override fun getIsRunningStream(id: Int): Flow<Boolean> = tiempoDao.getIsRunning(id)
    override fun getIsFinishedStream(id: Int): Flow<Boolean> = tiempoDao.getIsFinished(id)

    // Métodos para detenciones
    override suspend fun upsertDetencion(detencion: Detencion) = detencionDao.upsert(detencion)
    override suspend fun deleteDetencion(detencion: Detencion) = detencionDao.delete(detencion)
    override fun getOneDetencionStream(id: Int): Flow<Detencion> = detencionDao.getOne(id)
    override fun getAllDetencionesStream(): Flow<List<Detencion>> = detencionDao.getAll()
    override suspend fun setActiva(id: Int, activa: Boolean) = detencionDao.setActiva(id, activa)
    override fun getActivaStream(id: Int): Flow<Boolean> = detencionDao.getActiva(id)
}
