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
                tiemposRepository.deleteTiempo(Tiempo(tipoId = "", folio = folio, fecha = "", status = "", tiempo = 0))
            }
            _tiempos.value = _tiempos.value.toMutableMap().apply {
                remove(folio)
            }
        }
    }
}
