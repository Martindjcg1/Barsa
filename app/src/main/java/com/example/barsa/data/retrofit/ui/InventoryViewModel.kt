package com.example.barsa.data.retrofit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.models.CreateMaterialResponse
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.models.InventoryPaginationResponse
import com.example.barsa.data.retrofit.repository.InventoryRepository
import javax.inject.Inject
import android.content.Context
import android.net.Uri
import com.example.barsa.data.retrofit.models.CreateMovementDetail
import com.example.barsa.data.retrofit.models.CreateMovementResponseWrapper
import com.example.barsa.data.retrofit.models.DeleteMaterialResponse
import com.example.barsa.data.retrofit.models.InventoryMovementHeader
import com.example.barsa.data.retrofit.models.InventoryMovementsPaginationResponse

import com.example.barsa.data.retrofit.models.UpdateMaterialResponse
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    val tokenManager: TokenManager
) : ViewModel() {
    // ==================== SEALED CLASSES ====================
    sealed class InventoryState {
        object Initial : InventoryState()
        object Loading : InventoryState()
        data class Success(val response: InventoryPaginationResponse) : InventoryState()
        data class Error(val message: String) : InventoryState()
    }

    sealed class SearchState {
        object Initial : SearchState()
        object Loading : SearchState()
        data class Success(val response: InventoryPaginationResponse) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    sealed class CreateMaterialState {
        object Initial : CreateMaterialState()
        object Loading : CreateMaterialState()
        data class Success(val response: CreateMaterialResponse) : CreateMaterialState()
        data class Error(val message: String) : CreateMaterialState()
    }

    sealed class UpdateMaterialState {
        object Initial : UpdateMaterialState()
        object Loading : UpdateMaterialState()
        data class Success(val response: UpdateMaterialResponse) : UpdateMaterialState()
        data class Error(val message: String) : UpdateMaterialState()
    }

    sealed class DeleteMaterialState {
        object Initial : DeleteMaterialState()
        object Loading : DeleteMaterialState()
        data class Success(val response: DeleteMaterialResponse) : DeleteMaterialState()
        data class Error(val message: String) : DeleteMaterialState()
    }

    // ==================== SEALED CLASS PARA MOVIMIENTOS ====================
    sealed class InventoryMovementsState {
        object Initial : InventoryMovementsState()
        object Loading : InventoryMovementsState()
        data class Success(val response: InventoryMovementsPaginationResponse) : InventoryMovementsState()
        data class Error(val message: String) : InventoryMovementsState()
    }

    // ==================== SEALED CLASS PARA CREAR MOVIMIENTO ====================
    sealed class CreateMovementState {
        object Initial : CreateMovementState()
        object Loading : CreateMovementState()
        data class Success(val response: CreateMovementResponseWrapper) : CreateMovementState()
        data class Error(val message: String) : CreateMovementState()
    }

    // ==================== STATE FLOWS ====================
    private val _inventoryState = MutableStateFlow<InventoryState>(InventoryState.Initial)
    val inventoryState: StateFlow<InventoryState> = _inventoryState

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    private val _createMaterialState = MutableStateFlow<CreateMaterialState>(CreateMaterialState.Initial)
    val createMaterialState: StateFlow<CreateMaterialState> = _createMaterialState

    private val _updateMaterialState = MutableStateFlow<UpdateMaterialState>(UpdateMaterialState.Initial)
    val updateMaterialState: StateFlow<UpdateMaterialState> = _updateMaterialState

    private val _deleteMaterialState = MutableStateFlow<DeleteMaterialState>(DeleteMaterialState.Initial)
    val deleteMaterialState: StateFlow<DeleteMaterialState> = _deleteMaterialState

    private val _allItemsState = MutableStateFlow<Result<List<InventoryItem>>?>(null)
    val allItemsState: StateFlow<Result<List<InventoryItem>>?> = _allItemsState

    // ==================== STATE FLOW PARA MOVIMIENTOS ====================
    private val _inventoryMovementsState = MutableStateFlow<InventoryMovementsState>(InventoryMovementsState.Initial)
    val inventoryMovementsState: StateFlow<InventoryMovementsState> = _inventoryMovementsState

    // ==================== STATE FLOW PARA CREAR MOVIMIENTO ====================
    private val _createMovementState = MutableStateFlow<CreateMovementState>(CreateMovementState.Initial)
    val createMovementState: StateFlow<CreateMovementState> = _createMovementState

    // Variables para el manejo de paginación
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _itemsPerPage = MutableStateFlow(10)
    val itemsPerPage: StateFlow<Int> = _itemsPerPage

    // ==================== FUNCIONES AUXILIARES SEGURAS MEJORADAS ====================
    private fun safeString(value: String?): String {
        return try {
            value?.trim()?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun safeOptionalString(value: String?): String? {
        return try {
            value?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    // CORREGIDO: Funciones auxiliares más seguras
    private fun String?.safeTrim(): String {
        return try {
            this?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun String?.safeIsNotBlank(): Boolean {
        return try {
            this?.isNotBlank() == true
        } catch (e: Exception) {
            false
        }
    }

    private fun String?.safeToCleanString(): String? {
        return try {
            this?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    // ==================== FUNCIONES PRINCIPALES DE INVENTARIO ====================
    fun getInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = "false"
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando getInventoryItems - Página: $page")
            _inventoryState.value = InventoryState.Loading
            _currentPage.value = page
            try {
                val result = inventoryRepository.getInventoryItems(
                    page = page,
                    limit = limit,
                    codigoMat = safeOptionalString(codigoMat),
                    descripcion = safeOptionalString(descripcion),
                    unidad = safeOptionalString(unidad),
                    proceso = safeOptionalString(proceso),
                    borrado = safeOptionalString(borrado)
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "GetInventoryItems exitoso - ${response.data.size} items")
                    // Log detallado usando propiedades seguras
                    response.data.forEach { item ->
                        Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "    Stock: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
                        Log.d("InventoryViewModel", "    Precio: ${item.precioFormateado}, Imágenes: ${if (item.tieneImagenes) "Sí" else "No"}")
                    }
                    // Estadísticas usando propiedades seguras
                    val itemsConImagenes = response.data.count { it.tieneImagenes }
                    val itemsStockBajo = response.data.count { it.estadoStock == "Stock bajo" }
                    val itemsSinStock = response.data.count { it.estadoStock == "Sin stock" }
                    Log.d("InventoryViewModel", "Estadísticas - Con imágenes: $itemsConImagenes, Stock bajo: $itemsStockBajo, Sin stock: $itemsSinStock")
                    _inventoryState.value = InventoryState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "GetInventoryItems falló: ${error.message}")
                    _inventoryState.value = InventoryState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en getInventoryItems ViewModel", e)
                _inventoryState.value = InventoryState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Búsqueda mejorada con opciones flexibles
    fun searchInventoryItems(
        query: String?,
        page: Int = 1,
        limit: Int = 10,
        searchInCode: Boolean = true,
        searchInDescription: Boolean = true
    ) {
        viewModelScope.launch {
            // Validar query de forma segura
            val cleanQuery = safeOptionalString(query)
            if (cleanQuery == null) {
                Log.w("InventoryViewModel", "Query de búsqueda vacío o null")
                _searchState.value = SearchState.Error("Consulta de búsqueda vacía")
                return@launch
            }

            Log.d("InventoryViewModel", "Buscando items: '$cleanQuery' - Página: $page")
            Log.d("InventoryViewModel", "Buscar en código: $searchInCode, Buscar en descripción: $searchInDescription")

            _searchState.value = SearchState.Loading
            try {
                val result = inventoryRepository.searchInventoryItems(
                    query = cleanQuery,
                    page = page,
                    limit = limit,
                    searchInCode = searchInCode,
                    searchInDescription = searchInDescription
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda exitosa - ${response.data.size} items encontrados")
                    // Log detallado de resultados usando propiedades seguras
                    response.data.forEach { item ->
                        Log.d("InventoryViewModel", "  Encontrado: ${item.codigoMatSafe} - ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "    Stock: ${item.existenciaFormateada} (${item.estadoStock})")
                        Log.d("InventoryViewModel", "    Precio: ${item.precioFormateado}, Proceso: ${item.procesoSafe}")
                    }
                    _searchState.value = SearchState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda falló: ${error.message}")
                    _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en searchInventoryItems", e)
                _searchState.value = SearchState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función de búsqueda simplificada que busca en ambos campos por defecto
    fun searchInventoryItemsSimple(
        query: String?,
        page: Int = 1,
        limit: Int = 10
    ) {
        searchInventoryItems(
            query = query,
            page = page,
            limit = limit,
            searchInCode = true,
            searchInDescription = true
        )
    }

    // Función para búsqueda específica por código
    fun searchByCode(
        code: String?,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            // Validar código de forma segura
            val cleanCode = safeOptionalString(code)
            if (cleanCode == null) {
                Log.w("InventoryViewModel", "Código de búsqueda vacío o null")
                _searchState.value = SearchState.Error("Código de búsqueda vacío")
                return@launch
            }

            Log.d("InventoryViewModel", "Buscando por código: '$cleanCode'")
            _searchState.value = SearchState.Loading
            try {
                val result = inventoryRepository.searchByCode(cleanCode, page, limit)
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por código exitosa - ${response.data.size} items encontrados")
                    // Log usando propiedades seguras
                    response.data.forEach { item ->
                        Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe} (${item.estadoStock})")
                    }
                    _searchState.value = SearchState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda por código falló: ${error.message}")
                    _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en searchByCode", e)
                _searchState.value = SearchState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función para búsqueda específica por descripción
    fun searchByDescription(
        description: String?,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            // Validar descripción de forma segura
            val cleanDescription = safeOptionalString(description)
            if (cleanDescription == null) {
                Log.w("InventoryViewModel", "Descripción de búsqueda vacía o null")
                _searchState.value = SearchState.Error("Descripción de búsqueda vacía")
                return@launch
            }

            Log.d("InventoryViewModel", "Buscando por descripción: '$cleanDescription'")
            _searchState.value = SearchState.Loading
            try {
                val result = inventoryRepository.searchByDescription(cleanDescription, page, limit)
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por descripción exitosa - ${response.data.size} items encontrados")
                    // Log usando propiedades seguras
                    response.data.forEach { item ->
                        Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "    Stock: ${item.existenciaFormateada}, Precio: ${item.precioFormateado}")
                    }
                    _searchState.value = SearchState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda por descripción falló: ${error.message}")
                    _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en searchByDescription", e)
                _searchState.value = SearchState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función para búsqueda avanzada con múltiples filtros
    fun advancedSearch(
        query: String? = null,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = null,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Búsqueda avanzada - Query: '$query', Código: '$codigoMat', Descripción: '$descripcion'")
            _searchState.value = SearchState.Loading
            try {
                val result = inventoryRepository.advancedSearch(
                    query = safeOptionalString(query),
                    codigoMat = safeOptionalString(codigoMat),
                    descripcion = safeOptionalString(descripcion),
                    unidad = safeOptionalString(unidad),
                    proceso = safeOptionalString(proceso),
                    borrado = safeOptionalString(borrado),
                    page = page,
                    limit = limit
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda avanzada exitosa - ${response.data.size} items encontrados")
                    // Log detallado usando propiedades seguras
                    response.data.forEach { item ->
                        Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "    Unidad: ${item.unidadSafe}, Proceso: ${item.procesoSafe}")
                        Log.d("InventoryViewModel", "    Stock: ${item.existenciaFormateada} (${item.estadoStock})")
                    }
                    _searchState.value = SearchState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda avanzada falló: ${error.message}")
                    _searchState.value = SearchState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en advancedSearch", e)
                _searchState.value = SearchState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función para búsqueda local (en datos ya cargados) - útil para filtrado rápido
    fun searchInCurrentData(query: String?): List<InventoryItem> {
        val currentData = getCurrentInventoryData()
        val cleanQuery = safeOptionalString(query)
        return if (cleanQuery == null) {
            currentData
        } else {
            val filteredItems = currentData.filter { item ->
                // Usar propiedades seguras para la búsqueda
                item.codigoMatSafe.contains(cleanQuery, ignoreCase = true) ||
                        item.descripcionSafe.contains(cleanQuery, ignoreCase = true) ||
                        item.procesoSafe.contains(cleanQuery, ignoreCase = true) ||
                        item.unidadSafe.contains(cleanQuery, ignoreCase = true)
            }
            // Log de resultados de búsqueda local
            Log.d("InventoryViewModel", "Búsqueda local '$cleanQuery' - ${filteredItems.size} de ${currentData.size} items")
            filteredItems.forEach { item ->
                Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe}")
            }
            filteredItems
        }
    }

    // ==================== FUNCIONES PARA MOVIMIENTOS DE INVENTARIO ====================
    fun getInventoryMovements(
        page: Int = 1,
        limit: Int = 10,
        folio: String? = null,
        notes: String? = null,
        usuario: String? = null,
        codigoMat: String? = null,
        descripcion: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando getInventoryMovements - Página: $page")
            _inventoryMovementsState.value = InventoryMovementsState.Loading
            try {
                val result = inventoryRepository.getInventoryMovements(
                    page = page,
                    limit = limit,
                    folio = safeOptionalString(folio),
                    notes = safeOptionalString(notes),
                    usuario = safeOptionalString(usuario),
                    codigoMat = safeOptionalString(codigoMat),
                    descripcion = safeOptionalString(descripcion),
                    fechaInicio = safeOptionalString(fechaInicio),
                    fechaFin = safeOptionalString(fechaFin)
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "GetInventoryMovements exitoso - ${response.data.size} movimientos")
                    // Log detallado usando propiedades seguras
                    response.data.forEach { movement ->
                        Log.d("InventoryViewModel", "  ${movement.tipoMovimiento}: Consecutivo ${movement.consecutivoSafe}")
                        Log.d("InventoryViewModel", "    Usuario: ${movement.usuarioSafe}, Fecha: ${movement.fechaFormateada}")
                        Log.d("InventoryViewModel", "    Detalles: ${movement.detallesSafe.size} items, Total: ${movement.valorTotalFormateado}")
                    }
                    // Estadísticas usando propiedades seguras
                    val entradas = response.data.count { it.isEntry }
                    val salidas = response.data.count { it.isExit }
                    val procesados = response.data.count { it.procesadaSafe }
                    val conObservaciones = response.data.count { it.tieneObservacion }
                    Log.d("InventoryViewModel", "Estadísticas - Entradas: $entradas, Salidas: $salidas")
                    Log.d("InventoryViewModel", "Procesados: $procesados, Con observaciones: $conObservaciones")
                    _inventoryMovementsState.value = InventoryMovementsState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "GetInventoryMovements falló: ${error.message}")
                    _inventoryMovementsState.value = InventoryMovementsState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en getInventoryMovements ViewModel", e)
                _inventoryMovementsState.value = InventoryMovementsState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Búsqueda por usuario
    fun searchMovementsByUser(
        usuario: String?,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            val cleanUsuario = safeOptionalString(usuario)
            if (cleanUsuario == null) {
                Log.w("InventoryViewModel", "Usuario de búsqueda vacío")
                _inventoryMovementsState.value = InventoryMovementsState.Error("Usuario de búsqueda vacío")
                return@launch
            }

            Log.d("InventoryViewModel", "Buscando movimientos por usuario: '$cleanUsuario'")
            _inventoryMovementsState.value = InventoryMovementsState.Loading
            try {
                val result = inventoryRepository.searchMovementsByUser(cleanUsuario, page, limit)
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por usuario exitosa - ${response.data.size} movimientos")
                    response.data.forEach { movement ->
                        Log.d("InventoryViewModel", "  ${movement.tipoMovimiento}: ${movement.consecutivoSafe} (${movement.fechaFormateada})")
                    }
                    _inventoryMovementsState.value = InventoryMovementsState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda por usuario falló: ${error.message}")
                    _inventoryMovementsState.value = InventoryMovementsState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en searchMovementsByUser", e)
                _inventoryMovementsState.value = InventoryMovementsState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Búsqueda por rango de fechas
    fun searchMovementsByDateRange(
        fechaInicio: String?,
        fechaFin: String?,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            val cleanFechaInicio = safeOptionalString(fechaInicio)
            val cleanFechaFin = safeOptionalString(fechaFin)

            if (cleanFechaInicio == null && cleanFechaFin == null) {
                Log.w("InventoryViewModel", "Fechas de búsqueda vacías")
                _inventoryMovementsState.value = InventoryMovementsState.Error("Debe especificar al menos una fecha")
                return@launch
            }

            Log.d("InventoryViewModel", "Buscando movimientos por fechas: $cleanFechaInicio - $cleanFechaFin")
            _inventoryMovementsState.value = InventoryMovementsState.Loading
            try {
                val result = inventoryRepository.searchMovementsByDateRange(
                    fechaInicio = cleanFechaInicio ?: "",
                    fechaFin = cleanFechaFin ?: "",
                    page = page,
                    limit = limit
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por fechas exitosa - ${response.data.size} movimientos")
                    _inventoryMovementsState.value = InventoryMovementsState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Búsqueda por fechas falló: ${error.message}")
                    _inventoryMovementsState.value = InventoryMovementsState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en searchMovementsByDateRange", e)
                _inventoryMovementsState.value = InventoryMovementsState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // ==================== FUNCIONES PARA CREAR MOVIMIENTO CORREGIDAS ====================
    fun createMovement(
        folio: Int,
        movId: Int,
        fecha: String,
        detalles: List<CreateMovementDetail>,
        observacion: String = "",
        autoriza: String = "",
        procesada: Boolean = false
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "=== INICIANDO CREATE MOVEMENT DESDE VIEWMODEL ===")
            Log.d("InventoryViewModel", "Folio: $folio, MovId: $movId, Fecha: $fecha")
            Log.d("InventoryViewModel", "Detalles: ${detalles.size}, Observación: '$observacion'")

            _createMovementState.value = CreateMovementState.Loading
            try {
                val result = inventoryRepository.createMovement(
                    folio = folio,
                    movId = movId,
                    fecha = safeString(fecha).takeIf { it.isNotBlank() } ?: getCurrentDateForMovement(),
                    detalles = detalles,
                    observacion = safeString(observacion),
                    autoriza = safeString(autoriza),
                    procesada = procesada
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "✅ CreateMovement exitoso: ${response.message}")
                    _createMovementState.value = CreateMovementState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "❌ CreateMovement falló: ${error.message}")
                    _createMovementState.value = CreateMovementState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "💥 Exception en createMovement ViewModel", e)
                _createMovementState.value = CreateMovementState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // ✅ FUNCIÓN CORREGIDA PARA CREAR MOVIMIENTO DESDE ITEMS SELECCIONADOS
    fun createMovementFromSelectedItems(
        folio: Int,
        movId: Int,
        fecha: String,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = "",
        procesada: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                Log.d("InventoryViewModel", "=== CREANDO MOVIMIENTO DESDE ITEMS SELECCIONADOS ===")
                Log.d("InventoryViewModel", "Items: ${selectedItems.size}")

                // ✅ VALIDACIÓN MEJORADA DE PARÁMETROS
                val validationError = validateMovementData(folio, movId, selectedItems)
                if (validationError != null) {
                    Log.e("InventoryViewModel", "❌ Error de validación: $validationError")
                    _createMovementState.value = CreateMovementState.Error(validationError)
                    return@launch
                }

                // NUEVO: Validación más robusta de parámetros
                val safeObservacion = safeString(observacion)
                val safeAutoriza = safeString(autoriza)
                val safeFecha = safeString(fecha).takeIf { it.isNotEmpty() }
                    ?: getCurrentDateForMovement()

                Log.d("InventoryViewModel", "📋 Parámetros validados:")
                Log.d("InventoryViewModel", "  📌 Folio: $folio")
                Log.d("InventoryViewModel", "  📌 MovId: $movId")
                Log.d("InventoryViewModel", "  📌 Fecha: $safeFecha")
                Log.d("InventoryViewModel", "  📌 Observación: '$safeObservacion'")
                Log.d("InventoryViewModel", "  📌 Autoriza: '$safeAutoriza'")
                Log.d("InventoryViewModel", "  📌 Procesada: $procesada")

                _createMovementState.value = CreateMovementState.Loading

                val result = inventoryRepository.createMovementFromInventoryItems(
                    folio = folio,
                    movId = movId,
                    fecha = safeFecha,
                    selectedItems = selectedItems,
                    observacion = safeObservacion,
                    autoriza = safeAutoriza,
                    procesada = procesada
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "✅ CreateMovement desde items exitoso: ${response.message}")
                    _createMovementState.value = CreateMovementState.Success(response)

                    // Opcional: recargar los movimientos para mostrar el nuevo
                    delay(500)
                    getInventoryMovements(page = 1, limit = 10)

                }.onFailure { error ->
                    Log.e("InventoryViewModel", "❌ CreateMovement desde items falló: ${error.message}")

                    // ✅ MANEJO ESPECÍFICO DE ERRORES
                    val errorMessage = when {
                        error.message?.contains("folio de papeleta no existe", ignoreCase = true) == true -> {
                            "El folio de papeleta $folio no existe. Verifica el número e intenta nuevamente."
                        }
                        error.message?.contains("Token de acceso no disponible") == true -> {
                            "Sesión expirada. Por favor, inicia sesión nuevamente."
                        }
                        error.message?.contains("Fallo de conexión") == true -> {
                            "Sin conexión a internet. Verifica tu red e intenta nuevamente."
                        }
                        error.message?.contains("Stock insuficiente", ignoreCase = true) == true -> {
                            error.message ?: "Stock insuficiente. Verifica las cantidades disponibles."
                        }
                        else -> error.message ?: "Error desconocido al crear el movimiento"
                    }

                    _createMovementState.value = CreateMovementState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "💥 Exception en createMovementFromSelectedItems", e)
                _createMovementState.value = CreateMovementState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // ==================== NUEVAS FUNCIONES PARA MOVIMIENTOS ESPECÍFICOS ====================
    // Función para crear salida de almacén (movId = 5)
    fun createExitMovement(
        folio: Int,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = ""
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Creando salida de almacén")
            Log.d("InventoryViewModel", "Folio: $folio, Items: ${selectedItems.size}")
            // Validar stock antes de crear el movimiento
            val stockValidation = validateStockForExit(selectedItems)
            if (stockValidation != null) {
                Log.e("InventoryViewModel", "Validación de stock falló: $stockValidation")
                _createMovementState.value = CreateMovementState.Error(stockValidation)
                return@launch
            }
            createMovementFromSelectedItems(
                folio = folio,
                movId = 5, // Salida de almacén
                fecha = getCurrentDateForMovement(),
                selectedItems = selectedItems,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza),
                procesada = false
            )
        }
    }

    // Función para crear entrada de almacén (movId = 4)
    fun createEntryMovement(
        folio: Int,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = ""
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Creando entrada de almacén")
            Log.d("InventoryViewModel", "Folio: $folio, Items: ${selectedItems.size}")
            createMovementFromSelectedItems(
                folio = folio,
                movId = 4, // Entrada de almacén
                fecha = getCurrentDateForMovement(),
                selectedItems = selectedItems,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza),
                procesada = false
            )
        }
    }

    // Función para crear devolución de cliente (movId = 1)
    fun createClientReturnMovement(
        folio: Int,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = ""
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Creando devolución de cliente")
            Log.d("InventoryViewModel", "Folio: $folio, Items: ${selectedItems.size}")
            createMovementFromSelectedItems(
                folio = folio,
                movId = 1, // Devolución de cliente
                fecha = getCurrentDateForMovement(),
                selectedItems = selectedItems,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza),
                procesada = false
            )
        }
    }

    // Función para crear devolución a proveedor (movId = 2)
    fun createSupplierReturnMovement(
        folio: Int,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = ""
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Creando devolución a proveedor")
            Log.d("InventoryViewModel", "Folio: $folio, Items: ${selectedItems.size}")
            // Validar stock antes de crear el movimiento
            val stockValidation = validateStockForExit(selectedItems)
            if (stockValidation != null) {
                Log.e("InventoryViewModel", "Validación de stock falló: $stockValidation")
                _createMovementState.value = CreateMovementState.Error(stockValidation)
                return@launch
            }
            createMovementFromSelectedItems(
                folio = folio,
                movId = 2, // Devolución a proveedor
                fecha = getCurrentDateForMovement(),
                selectedItems = selectedItems,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza),
                procesada = false
            )
        }
    }

    // Función para crear devolución a almacén (movId = 3)
    fun createWarehouseReturnMovement(
        folio: Int,
        selectedItems: List<Pair<InventoryItem, Double>>,
        observacion: String = "",
        autoriza: String = ""
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Creando devolución a almacén")
            Log.d("InventoryViewModel", "Folio: $folio, Items: ${selectedItems.size}")
            createMovementFromSelectedItems(
                folio = folio,
                movId = 3, // Devolución a almacén
                fecha = getCurrentDateForMovement(),
                selectedItems = selectedItems,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza),
                procesada = false
            )
        }
    }

    // ==================== FUNCIONES DE VALIDACIÓN ====================
    // Validar stock para movimientos de salida
    fun validateStockForExit(selectedItems: List<Pair<InventoryItem, Double>>): String? {
        selectedItems.forEach { (item, cantidad) ->
            if (cantidad > item.existenciaSafe) {
                return "Stock insuficiente para ${item.codigoMatSafe}. Disponible: ${item.existenciaFormateada}, Solicitado: ${String.format("%.2f", cantidad)}"
            }
            if (cantidad <= 0) {
                return "La cantidad debe ser mayor a 0 para ${item.codigoMatSafe}"
            }
        }
        return null
    }

    // Reset para crear movimiento
    fun resetCreateMovementState() {
        _createMovementState.value = CreateMovementState.Initial
    }

    // Función helper para obtener la fecha actual en formato requerido
    fun getCurrentDateForMovement(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(Date())
        } catch (e: Exception) {
            "2024-01-01" // Fecha por defecto en caso de error
        }
    }

    // Función para validar datos antes de crear movimiento
    fun validateMovementData(
        folio: Int,
        movId: Int,
        selectedItems: List<Pair<InventoryItem, Double>>
    ): String? {
        return when {
            folio <= 0 -> "El folio debe ser un número mayor a 0"
            movId <= 0 -> "Debe seleccionar un tipo de movimiento válido"
            selectedItems.isEmpty() -> "Debe seleccionar al menos un producto"
            selectedItems.any { it.second <= 0 } -> "Todas las cantidades deben ser mayores a 0"
            selectedItems.any { it.first.codigoMatSafe.isBlank() } -> "Todos los productos deben tener un código válido"
            else -> null // Sin errores
        }
    }

    // Función para generar folio automático
    fun generateAutoFolio(): Int {
        return (System.currentTimeMillis() / 1000).toInt()
    }

    // Función para obtener el nombre del tipo de movimiento
    fun getMovementTypeName(movId: Int): String {
        return when (movId) {
            1 -> "Devolución de Cliente"
            2 -> "Devolución a Proveedor"
            3 -> "Devolución a Almacén"
            4 -> "Entrada a Almacén"
            5 -> "Salida de Almacén"
            else -> "Movimiento Desconocido"
        }
    }

    // Función para obtener el icono del tipo de movimiento
    fun getMovementTypeIcon(movId: Int): String {
        return when (movId) {
            1 -> "↩️" // Devolución cliente
            2 -> "📤" // Devolución proveedor
            3 -> "🔄" // Devolución almacén
            4 -> "📥" // Entrada
            5 -> "📤" // Salida
            else -> "❓"
        }
    }

    // ==================== FUNCIONES CRUD DE MATERIALES ====================
    fun createMaterial(
        context: Context,
        codigoMat: String?,
        descripcion: String?,
        unidad: String?,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String?,
        cantxunidad: Double,
        proceso: String?,
        imageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            // Validar datos de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
            val cleanDescripcion = safeOptionalString(descripcion)
            if (cleanCodigoMat == null) {
                Log.e("InventoryViewModel", "Código de material requerido")
                _createMaterialState.value = CreateMaterialState.Error("Código de material requerido")
                return@launch
            }
            if (cleanDescripcion == null) {
                Log.e("InventoryViewModel", "Descripción requerida")
                _createMaterialState.value = CreateMaterialState.Error("Descripción requerida")
                return@launch
            }

            // Aplicar validaciones de las propiedades seguras
            val safePcompra = maxOf(0.0, pcompra)
            val safeExistencia = maxOf(0.0, existencia)
            val safeMax = maxOf(1.0, max)
            val safeMin = maxOf(0.0, min)
            val safeInventarioInicial = maxOf(0.0, inventarioInicial)
            val safeCantxunidad = maxOf(1.0, cantxunidad)
            val cleanUnidad = safeString(unidad).takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = safeString(unidadEntrada).takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = safeString(proceso).takeIf { it.isNotBlank() } ?: "Sin proceso"

            Log.d("InventoryViewModel", "Iniciando createMaterial - Código: $cleanCodigoMat")
            Log.d("InventoryViewModel", "Datos validados: descripcion=$cleanDescripcion, unidad=$cleanUnidad, pcompra=$safePcompra, existencia=$safeExistencia, max=$safeMax, min=$safeMin, inventarioInicial=$safeInventarioInicial, unidadEntrada=$cleanUnidadEntrada, cantxunidad=$safeCantxunidad, proceso=$cleanProceso")
            Log.d("InventoryViewModel", "Imágenes recibidas: ${imageUris.size}")

            _createMaterialState.value = CreateMaterialState.Loading
            try {
                val result = inventoryRepository.createMaterial(
                    context = context,
                    codigoMat = cleanCodigoMat,
                    descripcion = cleanDescripcion,
                    unidad = cleanUnidad,
                    pcompra = safePcompra,
                    existencia = safeExistencia,
                    max = safeMax,
                    min = safeMin,
                    inventarioInicial = safeInventarioInicial,
                    unidadEntrada = cleanUnidadEntrada,
                    cantxunidad = safeCantxunidad,
                    proceso = cleanProceso,
                    imageUris = imageUris
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "CreateMaterial exitoso: ${response.message}")
                    // Log del item creado usando propiedades seguras
                    response.data?.let { item ->
                        Log.d("InventoryViewModel", "Material creado: ${item.codigoMatSafe} - ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "Stock inicial: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
                        Log.d("InventoryViewModel", "Precio: ${item.precioFormateado}, Rango: ${item.rangoStockFormateado}")
                    }
                    _createMaterialState.value = CreateMaterialState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "CreateMaterial falló: ${error.message}")
                    _createMaterialState.value = CreateMaterialState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en createMaterial ViewModel", e)
                _createMaterialState.value = CreateMaterialState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun updateMaterial(
        context: Context,
        codigoMat: String?,
        descripcion: String?,
        unidad: String?,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String?,
        cantxunidad: Double,
        proceso: String?,
        borrado: Boolean = false,
        newImageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            // Validar datos de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
            val cleanDescripcion = safeOptionalString(descripcion)
            if (cleanCodigoMat == null) {
                Log.e("InventoryViewModel", "Código de material requerido")
                _updateMaterialState.value = UpdateMaterialState.Error("Código de material requerido")
                return@launch
            }
            if (cleanDescripcion == null) {
                Log.e("InventoryViewModel", "Descripción requerida")
                _updateMaterialState.value = UpdateMaterialState.Error("Descripción requerida")
                return@launch
            }

            // Aplicar validaciones de las propiedades seguras
            val safePcompra = maxOf(0.0, pcompra)
            val safeExistencia = maxOf(0.0, existencia)
            val safeMax = maxOf(1.0, max)
            val safeMin = maxOf(0.0, min)
            val safeInventarioInicial = maxOf(0.0, inventarioInicial)
            val safeCantxunidad = maxOf(1.0, cantxunidad)
            val cleanUnidad = safeString(unidad).takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = safeString(unidadEntrada).takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = safeString(proceso).takeIf { it.isNotBlank() } ?: "Sin proceso"

            Log.d("InventoryViewModel", "Iniciando updateMaterial - Código: $cleanCodigoMat")
            Log.d("InventoryViewModel", "Datos validados: descripcion=$cleanDescripcion, unidad=$cleanUnidad, pcompra=$safePcompra, existencia=$safeExistencia, max=$safeMax, min=$safeMin, inventarioInicial=$safeInventarioInicial, unidadEntrada=$cleanUnidadEntrada, cantxunidad=$safeCantxunidad, proceso=$cleanProceso, borrado=$borrado")
            Log.d("InventoryViewModel", "Nuevas imágenes: ${newImageUris.size}")

            _updateMaterialState.value = UpdateMaterialState.Loading
            try {
                val result = inventoryRepository.updateMaterial(
                    context = context,
                    codigoMat = cleanCodigoMat,
                    descripcion = cleanDescripcion,
                    unidad = cleanUnidad,
                    pcompra = safePcompra,
                    existencia = safeExistencia,
                    max = safeMax,
                    min = safeMin,
                    inventarioInicial = safeInventarioInicial,
                    unidadEntrada = cleanUnidadEntrada,
                    cantxunidad = safeCantxunidad,
                    proceso = cleanProceso,
                    borrado = borrado,
                    newImageUris = newImageUris
                )
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "UpdateMaterial exitoso: ${response.message}")
                    // Log del item actualizado usando propiedades seguras
                    response.data?.let { item ->
                        Log.d("InventoryViewModel", "Material actualizado: ${item.codigoMatSafe} - ${item.descripcionSafe}")
                        Log.d("InventoryViewModel", "Stock actualizado: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
                        Log.d("InventoryViewModel", "Precio: ${item.precioFormateado}, Rango: ${item.rangoStockFormateado}")
                    }
                    _updateMaterialState.value = UpdateMaterialState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "UpdateMaterial falló: ${error.message}")
                    _updateMaterialState.value = UpdateMaterialState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en updateMaterial ViewModel", e)
                _updateMaterialState.value = UpdateMaterialState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun deleteMaterial(codigoMat: String?) {
        viewModelScope.launch {
            // Validar código de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
            if (cleanCodigoMat == null) {
                Log.e("InventoryViewModel", "Código de material requerido para eliminar")
                _deleteMaterialState.value = DeleteMaterialState.Error("Código de material requerido")
                return@launch
            }

            Log.d("InventoryViewModel", "Iniciando deleteMaterial - Código: $cleanCodigoMat")
            _deleteMaterialState.value = DeleteMaterialState.Loading
            try {
                val result = inventoryRepository.deleteMaterial(cleanCodigoMat)
                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "DeleteMaterial exitoso: ${response.body.message}")
                    _deleteMaterialState.value = DeleteMaterialState.Success(response)
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "DeleteMaterial falló: ${error.message}")
                    _deleteMaterialState.value = DeleteMaterialState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en deleteMaterial ViewModel", e)
                _deleteMaterialState.value = DeleteMaterialState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun getAllInventoryItems() {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Obteniendo todos los items de inventario")
            try {
                val result = inventoryRepository.getAllInventoryItems()
                _allItemsState.value = result
                result.onSuccess { items ->
                    Log.d("InventoryViewModel", "Todos los items obtenidos exitosamente - ${items.size} items")
                    // Estadísticas usando propiedades seguras
                    val itemsConImagenes = items.count { it.tieneImagenes }
                    val itemsStockBajo = items.count { it.estadoStock == "Stock bajo" }
                    val itemsSinStock = items.count { it.estadoStock == "Sin stock" }
                    val itemsStockNormal = items.count { it.estadoStock == "Stock normal" }
                    val itemsStockAlto = items.count { it.estadoStock == "Stock alto" }

                    Log.d("InventoryViewModel", "Estadísticas generales:")
                    Log.d("InventoryViewModel", "  Con imágenes: $itemsConImagenes")
                    Log.d("InventoryViewModel", "  Sin stock: $itemsSinStock")
                    Log.d("InventoryViewModel", "  Stock bajo: $itemsStockBajo")
                    Log.d("InventoryViewModel", "  Stock normal: $itemsStockNormal")
                    Log.d("InventoryViewModel", "  Stock alto: $itemsStockAlto")

                    // Log de algunos items usando propiedades seguras
                    items.take(5).forEach { item ->
                        Log.d("InventoryViewModel", "  ${item.codigoMatSafe}: ${item.descripcionSafe} (${item.estadoStock})")
                    }
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Error al obtener todos los items: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en getAllInventoryItems", e)
                _allItemsState.value = Result.failure(Exception("Error inesperado: ${e.message}"))
            }
        }
    }

    // ==================== FUNCIONES DE PAGINACIÓN ====================
    fun changePage(page: Int) {
        _currentPage.value = page
        getInventoryItems(page = page, limit = _itemsPerPage.value)
    }

    fun changeItemsPerPage(limit: Int) {
        _itemsPerPage.value = limit
        getInventoryItems(page = 1, limit = limit) // Resetear a página 1
    }

    // ==================== FUNCIONES DE RESET ====================
    fun resetInventoryState() {
        _inventoryState.value = InventoryState.Initial
    }

    fun resetSearchState() {
        _searchState.value = SearchState.Initial
    }

    fun resetCreateMaterialState() {
        _createMaterialState.value = CreateMaterialState.Initial
    }

    fun resetUpdateMaterialState() {
        _updateMaterialState.value = UpdateMaterialState.Initial
    }

    fun resetDeleteMaterialState() {
        _deleteMaterialState.value = DeleteMaterialState.Initial
    }

    fun resetAllItemsState() {
        _allItemsState.value = null
    }

    // Reset para movimientos
    fun resetInventoryMovementsState() {
        _inventoryMovementsState.value = InventoryMovementsState.Initial
    }

    fun clearAllStates() {
        _inventoryState.value = InventoryState.Initial
        _searchState.value = SearchState.Initial
        _createMaterialState.value = CreateMaterialState.Initial
        _updateMaterialState.value = UpdateMaterialState.Initial
        _deleteMaterialState.value = DeleteMaterialState.Initial
        _inventoryMovementsState.value = InventoryMovementsState.Initial
        _createMovementState.value = CreateMovementState.Initial
        _allItemsState.value = null
        _currentPage.value = 1
        _itemsPerPage.value = 10
    }

    // ==================== FUNCIONES AUXILIARES DE INVENTARIO ====================
    fun getCurrentInventoryData(): List<InventoryItem> {
        return when (val currentState = _inventoryState.value) {
            is InventoryState.Success -> currentState.response.data
            else -> emptyList()
        }
    }

    fun getCurrentSearchData(): List<InventoryItem> {
        return when (val currentState = _searchState.value) {
            is SearchState.Success -> currentState.response.data
            else -> emptyList()
        }
    }

    fun hasInventoryData(): Boolean {
        return _inventoryState.value is InventoryState.Success
    }

    fun hasSearchData(): Boolean {
        return _searchState.value is SearchState.Success
    }

    fun getItemByCode(codigoMat: String?): InventoryItem? {
        val currentData = getCurrentInventoryData()
        val cleanCode = safeOptionalString(codigoMat) ?: return null
        return currentData.find { it.codigoMatSafe.equals(cleanCode, ignoreCase = true) }
    }

    // Función para obtener items por estado de stock
    fun getItemsByStockStatus(status: String): List<InventoryItem> {
        val currentData = getCurrentInventoryData()
        return currentData.filter { it.estadoStock == status }
    }

    // Función para obtener items con imágenes
    fun getItemsWithImages(): List<InventoryItem> {
        val currentData = getCurrentInventoryData()
        return currentData.filter { it.tieneImagenes }
    }

    // Función para obtener estadísticas de inventario
    fun getInventoryStats(): Map<String, Int> {
        val currentData = getCurrentInventoryData()
        return mapOf(
            "total" to currentData.size,
            "conImagenes" to currentData.count { it.tieneImagenes },
            "sinStock" to currentData.count { it.estadoStock == "Sin stock" },
            "stockBajo" to currentData.count { it.estadoStock == "Stock bajo" },
            "stockNormal" to currentData.count { it.estadoStock == "Stock normal" },
            "stockAlto" to currentData.count { it.estadoStock == "Stock alto" }
        )
    }

    // ==================== FUNCIONES AUXILIARES PARA MOVIMIENTOS ====================
    fun getCurrentMovementsData(): List<InventoryMovementHeader> {
        return when (val currentState = _inventoryMovementsState.value) {
            is InventoryMovementsState.Success -> currentState.response.data
            else -> emptyList()
        }
    }

    fun hasMovementsData(): Boolean {
        return _inventoryMovementsState.value is InventoryMovementsState.Success
    }

    fun getMovementsByType(movId: Int): List<InventoryMovementHeader> {
        val currentData = getCurrentMovementsData()
        return currentData.filter { it.movIdSafe == movId }
    }

    fun getEntriesMovements(): List<InventoryMovementHeader> {
        val currentData = getCurrentMovementsData()
        return currentData.filter { it.isEntry }
    }

    fun getExitsMovements(): List<InventoryMovementHeader> {
        val currentData = getCurrentMovementsData()
        return currentData.filter { it.isExit }
    }

    fun getMovementsStats(): Map<String, Int> {
        val currentData = getCurrentMovementsData()
        return mapOf(
            "total" to currentData.size,
            "entradas" to currentData.count { it.isEntry },
            "salidas" to currentData.count { it.isExit },
            "procesados" to currentData.count { it.procesadaSafe },
            "conObservaciones" to currentData.count { it.tieneObservacion },
            "devolucionCliente" to currentData.count { it.movIdSafe == 1 },
            "devolucionProveedor" to currentData.count { it.movIdSafe == 2 },
            "devolucionAlmacen" to currentData.count { it.movIdSafe == 3 },
            "entradaAlmacen" to currentData.count { it.movIdSafe == 4 },
            "salidaAlmacen" to currentData.count { it.movIdSafe == 5 }
        )
    }

    // Función para recargar datos después de operaciones CRUD
    fun reloadInventoryData(
        page: Int = 1,
        limit: Int = 100,
        descripcion: String? = null
    ) {
        Log.d("InventoryViewModel", "Recargando datos del inventario después de operación CRUD")
        getInventoryItems(page = page, limit = limit, descripcion = safeOptionalString(descripcion))
    }
}
