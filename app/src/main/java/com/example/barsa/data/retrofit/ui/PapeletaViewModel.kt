package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.Papeleta
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.example.barsa.data.retrofit.repository.PapeletaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var totalPages = 1

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    fun getListadoPapeletas(page: Int = _currentPage.value, folio: String? = null) {
        _currentPage.value = page
        viewModelScope.launch {
            Log.d("PapeletaViewModel","Llamada a la API")
            _papeletaState.value = PapeletaState.Loading
            try {
                val result = if (folio != null) {
                    papeletaRepository.getListadoPapeletas(page = page, folio = folio.toIntOrNull())
                } else {
                    papeletaRepository.getListadoPapeletas(page = page)
                }

                result.onSuccess { response ->
                    totalPages = response.totalPages
                    _papeletaState.value = PapeletaState.Success(response.data, totalPages, _currentPage.value)
                }.onFailure { error ->
                    _papeletaState.value = PapeletaState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _papeletaState.value = PapeletaState.Error("Error inesperado")
            }
        }
    }

    fun nextPage() {
        if (_currentPage.value < totalPages){
            getListadoPapeletas(_currentPage.value + 1)
            Log.d("NextPage", "NP")
        }

    }

    fun previousPage() {
        if (_currentPage.value > 1) {
            getListadoPapeletas(_currentPage.value - 1)
            Log.d("previousPage", "PP")}
    }

    sealed class EtapasState {
        object Loading : EtapasState()
        data class Success(val message: String) : EtapasState()
        data class Error(val message: String) : EtapasState()
    }

    private val _etapasState = MutableStateFlow<EtapasState>(EtapasState.Loading)
    val etapasState: StateFlow<EtapasState> = _etapasState

    fun resetEtapasState() {
        _etapasState.value = EtapasState.Loading
    }

    //private val _objetoTiempo = MutableStateFlow<TiempoRemoto?>(null)
    //val objetoTiempo : Flow<TiempoRemoto?> = _objetoTiempo
    private val _tiemposEnEjecucion = MutableStateFlow<List<TiempoRemoto>>(emptyList())
    val tiemposEnEjecucion: Flow<List<TiempoRemoto>> = _tiemposEnEjecucion.asStateFlow()

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
                        listOf("Pintura", "Armado", "Tapiceria", "Empaque")
                            .firstOrNull { it !in finalizadas }
                            ?.let { disponibles.add(it) }
                    }
                    _etapasDisponibles.value = disponibles
                    _tiemposEnEjecucion.value = tiempos.filter { it.isRunning }
                    _etapasState.value = EtapasState.Success("1")
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

    fun cargarUltimaDetencionActiva(folio: Int) {
        viewModelScope.launch {
            _etapasState.value = EtapasState.Loading
            try {
                val result = papeletaRepository.getUltimaDetencion(folio)
                result.onSuccess { _ultimaDetencion.value = it }
                    .onFailure { error ->
                        Log.e("ultimaDetencionActiva", "Error al obtener detención activa", error)
                        _ultimaDetencion.value = null
                        _etapasState.value = EtapasState.Error(error.message ?: "Error inesperado")
                        _etapasState.value = EtapasState.Success("1")
                    }
            } catch (e: Exception) {
                Log.e("ultimaDetencionActiva", "Error inesperado", e)
                _ultimaDetencion.value = null
            }
        }
    }

    sealed class TiempoEtapaState {
        object Loading : TiempoEtapaState()
        data class Success(val message: String) : TiempoEtapaState()
        data class Error(val message: String) : TiempoEtapaState()
    }

    private val _tiempoEtapasState = MutableStateFlow<TiempoEtapaState>(TiempoEtapaState.Loading)
    val tiempoEtapasState: StateFlow<TiempoEtapaState> = _tiempoEtapasState

    private val _tiempoPorEtapa = MutableStateFlow<TiempoRemoto?>(null)
    val tiempoPorEtapa: StateFlow<TiempoRemoto?> = _tiempoPorEtapa

    fun resetTiempoEtapaState() {
        _tiempoPorEtapa.value = null
        _tiempoEtapasState.value = TiempoEtapaState.Loading
    }

    fun cargarTiempoPorEtapa(folio: Int, etapa: String) {
        viewModelScope.launch {
            _tiempoEtapasState.value = TiempoEtapaState.Loading
            try {
                val result = papeletaRepository.getTiempoPorEtapa(folio, etapa)
                result.onSuccess {
                    _tiempoPorEtapa.value = it
                    _tiempoEtapasState.value = TiempoEtapaState.Success("1")
                }.onFailure {
                    _tiempoPorEtapa.value = null
                    _tiempoEtapasState.value = TiempoEtapaState.Error( it.message ?: "Error al obtener el tiempo")
                }
            } catch (e: Exception) {
                _tiempoPorEtapa.value = null
                _tiempoEtapasState.value = TiempoEtapaState.Error("Error al obtener el tiempo")
            }
        }
    }

    // POST Y PUT

    private val _desactivacionState = MutableStateFlow<Result<String>?>(null)
    val desactivacionState: StateFlow<Result<String>?> = _desactivacionState

    fun resetDesactivacionState() {
        _desactivacionState.value = null
    }

    fun desactivarDetencionTiempo(folio: Int, etapa: String) {
        viewModelScope.launch {
            _desactivacionState.value = null
            val result = papeletaRepository.desactivarDetencionTiempo(folio, etapa)
            cargarUltimaDetencionActiva(folio)
            _desactivacionState.value = result
        }
    }

    private val _mensajeInicioTiempo = MutableStateFlow<String?>(null)
    val mensajeInicioTiempo: StateFlow<String?> = _mensajeInicioTiempo

    fun resetMensajeInicioTiempo() {
        _mensajeInicioTiempo.value = null
    }

    fun iniciarTiempo(folio: Int, etapa: String, fechaInicio: String) {
        Log.d("API", "iniciarTiempo Folio: $folio, Etapa: $etapa, fecha: $fechaInicio")
        viewModelScope.launch {
            val result = papeletaRepository.iniciarTiempo(folio, etapa, fechaInicio)
            result.onSuccess { mensaje ->
                _mensajeInicioTiempo.value = mensaje
                Log.d("iniciarTiempoVM success", mensaje)
            }.onFailure { error ->
                _mensajeInicioTiempo.value = error.message ?: "Error al iniciar tiempo"
                error.message?.let { Log.d("iniciarTiempoVM Error", it) }
            }
        }
    }

    private val _pausarTiempoResult = MutableStateFlow<Result<String>?>(null)
    val pausarTiempoResult: StateFlow<Result<String>?> = _pausarTiempoResult

    fun resetPausarTiempoResult() {
        _pausarTiempoResult.value = null
    }

    fun pausarTiempo(request: PausarTiempoRequest) {
        viewModelScope.launch {
            _pausarTiempoResult.value = null
            val result = papeletaRepository.pausarTiempo(request)
            _pausarTiempoResult.value = result
        }
    }

    private val _reiniciarTiempoResult = MutableStateFlow<Result<String>?>(null)
    val reiniciarTiempoResult: StateFlow<Result<String>?> = _reiniciarTiempoResult

    fun resetReiniciarTiempoResult() {
        _pausarTiempoResult.value = null
    }

    fun reiniciarTiempo(folio: Int, etapa: String) {
        viewModelScope.launch {
            val result = papeletaRepository.reiniciarTiempo(folio, etapa)
            _reiniciarTiempoResult.value = result

            result.onFailure { error ->
                Log.d("reiniciarTiempoVM Error", error.message ?: "Error al reiniciar tiempo")
            }
        }
    }

    private val _finalizarTiempoResult = MutableStateFlow<Result<String>?>(null)
    val finalizarTiempoResult: StateFlow<Result<String>?> = _finalizarTiempoResult

    fun resetFinalizarTiempoResult() {
        _finalizarTiempoResult.value = null
    }

    fun finalizarTiempo(folio: Int, etapa: String, fechaFin: String, tiempo: Int) {
        viewModelScope.launch {
            _finalizarTiempoResult.value = null
            val result = papeletaRepository.finalizarTiempo(folio, etapa, fechaFin, tiempo)
            _finalizarTiempoResult.value = result

            result.onFailure { error ->
                Log.e("finalizarTiempoVM", error.message ?: "Error al finalizar tiempo")
            }
        }
    }

    private val _detencionTiempoResult = MutableStateFlow<Result<String>?>(null)
    val detencionTiempoResult: StateFlow<Result<String>?> = _detencionTiempoResult

    fun resetdetencionTiempoResult() {
        _detencionTiempoResult.value = null
    }

    fun reportarDetencionTiempo(
        tiempo: Int,
        etapa: String,
        folio: Int,
        fecha: String,
        motivo: String
    ) {
        viewModelScope.launch {
                _detencionTiempoResult.value = null
                val result = papeletaRepository.reportarDetencionTiempo(
                    tiempo = tiempo,
                    etapa = etapa,
                    folio = folio,
                    fecha = fecha,
                    motivo = motivo
                )
                _detencionTiempoResult.value = result
                cargarUltimaDetencionActiva(folio)
                result.onFailure { error ->
                    Log.e("detencionTiempoVM", error.message ?: "Error al detener tiempo")
                }
        }
    }

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