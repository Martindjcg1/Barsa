package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.CronometroService
import com.example.barsa.Producciones.formatearFechaActual
import com.example.barsa.data.retrofit.models.DetallePapeleta
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.DetencionesInfo
import com.example.barsa.data.retrofit.models.EtapasFinalizadasInfo
import com.example.barsa.data.retrofit.models.InformePeriodo
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.Papeleta
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.example.barsa.data.retrofit.models.TiemposPeriodo
import com.example.barsa.data.retrofit.repository.PapeletaRepository
import com.example.barsa.data.room.repository.TiemposRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PapeletaViewModel @Inject constructor(
    private val papeletaRepository: PapeletaRepository,
   // private val tiemposRepository: TiemposRepository
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
        Log.d("PapeletaViewModel","currentpage ${_currentPage.value} y page $page")
        _currentPage.value = page
        viewModelScope.launch {
            Log.d("PapeletaViewModel","Llamada a la API con pagina $page")
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

    fun resetEtapasFinDis()
    {
        _etapasFinalizadas.value = emptySet()
        _etapasDisponibles.value = emptyList()
    }

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
                    _etapasState.value = EtapasState.Error(error.message ?: "Error desconocido")
                    _etapasFinalizadas.value = emptySet()
                    _etapasDisponibles.value = emptyList()
                    _tiemposEnEjecucion.value = emptyList()

                }
            }
            catch (e: Exception){
                _etapasState.value = EtapasState.Error("Error inesperado E")
                _etapasFinalizadas.value = emptySet()
                _etapasDisponibles.value = emptyList()
                _tiemposEnEjecucion.value = emptyList()

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
                result.onSuccess {
                    _ultimaDetencion.value = it
                    _etapasState.value = EtapasState.Success("Detención actualizada")
                }
                    .onFailure { error ->
                        Log.e("ultimaDetencionActiva", "Error al obtener detención activa", error)
                        _ultimaDetencion.value = null
                        _etapasState.value = EtapasState.Error(error.message ?: "Error inesperado")
                        //_etapasState.value = EtapasState.Success("1")
                        _etapasFinalizadas.value = emptySet()
                        _etapasDisponibles.value = emptyList()
                        _tiemposEnEjecucion.value = emptyList()

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

    sealed class DetencionesEtapaState {
        object Loading : DetencionesEtapaState()
        data class Success(val lista: List<DetencionRemota>) : DetencionesEtapaState()
        data class Error(val message: String) : DetencionesEtapaState()
    }

    private val _detencionesEtapaState = MutableStateFlow<DetencionesEtapaState>(DetencionesEtapaState.Loading)
    val detencionesEtapaState: StateFlow<DetencionesEtapaState> = _detencionesEtapaState

    fun resetDetencionesEtapaState() {
        _detencionesEtapaState.value = DetencionesEtapaState.Loading
    }

    fun cargarDetencionesPorEtapa(folio: Int, etapa: String) {
        viewModelScope.launch {
            _detencionesEtapaState.value = DetencionesEtapaState.Loading
            try {
                val result = papeletaRepository.getDetencionesPorEtapa(folio, etapa)
                result.onSuccess { lista ->
                    _detencionesEtapaState.value = DetencionesEtapaState.Success(lista)
                }.onFailure { error ->
                    _detencionesEtapaState.value = DetencionesEtapaState.Error(error.message ?: "Error al obtener detenciones")
                }
            } catch (e: Exception) {
                _detencionesEtapaState.value = DetencionesEtapaState.Error("Error inesperado al obtener detenciones")
            }
        }
    }

    ///////////////////////////////////////

    sealed class TiemposFolioState {
        object Loading : TiemposFolioState()
        data class Success(val lista: List<TiempoRemoto>) : TiemposFolioState()
        data class Error(val message: String) : TiemposFolioState()
    }

    private val _tiemposFolioState = MutableStateFlow<TiemposFolioState>(TiemposFolioState.Loading)
    val tiemposFolioState: StateFlow<TiemposFolioState> = _tiemposFolioState

    fun resetTiemposFolioState() {
        _tiemposFolioState.value = TiemposFolioState.Loading
    }

    fun cargarTiemposPorFolio(folio: Int) {
        viewModelScope.launch {
            _tiemposFolioState.value = TiemposFolioState.Loading
            try {
                val result = papeletaRepository.getTiemposPorFolio(folio)
                result.onSuccess { lista ->
                    _tiemposFolioState.value = TiemposFolioState.Success(lista)
                }.onFailure { error ->
                    _tiemposFolioState.value =
                        TiemposFolioState.Error(error.message ?: "Error al obtener los tiempos del folio")
                }
            } catch (e: Exception) {
                _tiemposFolioState.value =
                    TiemposFolioState.Error("Error inesperado al obtener los tiempos del folio")
            }
        }
    }

    sealed class TiemposFolioMapState {
        object Loading : TiemposFolioMapState()
        data class Success(val mapa: Map<Int, List<TiempoRemoto>>) : TiemposFolioMapState()
        data class Error(val message: String) : TiemposFolioMapState()
    }

    private val _tiemposMapFolioState = MutableStateFlow<TiemposFolioMapState>(TiemposFolioMapState.Loading)
    val tiemposMapFolioState: StateFlow<TiemposFolioMapState> = _tiemposMapFolioState


    fun resetTiemposMapFolioState() {
        _tiemposMapFolioState.value = TiemposFolioMapState.Loading
    }

    fun cargarTiemposPorFolioMap(folio: Int) {
        viewModelScope.launch {
            val currentMap = when (val state = _tiemposMapFolioState.value) {
                is TiemposFolioMapState.Success -> state.mapa.toMutableMap()
                else -> mutableMapOf()
            }

            try {
                val result = papeletaRepository.getTiemposPorFolio(folio)
                result.onSuccess { lista ->
                    currentMap[folio] = lista // Actualiza solo este folio
                    _tiemposMapFolioState.value = TiemposFolioMapState.Success(currentMap)
                }.onFailure { error ->
                    _tiemposMapFolioState.value = TiemposFolioMapState.Error(
                        error.message ?: "Error al obtener los tiempos del folio $folio"
                    )
                }
            } catch (e: Exception) {
                _tiemposMapFolioState.value = TiemposFolioMapState.Error(
                    "Error inesperado al obtener los tiempos del folio $folio"
                )
            }
        }
    }


    ///////////////////////////////////////7

    sealed class DetencionesFolioState {
        object Loading : DetencionesFolioState()
        data class Success(val lista: List<DetencionRemota>) : DetencionesFolioState()
        data class Error(val message: String) : DetencionesFolioState()
    }


    private val _detencionesFolioState = MutableStateFlow<DetencionesFolioState>(DetencionesFolioState.Loading)
    val detencionesFolioState: StateFlow<DetencionesFolioState> = _detencionesFolioState

    fun resetDetencionesFolioState() {
        _detencionesFolioState.value = DetencionesFolioState.Loading
    }

    fun cargarDetencionesPorFolio(folio: Int) {
        viewModelScope.launch {
            _detencionesFolioState.value = DetencionesFolioState.Loading
            try {
                val result = papeletaRepository.getDetencionesPorFolio(folio)
                result.onSuccess { lista ->
                    _detencionesFolioState.value = DetencionesFolioState.Success(lista)
                }.onFailure { error ->
                    _detencionesFolioState.value = DetencionesFolioState.Error(error.message ?: "Error al obtener detenciones del folio")
                }
            } catch (e: Exception) {
                _detencionesFolioState.value = DetencionesFolioState.Error("Error inesperado al obtener detenciones del folio")
            }
        }
    }

    ///////////////////////////////////////////
    /*sealed class TiemposPeriodoState {
        object Loading : TiemposPeriodoState()
        data class Success(val lista: List<TiemposPeriodo>) : TiemposPeriodoState()
        data class Error(val message: String) : TiemposPeriodoState()
    }

    private val _tiemposPeriodoState = MutableStateFlow<TiemposPeriodoState>(TiemposPeriodoState.Loading)
    val tiemposPeriodoState: StateFlow<TiemposPeriodoState> = _tiemposPeriodoState

    fun resetTiemposPeriodoState() {
        _tiemposPeriodoState.value = TiemposPeriodoState.Loading
    }

    fun cargarTiemposPorPeriodo(fechaInicio: String, fechaFin: String) {
        viewModelScope.launch {
            _tiemposPeriodoState.value = TiemposPeriodoState.Loading
            try {
                val result = papeletaRepository.getTiemposPorPeriodo(fechaInicio, fechaFin)
                result.onSuccess { respuesta ->
                    _tiemposPeriodoState.value = TiemposPeriodoState.Success(respuesta.data)
                }.onFailure { error ->
                    _tiemposPeriodoState.value = TiemposPeriodoState.Error(error.message ?: "Error al obtener tiempos del periodo")
                }
            } catch (e: Exception) {
                _tiemposPeriodoState.value = TiemposPeriodoState.Error("Error inesperado al obtener tiempos del periodo")
            }
        }
    }*/

    sealed class InformePeriodoState {
        object Loading : InformePeriodoState()
        data class Success(val informe: InformePeriodo) : InformePeriodoState()
        data class Error(val message: String) : InformePeriodoState()
    }

    private val _informePeriodoState = MutableStateFlow<InformePeriodoState>(InformePeriodoState.Loading)
    val informePeriodoState: StateFlow<InformePeriodoState> = _informePeriodoState

    fun resetInformePeriodoState() {
        _informePeriodoState.value = InformePeriodoState.Loading
    }

    fun cargarInformePorPeriodo(fechaInicio: String, fechaFin: String) {
        viewModelScope.launch {
            _informePeriodoState.value = InformePeriodoState.Loading
            try {
                val allTiempos = mutableListOf<TiemposPeriodo>()
                var currentPage = 1
                var totalPages: Int

                do {
                    val result = papeletaRepository.getTiemposPorPeriodo(fechaInicio, fechaFin, currentPage)

                    result.onFailure { error ->
                        _informePeriodoState.value = InformePeriodoState.Error(error.message ?: "Error al obtener informe")
                        return@launch  // 🚨 Detener la ejecución por completo
                    }

                    val respuesta = result.getOrNull() ?: break

                    allTiempos.addAll(respuesta.data)

                    totalPages = respuesta.totalPages
                    currentPage++

                } while (currentPage <= totalPages)

                if (allTiempos.isEmpty()) {
                    _informePeriodoState.value = InformePeriodoState.Error("No se encontraron datos en el periodo.")
                    return@launch  // 🚨 Salir para no continuar
                }

                val totalPapeletas = allTiempos.map { it.procesoFolio }.distinct().count()

                val tiemposPorEtapa = allTiempos
                    .groupBy { it.etapa }
                    .mapValues { (_, items) -> items.map { it.tiempo }.average().toFloat() }

                val totalDetenciones = allTiempos.flatMap { it.detenciones }
                val activas = totalDetenciones.count { it.activa }
                val inactivas = totalDetenciones.size - activas

                val detencionesInfo = DetencionesInfo(
                    total = totalDetenciones.size,
                    activas = activas,
                    inactivas = inactivas
                )

                val etapasFinalizadas = allTiempos.filter { it.isFinished }
                val etapasSinFinalizar = allTiempos.filter { !it.isFinished }
                val totalFinalizadas = etapasFinalizadas.size
                val totalSinFinalizar = etapasSinFinalizar.size
                //val nombresEtapas = etapasFinalizadas.map { it.etapa }.distinct()
                /*val promedioTiempoFinalizadas = if (etapasFinalizadas.isNotEmpty()) {
                    etapasFinalizadas.map { it.tiempo }.average().toFloat()
                } else 0f*/

                val etapasFinalizadasInfo = EtapasFinalizadasInfo(
                    totalFinalizadas = totalFinalizadas,
                    totalSinFinalizar = totalSinFinalizar
                    //nombresEtapas = nombresEtapas,
                    //promedioTiempoFinalizadas = promedioTiempoFinalizadas
                )

                val informe = InformePeriodo(
                    totalPapeletas = totalPapeletas,
                    tiempoPromedioPorEtapa = tiemposPorEtapa,
                    detencionesInfo = detencionesInfo,
                    tiemposRaw = allTiempos,
                    etapasFinalizadas = etapasFinalizadasInfo
                )

                _informePeriodoState.value = InformePeriodoState.Success(informe)

            } catch (e: Exception) {
                _informePeriodoState.value = InformePeriodoState.Error("Error inesperado al obtener informe")
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
            //cargarUltimaDetencionActiva(folio)
            _desactivacionState.value = result
            Log.d("PapeletaViewModel", "Desactivar detención ${_desactivacionState.value}")
        }
    }

    private val _mensajeInicioTiempo = MutableStateFlow<Result<String>?>(null)
    val mensajeInicioTiempo: StateFlow<Result<String>?> = _mensajeInicioTiempo

    fun resetMensajeInicioTiempo() {
        _mensajeInicioTiempo.value = null
    }

    fun iniciarTiempo(folio: Int, etapa: String, fechaInicio: String) {
        Log.d("API", "iniciarTiempo Folio: $folio, Etapa: $etapa, fecha: $fechaInicio")
        viewModelScope.launch {
            val result = papeletaRepository.iniciarTiempo(folio, etapa, fechaInicio)
            result.onSuccess {
                _mensajeInicioTiempo.value = result
                Log.d("PapeletaViewModel", "Iniciar ${mensajeInicioTiempo.value}")
            }.onFailure { error ->
                _mensajeInicioTiempo.value = result
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
            Log.d("PapeletaViewModel", "Pausar ${_pausarTiempoResult.value}")
        }
    }

    private val _reiniciarTiempoResult = MutableStateFlow<Result<String>?>(null)
    val reiniciarTiempoResult: StateFlow<Result<String>?> = _reiniciarTiempoResult

    fun resetReiniciarTiempoResult() {
        _reiniciarTiempoResult.value = null
    }

    fun reiniciarTiempo(folio: Int, etapa: String) {
        viewModelScope.launch {
            val result = papeletaRepository.reiniciarTiempo(folio, etapa)
            _reiniciarTiempoResult.value = result
            Log.d("PapeletaViewModel", "Reiniciar ${reiniciarTiempoResult.value}")

            result.onFailure { error ->
                Log.d("PapeletaViewModel", "Reiniciar ${error.message}")
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
            Log.d("PapeletaViewModel", "Finalizar ${finalizarTiempoResult.value}")

            result.onFailure { error ->
                _finalizarTiempoResult.value = result
                Log.e("papeletaViewModel", error.message ?: "Error al finalizar tiempo")
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
                //cargarUltimaDetencionActiva(folio)
                Log.d("PapeletaViewModel", "Detencion ${detencionTiempoResult.value}")
                result.onFailure { error ->
                    Log.e("PapeletaViewModel", error.message ?: "Error al detener tiempo")
                }
        }
    }

    private val _detalleActual = MutableStateFlow<List<DetallePapeleta>>(emptyList())
    val detalleActual: StateFlow<List<DetallePapeleta>> = _detalleActual

    fun setDetalleActual(detalle: List<DetallePapeleta>) {
        _detalleActual.value = detalle
    }

    /*
    private val _hayInconsistencias = MutableStateFlow(false)
    val hayInconsistencias: StateFlow<Boolean> = _hayInconsistencias

    private val _cambiarIsRunning = MutableStateFlow(false)
    val cambiarIsRunning: StateFlow<Boolean> = _cambiarIsRunning

    fun sethayInconsistencias(valor: Boolean)
    {
        _hayInconsistencias.value = valor
    }

    fun verificarYCorregirInconsistencias(cronometroService: CronometroService?) {
        viewModelScope.launch {
            tiemposRepository.getAllTiempoIsRunning()
                .collectLatest { tiemposCorriendo ->
                    tiemposCorriendo.forEach { tiempo ->
                        val estaCorriendoEnService = cronometroService?.isRunning(tiempo.id, tiempo.etapa) ?: false

                        if (!estaCorriendoEnService) {
                            val fechaActual = formatearFechaActual()
                            Log.d("Sync", "⚠️ Inconsistencia: ${tiempo.procesoFolio} - ${tiempo.etapa}, pausando...")

                            pausarTiempo(
                                PausarTiempoRequest(
                                    folio = tiempo.procesoFolio,
                                    etapa = tiempo.etapa,
                                    tiempo = tiempo.tiempo,
                                    fechaPausa = fechaActual
                                )
                            )
                        }
                    }
                }
        }
    }*/

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