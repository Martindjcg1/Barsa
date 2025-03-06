package com.example.barsa.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.local.Tiempo
import com.example.barsa.data.repository.OfflineTiemposRepository
import com.example.barsa.data.repository.TiemposRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TiemposViewModel @Inject constructor(
    private val tiemposRepository: TiemposRepository
) : ViewModel() {

    init {
        Log.d("TiemposViewModel", "TiemposRepository injected: $tiemposRepository")
    }

    private val _tiempo = MutableStateFlow<Tiempo?>(null)
    val tiempo: StateFlow<Tiempo?> = _tiempo.asStateFlow()

    fun upsertTiempo(tiempo: Tiempo) {
        viewModelScope.launch {
            tiemposRepository.upsertTiempo(tiempo)
            _tiempo.value = tiempo
        }
    }

    fun fetchTiempo(folio: Int) {
        viewModelScope.launch {
            tiemposRepository.getOneStream(folio).collect { result ->
                _tiempo.value = result
            }
        }
    }
}
