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
import com.example.barsa.data.retrofit.models.DeleteMaterialResponse
import com.example.barsa.data.retrofit.models.UpdateMaterialResponse

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

    // Variables para el manejo de paginación
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    private val _itemsPerPage = MutableStateFlow(10)
    val itemsPerPage: StateFlow<Int> = _itemsPerPage

    // ==================== FUNCIONES AUXILIARES PARA MANEJO SEGURO DE NULLS ====================
    private fun String?.safeTrim(): String {
        return this?.trim() ?: ""
    }

    private fun String?.safeIsNotBlank(): Boolean {
        return this?.isNotBlank() == true
    }

    private fun String?.safeToCleanString(): String? {
        return this?.trim()?.takeIf { it.isNotBlank() }
    }

    // ==================== FUNCIONES PRINCIPALES ====================
    fun getInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = null
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando getInventoryItems - Página: $page")
            _inventoryState.value = InventoryState.Loading
            _currentPage.value = page

            try {
                val result = inventoryRepository.getInventoryItems(
                    page = page,
                    limit = limit,
                    codigoMat = codigoMat.safeToCleanString(),
                    descripcion = descripcion.safeToCleanString(),
                    unidad = unidad.safeToCleanString(),
                    proceso = proceso.safeToCleanString(),
                    borrado = borrado.safeToCleanString()
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
            val cleanQuery = query.safeToCleanString()
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
            val cleanCode = code.safeToCleanString()
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
            val cleanDescription = description.safeToCleanString()
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
                    query = query.safeToCleanString(),
                    codigoMat = codigoMat.safeToCleanString(),
                    descripcion = descripcion.safeToCleanString(),
                    unidad = unidad.safeToCleanString(),
                    proceso = proceso.safeToCleanString(),
                    borrado = borrado.safeToCleanString(),
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
        val cleanQuery = query.safeToCleanString()

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
            val cleanCodigoMat = codigoMat.safeToCleanString()
            val cleanDescripcion = descripcion.safeToCleanString()

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
            val cleanUnidad = unidad.safeToCleanString() ?: "UND"
            val cleanUnidadEntrada = unidadEntrada.safeToCleanString() ?: "UND"
            val cleanProceso = proceso.safeToCleanString() ?: "Sin proceso"

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
            val cleanCodigoMat = codigoMat.safeToCleanString()
            val cleanDescripcion = descripcion.safeToCleanString()

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
            val cleanUnidad = unidad.safeToCleanString() ?: "UND"
            val cleanUnidadEntrada = unidadEntrada.safeToCleanString() ?: "UND"
            val cleanProceso = proceso.safeToCleanString() ?: "Sin proceso"

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
            val cleanCodigoMat = codigoMat.safeToCleanString()
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

    fun clearAllStates() {
        _inventoryState.value = InventoryState.Initial
        _searchState.value = SearchState.Initial
        _createMaterialState.value = CreateMaterialState.Initial
        _updateMaterialState.value = UpdateMaterialState.Initial
        _deleteMaterialState.value = DeleteMaterialState.Initial
        _allItemsState.value = null
        _currentPage.value = 1
        _itemsPerPage.value = 10
    }

    // ==================== FUNCIONES AUXILIARES ====================
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
        val cleanCode = codigoMat.safeToCleanString() ?: return null

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

    // Función para recargar datos después de operaciones CRUD
    fun reloadInventoryData(
        page: Int = 1,
        limit: Int = 100,
        descripcion: String? = null
    ) {
        Log.d("InventoryViewModel", "Recargando datos del inventario después de operación CRUD")
        getInventoryItems(page = page, limit = limit, descripcion = descripcion)
    }
}
