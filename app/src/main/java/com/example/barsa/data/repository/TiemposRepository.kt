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
import kotlinx.coroutines.flow.Flow

interface TiemposRepository {

    /**
     * Insert tiempo in the data source
     */
    suspend fun insertTiempo(tiempo: Tiempo)

    /**
     * Update tiempo in the data source
     */
    suspend fun updateTiempo(tiempo: Tiempo)

    /**
     * Delete from the data source
     */
    suspend fun deleteTiempo(tiempo: Tiempo)

    /**
     * Retrieve all from the the given data source.
     */
    fun getAllStream(): Flow<List<Tiempo>>

    /**
     * Retrieve one from the given data source that matches with the [folio].
     */
    fun getOneStream(folio: String): Flow<Tiempo?>
}
