package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.Papeleta
import com.example.barsa.data.retrofit.repository.PapeletaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PapeletaViewModel @Inject constructor(
    private val papeletaRepository: PapeletaRepository
) : ViewModel() {

    sealed class PapeletaState {
        object Loading : PapeletaState()
        data class Success(val response: List<Papeleta>, val totalPages: Int, val currentPage: Int) : PapeletaState()
        data class Error(val message: String) : PapeletaState()
    }

    private val _papeletaState = MutableStateFlow<PapeletaState>(PapeletaState.Loading)
    val papeletaState: StateFlow<PapeletaState> = _papeletaState

    fun resetPapeletaState() {
        _papeletaState.value = PapeletaState.Loading
    }

    var currentPage = 1
        private set
    private var totalPages = 1

    fun getListadoPapeletas(page: Int = currentPage, folio: String? = null) {
        currentPage = page
        viewModelScope.launch {
            _papeletaState.value = PapeletaState.Loading
            try {
                val result = if (folio != null) {
                    papeletaRepository.getListadoPapeletas(page = page, folio = folio.toIntOrNull())
                } else {
                    papeletaRepository.getListadoPapeletas(page = page)
                }

                result.onSuccess { response ->
                    totalPages = response.totalPages
                    _papeletaState.value = PapeletaState.Success(response.data, totalPages, currentPage)
                }.onFailure { error ->
                    _papeletaState.value = PapeletaState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _papeletaState.value = PapeletaState.Error("Error inesperado")
            }
        }
    }


    fun nextPage() {
        if (currentPage < totalPages) getListadoPapeletas(currentPage + 1)
    }

    fun previousPage() {
        if (currentPage > 1) getListadoPapeletas(currentPage - 1)
    }

    /*private val _etapasDisponibles = MutableStateFlow<List<String>>(emptyList())
    val etapasDisponibles: StateFlow<List<String>> = _etapasDisponibles

    fun getEtapasDisponiblesPorFolio(folio: Int) {
        viewModelScope.launch {
            val result = papeletaRepository.getTiemposPorFolio(folio)
            result.onSuccess { tiempos ->
                val etapasFinalizadas = tiempos.filter { it.isFinished }.map { it.etapa }.toSet()
                val maderaFinalizada = "Madera" in etapasFinalizadas
                val produccionFinalizada = "Producción" in etapasFinalizadas

                val disponibles = mutableListOf<String>()
                if (!maderaFinalizada) disponibles.add("Madera")
                if (!produccionFinalizada) disponibles.add("Producción")
                if (disponibles.isEmpty()) {
                    val flujoRestante = listOf("Pintura", "Tapiceria", "Empaque")
                    flujoRestante.firstOrNull { it !in etapasFinalizadas }?.let { disponibles.add(it) }
                }

                _etapasDisponibles.value = disponibles
            }.onFailure { error ->
                Log.e("getEtapasDisponibles", "Error al obtener tiempos", error)
                _etapasDisponibles.value = emptyList()
            }
        }
    }

    private val _etapasFinalizadas = MutableStateFlow<Set<String>>(emptySet())
    val etapasFinalizadas: StateFlow<Set<String>> = _etapasFinalizadas

    fun getEtapasFinalizadasPorFolio(folio: Int) {
        viewModelScope.launch {
            try {
                val result = papeletaRepository.getTiemposPorFolio(folio)
                result.onSuccess { tiempos ->
                    val finalizadas = tiempos.filter { it.isFinished }.map { it.etapa }.toSet()
                    _etapasFinalizadas.value = finalizadas
                }.onFailure { e ->
                    Log.e("getEtapasFinalizadas", "Error de servidor", e)
                    _etapasFinalizadas.value = emptySet()
                }
            } catch (e: Exception) {
                Log.e("getEtapasFinalizadas", "Error inesperado", e)
                _etapasFinalizadas.value = emptySet()
            }
        }
    }*/
    sealed class EtapasState {
        object Loading : EtapasState()
        //data class Success(val finalizadas: Set<String>, val disponibles: List<String>) : EtapasState()
        data class Error(val message: String) : EtapasState()
    }

    private val _etapasState = MutableStateFlow<EtapasState>(EtapasState.Loading)
    val etapasState: StateFlow<EtapasState> = _etapasState

    fun resetEtapasState() {
        _etapasState.value = EtapasState.Loading
    }

    private val _etapasFinalizadas = MutableStateFlow<Set<String>>(emptySet())
    val etapasFinalizadas: StateFlow<Set<String>> = _etapasFinalizadas

    private val _etapasDisponibles = MutableStateFlow<List<String>>(emptyList())
    val etapasDisponibles: StateFlow<List<String>> = _etapasDisponibles

    fun cargarInfoEtapasPorFolio(folio: Int) {
        viewModelScope.launch {
            _etapasState.value = EtapasState.Loading
            try {
                val result = papeletaRepository.getTiemposPorFolio(folio)
                result.onSuccess { tiempos ->
                    // Actualiza etapas finalizadas
                    val finalizadas = tiempos.filter { it.isFinished }.map { it.etapa }.toSet()
                    _etapasFinalizadas.value = finalizadas

                    // Calcula etapas disponibles
                    val disponibles = mutableListOf<String>()
                    if ("Madera" !in finalizadas) disponibles.add("Madera")
                    if ("Producción" !in finalizadas) disponibles.add("Producción")
                    if (disponibles.isEmpty()) {
                        listOf("Pintura", "Tapiceria", "Empaque")
                            .firstOrNull { it !in finalizadas }
                            ?.let { disponibles.add(it) }
                    }
                    _etapasDisponibles.value = disponibles
                }.onFailure { error ->
                    Log.e("cargarInfoEtapasPorFolio", "Error al obtener tiempos")
                    _etapasFinalizadas.value = emptySet()
                    _etapasDisponibles.value = emptyList()
                    _etapasState.value = EtapasState.Error(error.message ?: "Error desconocido")
                }
            }
            catch (e: Exception){
                _etapasState.value = EtapasState.Error("Error inesperado E")
            }
        }
    }


    private val _ultimaDetencion = MutableStateFlow<DetencionRemota?>(null)
    val ultimaDetencion: StateFlow<DetencionRemota?> = _ultimaDetencion

    fun resetDesactivacionState() {
        _desactivacionState.value = null
    }

    fun cargarUltimaDetencionActiva(folio: Int) {
        viewModelScope.launch {
            try {
                val result = papeletaRepository.getUltimaDetencion(folio)
                result.onSuccess { _ultimaDetencion.value = it }
                    .onFailure {
                        Log.e("ultimaDetencionActiva", "Error al obtener detención activa", it)
                        _ultimaDetencion.value = null
                    }
            } catch (e: Exception) {
                Log.e("ultimaDetencionActiva", "Error inesperado", e)
                _ultimaDetencion.value = null
            }
        }
    }

    private val _desactivacionState = MutableStateFlow<Result<String>?>(null)
    val desactivacionState: StateFlow<Result<String>?> = _desactivacionState

    fun desactivarDetencionTiempo(folio: Int, etapa: String) {
        viewModelScope.launch {
            _desactivacionState.value = null
            val result = papeletaRepository.desactivarDetencionTiempo(folio, etapa)
            cargarUltimaDetencionActiva(folio)
            _desactivacionState.value = result
        }
    }


}