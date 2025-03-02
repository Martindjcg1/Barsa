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

import com.example.barsa.data.local.Tiempo
import com.example.barsa.data.local.TiempoDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineTiemposRepository @Inject constructor(private val tiempoDao: TiempoDao) : TiemposRepository {
    override suspend fun insertTiempo(tiempo: Tiempo) = tiempoDao.insert(tiempo)
    override suspend fun updateTiempo(tiempo: Tiempo) = tiempoDao.update(tiempo)
    override suspend fun deleteTiempo(tiempo: Tiempo) = tiempoDao.delete(tiempo)
    override fun getAllStream(): Flow<List<Tiempo>> = tiempoDao.getAll()
    override fun getOneStream(folio: String): Flow<Tiempo?> = tiempoDao.getOne(folio)
}
