package com.example.barsa.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.local.Tiempo
import com.example.barsa.data.repository.OfflineTiemposRepository
import com.example.barsa.data.repository.TiemposRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TiemposViewModel @Inject constructor(
    private val tiemposRepository: TiemposRepository
) : ViewModel() {

    private val _tiempos = MutableStateFlow<Map<Int, Tiempo>>(emptyMap())
    val tiempos: StateFlow<Map<Int, Tiempo>> = _tiempos.asStateFlow()

    private val _isRunningMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isRunningMap: StateFlow<Map<Int, Boolean>> = _isRunningMap.asStateFlow()

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

    fun getTiempoStream(folio: Int): Flow<Tiempo?> {
        return tiemposRepository.getOneStream(folio)
    }


    fun fetchTiempo(folio: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tiemposRepository.getOneStream(folio).collect { result ->
                    // Modifica solo el folio en el mapa
                    _tiempos.value = _tiempos.value.toMutableMap().apply {
                        if (result != null) {
                            put(folio, result) // Actualiza o inserta solo el folio
                        }
                    }
                }
            }
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
    }
}
