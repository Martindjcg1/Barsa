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
                    codigoMat = codigoMat,
                    descripcion = descripcion,
                    unidad = unidad,
                    proceso = proceso,
                    borrado = borrado
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "GetInventoryItems exitoso - ${response.data.size} items")
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
        query: String,
        page: Int = 1,
        limit: Int = 10,
        searchInCode: Boolean = true,
        searchInDescription: Boolean = true
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Buscando items: '$query' - Página: $page")
            Log.d("InventoryViewModel", "Buscar en código: $searchInCode, Buscar en descripción: $searchInDescription")
            _searchState.value = SearchState.Loading

            try {
                val result = inventoryRepository.searchInventoryItems(
                    query = query,
                    page = page,
                    limit = limit,
                    searchInCode = searchInCode,
                    searchInDescription = searchInDescription
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda exitosa - ${response.data.size} items encontrados")
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
        query: String,
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
        code: String,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Buscando por código: '$code'")
            _searchState.value = SearchState.Loading

            try {
                val result = inventoryRepository.searchByCode(code, page, limit)

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por código exitosa - ${response.data.size} items encontrados")
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
        description: String,
        page: Int = 1,
        limit: Int = 10
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Buscando por descripción: '$description'")
            _searchState.value = SearchState.Loading

            try {
                val result = inventoryRepository.searchByDescription(description, page, limit)

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda por descripción exitosa - ${response.data.size} items encontrados")
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
                    query = query,
                    codigoMat = codigoMat,
                    descripcion = descripcion,
                    unidad = unidad,
                    proceso = proceso,
                    borrado = borrado,
                    page = page,
                    limit = limit
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "Búsqueda avanzada exitosa - ${response.data.size} items encontrados")
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
    fun searchInCurrentData(query: String): List<InventoryItem> {
        val currentData = getCurrentInventoryData()
        return if (query.isBlank()) {
            currentData
        } else {
            currentData.filter { item ->
                item.codigoMat.contains(query, ignoreCase = true) ||
                        item.descripcion.contains(query, ignoreCase = true)
            }
        }
    }

    fun createMaterial(
        context: Context,
        codigoMat: String,
        descripcion: String,
        unidad: String,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String,
        cantxunidad: Double,
        proceso: String,
        imageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando createMaterial - Código: $codigoMat")
            Log.d("InventoryViewModel", "Datos: descripcion=$descripcion, unidad=$unidad, pcompra=$pcompra, existencia=$existencia, max=$max, min=$min, inventarioInicial=$inventarioInicial, unidadEntrada=$unidadEntrada, cantxunidad=$cantxunidad, proceso=$proceso")
            Log.d("InventoryViewModel", "Imágenes recibidas: ${imageUris.size}")

            _createMaterialState.value = CreateMaterialState.Loading

            try {
                val result = inventoryRepository.createMaterial(
                    context = context,
                    codigoMat = codigoMat,
                    descripcion = descripcion,
                    unidad = unidad,
                    pcompra = pcompra,
                    existencia = existencia,
                    max = max,
                    min = min,
                    inventarioInicial = inventarioInicial,
                    unidadEntrada = unidadEntrada,
                    cantxunidad = cantxunidad,
                    proceso = proceso,
                    imageUris = imageUris
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "CreateMaterial exitoso: ${response.message}")
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
        codigoMat: String,
        descripcion: String,
        unidad: String,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String,
        cantxunidad: Double,
        proceso: String,
        borrado: Boolean = false,
        newImageUris: List<Uri> = emptyList()
    ) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando updateMaterial - Código: $codigoMat")
            Log.d("InventoryViewModel", "Datos: descripcion=$descripcion, unidad=$unidad, pcompra=$pcompra, existencia=$existencia, max=$max, min=$min, inventarioInicial=$inventarioInicial, unidadEntrada=$unidadEntrada, cantxunidad=$cantxunidad, proceso=$proceso, borrado=$borrado")
            Log.d("InventoryViewModel", "Nuevas imágenes: ${newImageUris.size}")

            _updateMaterialState.value = UpdateMaterialState.Loading

            try {
                val result = inventoryRepository.updateMaterial(
                    context = context,
                    codigoMat = codigoMat,
                    descripcion = descripcion,
                    unidad = unidad,
                    pcompra = pcompra,
                    existencia = existencia,
                    max = max,
                    min = min,
                    inventarioInicial = inventarioInicial,
                    unidadEntrada = unidadEntrada,
                    cantxunidad = cantxunidad,
                    proceso = proceso,
                    borrado = borrado,
                    newImageUris = newImageUris
                )

                result.onSuccess { response ->
                    Log.d("InventoryViewModel", "UpdateMaterial exitoso: ${response.message}")
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

    fun deleteMaterial(codigoMat: String) {
        viewModelScope.launch {
            Log.d("InventoryViewModel", "Iniciando deleteMaterial - Código: $codigoMat")
            _deleteMaterialState.value = DeleteMaterialState.Loading

            try {
                val result = inventoryRepository.deleteMaterial(codigoMat)

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
                }.onFailure { error ->
                    Log.e("InventoryViewModel", "Error al obtener todos los items: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Exception en getAllInventoryItems", e)
                _allItemsState.value = Result.failure(Exception("Error inesperado: ${e.message}"))
            }
        }
    }

    fun changePage(page: Int) {
        _currentPage.value = page
        getInventoryItems(page = page, limit = _itemsPerPage.value)
    }

    fun changeItemsPerPage(limit: Int) {
        _itemsPerPage.value = limit
        getInventoryItems(page = 1, limit = limit) // Resetear a página 1
    }

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

    // Funciones auxiliares para obtener datos actuales
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

    fun getItemByCode(codigoMat: String): InventoryItem? {
        val currentData = getCurrentInventoryData()
        return currentData.find { it.codigoMat == codigoMat }
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