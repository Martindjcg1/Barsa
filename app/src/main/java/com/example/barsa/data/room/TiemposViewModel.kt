package com.example.barsa.data.room

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.room.local.Detencion
import com.example.barsa.data.room.local.Proceso
import com.example.barsa.data.room.local.Tiempo
import com.example.barsa.data.room.repository.TiemposRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TiemposViewModel @Inject constructor(
    private val tiemposRepository: TiemposRepository
) : ViewModel() {

    private val _tiempos = MutableStateFlow<Map<Pair<Int, String>, Tiempo>>(emptyMap())
    val tiempos: StateFlow<Map<Pair<Int, String>, Tiempo>> = _tiempos

    fun getTiempoStream(folio: Int, etapa: String): Flow<Tiempo?> {
        //Log.d("getTiempoStream", "Solicitando tiempo para folio=$folio, etapa=$etapa")
        return tiemposRepository.getOneTiempoStream(folio, etapa)
            .catch { e ->
                Log.e("getTiempoStream", "Error al obtener tiempo para folio=$folio, etapa=$etapa", e)
                emit(null)
            }
    }

    fun fetchTiempo(folio: Int, etapa: String) {
        //Log.d("fetchTiempo", "Fetching tiempo para folio=$folio, etapa=$etapa")
        viewModelScope.launch {
            tiemposRepository.getOneTiempoStream(folio, etapa)
                .catch { e ->
                    Log.e("fetchTiempo", "Error al obtener tiempo", e)
                    emit(null)
                }
                .collectLatest { result ->
                    //Log.d("fetchTiempo", "Resultado obtenido: $result")
                    result?.let {
                        _tiempos.update { tiemposActuales ->
                            tiemposActuales.toMutableMap().apply {
                                put(folio to etapa, it)
                            }
                        }
                        //Log.d("fetchTiempo", "Tiempo actualizado en _tiempos: $it")
                    }
                }
        }
    }

    suspend fun fetchTiempoDirecto(folio: Int, etapa: String): Tiempo? {
        //Log.d("fetchTiempoDirecto", "Accediendo directamente a tiempo folio=$folio, etapa=$etapa")
        return try {
            val tiempo = tiemposRepository.getOneTiempoStream(folio, etapa).firstOrNull()
            //Log.d("fetchTiempoDirecto", "Tiempo obtenido: $tiempo")
            tiempo
        } catch (e: Exception) {
            Log.e("fetchTiempoDirecto", "Error al obtener tiempo", e)
            null
        }
    }

    fun getEtapaDisponible(folio: Int): Flow<List<String>> =
        tiemposRepository.getAllTiempoStream(folio)
            .map { tiempos ->
                val etapasFinalizadas = tiempos.filter { it.isFinished }.map { it.etapa }.toSet()
                val maderaFinalizada = "Madera" in etapasFinalizadas
                val produccionFinalizada = "Producción" in etapasFinalizadas

                val etapasDisponibles = mutableListOf<String>()
                if (!maderaFinalizada) etapasDisponibles.add("Madera")
                if (!produccionFinalizada) etapasDisponibles.add("Producción")
                if (etapasDisponibles.isEmpty()) {
                    val flujoRestante = listOf("Pintura", "Tapiceria", "Empaque")
                    flujoRestante.firstOrNull { it !in etapasFinalizadas }?.let { etapasDisponibles.add(it) }
                }

                etapasDisponibles
            }
            .catch { e ->
                Log.e("getEtapaDisponible", "Error...", e)
            }


    fun getEtapasFinalizadas(folio: Int): Flow<Set<String>> =
        tiemposRepository.getAllTiempoStream(folio)
            .map { tiempos ->
                tiempos.filter { it.isFinished }.map { it.etapa }.toSet()
            }
            .catch { e ->
                Log.e("getEtapasFinalizadas", "Error...", e)
                emit(emptySet())
            }


    fun getIsRunning(folio: Int): Flow<Boolean> {
        //Log.d("getIsRunning", "Consultando isRunning para folio=$folio")
        return tiemposRepository.getIsRunningStream(folio)
            .catch { e ->
                Log.e("getIsRunning", "Error en isRunning para folio=$folio", e)
                emit(false)
            }
    }

    fun getTiempoId(folio: Int, etapa: String): Flow<Int?> {
        //Log.d("getTiempoId", "Obteniendo ID para folio=$folio, etapa=$etapa")
        return tiemposRepository.getOneTiempoStream(folio, etapa)
            .map {
                //Log.d("getTiempoId", "Tiempo encontrado: $it")
                it?.id
            }
            .catch { e ->
                Log.e("getTiempoId", "Error al obtener ID de tiempo", e)
                emit(null)
            }
    }

    suspend fun checkIfProcesoExists(folio: Int): Boolean {
        //Log.d("checkIfProcesoExists", "Verificando si existe proceso folio=$folio")
        val result = tiemposRepository.getAllTiempoStream(folio).firstOrNull()?.isNotEmpty() == true
        //Log.d("checkIfProcesoExists", "Existe: $result")
        return result
    }

    suspend fun checkIfTiempoExists(folio: Int, etapa: String): Boolean {
        //Log.d("checkIfTiempoExists", "Verificando si existe tiempo para folio=$folio, etapa=$etapa")
        val result = tiemposRepository.getOneTiempoStream(folio, etapa).firstOrNull() != null
        //Log.d("checkIfTiempoExists", "Existe: $result")
        return result
    }

    fun updateIsRunningByFolio(folio: Int, etapa: String, isRunning: Boolean) {
        //Log.d("updateIsRunning", "Actualizando isRunning=$isRunning para folio=$folio, etapa=$etapa")
        viewModelScope.launch {
            val tiempoId = getTiempoId(folio, etapa).firstOrNull()
            //Log.d("updateIsRunning", "ID encontrado: $tiempoId")
            if (tiempoId != null) {
                tiemposRepository.updateIsRunning(tiempoId, isRunning)
                //Log.d("updateIsRunning", "isRunning actualizado en base de datos")
            } else {
                //Log.e("updateIsRunning", "Tiempo no encontrado")
            }
        }
    }

    fun upsertProceso(proceso: Proceso) {
        //Log.d("upsertProceso", "Insertando proceso: $proceso")
        viewModelScope.launch {
            try {
                tiemposRepository.upsertProceso(proceso)
                //Log.d("upsertProceso", "Proceso insertado correctamente")
            } catch (e: Exception) {
                Log.e("upsertProceso", "Error al insertar proceso", e)
            }
        }
    }

    fun upsertTiempo(tiempo: Tiempo) {
        //Log.d("upsertTiempo", "Insertando tiempo: $tiempo")
        viewModelScope.launch {
            try {
                val tiempoExistente = fetchTiempoDirecto(tiempo.procesoFolio, tiempo.etapa)
                if (tiempoExistente == null) {
                    tiemposRepository.upsertTiempo(tiempo.copy(isFinished = false))
                    //Log.d("upsertTiempo", "Tiempo insertado")
                } else {
                    //Log.d("upsertTiempo", "Tiempo ya existe, no se inserta")
                }
            } catch (e: Exception) {
                Log.e("upsertTiempo", "Error al insertar tiempo", e)
            }
        }
    }

    fun updateTiempoByFolio(folio: Int, etapa: String, nuevoTiempo: Int) {
        //Log.d("updateTiempo", "Actualizando tiempo a $nuevoTiempo para folio=$folio, etapa=$etapa")
        viewModelScope.launch {
            val tiempoId = getTiempoId(folio, etapa).firstOrNull()
            if (tiempoId != null) {
                tiemposRepository.updateTiempo(tiempoId, etapa, nuevoTiempo)
                //Log.d("updateTiempo", "Tiempo actualizado correctamente")
            } else {
                //Log.e("updateTiempo", "No se encontró el tiempo para actualizar")
            }
        }
    }

    fun deleteTiempoByFolioEtapa(folio: Int, etapa: String) {
        //Log.d("deleteTiempo", "Eliminando tiempo para folio=$folio, etapa=$etapa")
        viewModelScope.launch {
            val tiempoId = getTiempoId(folio, etapa).firstOrNull()
            if (tiempoId != null) {
                tiemposRepository.deleteTiempoByFolioEtapa(tiempoId, folio, etapa)
                //Log.d("deleteTiempo", "Tiempo eliminado")
            } else {
                //Log.e("deleteTiempo", "Tiempo no encontrado para eliminar")
            }
        }
    }

    fun finalizarTiempoByFolioEtapa(folio: Int, etapa: String) {
        //Log.d("finalizarTiempo", "Finalizando tiempo para folio=$folio, etapa=$etapa")
        viewModelScope.launch {
            val tiempoId = getTiempoId(folio, etapa).firstOrNull()
            if (tiempoId != null) {
                tiemposRepository.finalizarTiempo(tiempoId, true, System.currentTimeMillis())
                //Log.d("finalizarTiempo", "Tiempo finalizado")
            } else {
                //Log.e("finalizarTiempo", "Tiempo no encontrado para finalizar")
            }
        }
    }

    fun getDetencionId(folioTiempo: Int, etapa: String): Flow<Int?> {
        //Log.d("getDetencionId", "Obteniendo ID para id=$id")
        return tiemposRepository.getOneDetencionStream(folioTiempo, etapa)
            .map {
                //Log.d("getDetencionId", "Detencion encontrado: $it")
                it?.id
            }
            .catch { e ->
                Log.e("getDetencionId", "Error al obtener ID de detencion", e)
                emit(null)
            }
    }

    fun upsertDetencion(detencion: Detencion) {
        //Log.d("upsertDetencion", "Insertando detencion: $detencion")
        viewModelScope.launch {
            try {
                tiemposRepository.upsertDetencion(detencion)
                //Log.d("upsertDetencion", "Detencion insertado correctamente")
            } catch (e: Exception) {
                Log.e("upsertDetencion", "Error al insertar detencion", e)
            }
        }
    }

    fun getIsDetencionActiva(id: Int): Flow<Boolean> {
        //Log.d("getIsDetencionActiva", "Consultando DetencionActiva para id=$id")
        return tiemposRepository.getActivaStream(id)
            .catch { e ->
                Log.e("getIsDetencionActiva", "Error en DetencionActiva para id=$id", e)
                emit(false)
            }
    }

    fun updateIsDetencionActiva(folioTiempo: Int, etapa: String, isActiva: Boolean) {
        //Log.d("updateIsDetencionActiva", "Actualizando isActiva=$isActiva para id=$id")
        viewModelScope.launch {
            val detencionId = getDetencionId(folioTiempo, etapa).firstOrNull()
            //Log.d("updateIsActiva", "ID encontrado: $id")
            if (detencionId != null) {
                tiemposRepository.setActiva(detencionId, isActiva)
                //Log.d("updateIsDetencionActiva", "isActiva actualizada en base de datos")
            } else {
                //Log.e("updateIsDetencionActiva", "Detencion no encontrada")
            }
        }
    }

    fun ultimaDetencionActiva(folioPapeleta: Int): Flow<Detencion?> {
        return tiemposRepository.getUltimaDetencionActiva(folioPapeleta)
            .catch { e ->
                Log.e("observarUltimaDetencionActiva", "Error al observar última detención activa", e)
                emit(null)
            }
    }

    fun setDetencionActiva(id: Int, isActiva: Boolean) {
        //Log.d("updateIsDetencionActiva", "Actualizando isActiva=$isActiva para id=$id")
        viewModelScope.launch {
            Log.d("updateIsActiva", "ID encontrado: $id, isActiva: $isActiva")
            tiemposRepository.setActiva(id, isActiva)
        }
    }

}

    /*
    fun setIsRunning(folio: Int, isRunning: Boolean) {
        _isRunningMap.value = _isRunningMap.value.toMutableMap().apply {
            put(folio, isRunning)
        }
    }


    fun upsertTiempo(tiempo: Tiempo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tiemposRepository.upsertTiempo(tiempo)
            }
            // Llamar a fetchTiempo para actualizar solo el folio específico
            fetchTiempo(tiempo.folio)
        }
    }


    fun deleteTiempo(folio: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tiemposRepository.deleteTiempo(Tiempo(tipoId = "", folio = folio, fecha = "", status = "", tiempo = 0, isRunning = false))
            }
            _tiempos.value = _tiempos.value.toMutableMap().apply {
                remove(folio)
            }
        }
    }

    fun checkIfFolioExists(folio: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = tiemposRepository.getOneStream(folio).firstOrNull() != null
            Log.d("CheckFolio", "Folio exists: $exists") // Log para depuración
            onResult(exists)
        }
    }


    fun updateIsRunning(folio: Int, isRunning: Boolean) {
        //Log.d("TiemposRepository", "Entrando a updateIsRunning con ${folio} y valor ${isRunning}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tiemposRepository.updateIsRunning(folio, isRunning)
          //      Log.d("TiemposRepository", "Actualizado isRunning para ${folio} con valor ${isRunning}")
            }
            // También actualiza el flujo local
            _isRunningMap.value = _isRunningMap.value.toMutableMap().apply {
                put(folio, isRunning)
            }
        }
    }

    fun updateTiempo(folio: Int, nuevoTiempo: Int) {
        //Log.d("TiemposRepository", "Entrando a updateTiempo con ${folio} y un tiempo de ${nuevoTiempo}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tiemposRepository.updateTiempo(folio, nuevoTiempo)
          //      Log.d("TiemposRepository", "Actualizado tiempo de ${folio}: ${nuevoTiempo} s")
            }
            // Opcional: actualizar el tiempo en el flujo local (si ya se tiene)
            _tiempos.value = _tiempos.value.toMutableMap().apply {
                val current = this[folio]
                if (current != null) {
                    put(folio, current.copy(tiempo = nuevoTiempo))
                }
            }
        }
    }

    fun getIsRunning(folio: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isRunning = tiemposRepository.getIsRunning(folio)
            // Actualizar el mapa local
            _isRunningMap.value = _isRunningMap.value.toMutableMap().apply {
                put(folio, isRunning)
            }
            onResult(isRunning)
        }
    }*/