package com.example.barsa.Body.Inventory

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory

import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun InventoryItemsList(
    category: InventoryCategory,
    onBackClick: () -> Unit,
    onItemClick: (InventoryItem) -> Unit = {},
    inventoryViewModel: InventoryViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var hasLoadedInitialData by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val searchState by inventoryViewModel.searchState.collectAsState()
    val currentPage by inventoryViewModel.currentPage.collectAsState()

    // Estado derivado más estable - evita recomposiciones innecesarias
    val stableCurrentState by remember {
        derivedStateOf {
            if (isSearching) searchState else inventoryState
        }
    }

    // Items con validación extra y estabilización MEJORADA
    val stableCurrentItems by remember {
        derivedStateOf {
            val items = when (stableCurrentState) {
                is InventoryViewModel.InventoryState.Success -> {
                    (stableCurrentState as InventoryViewModel.InventoryState.Success).response.data?.filterNotNull() ?: emptyList()
                }
                is InventoryViewModel.SearchState.Success -> {
                    (stableCurrentState as InventoryViewModel.SearchState.Success).response.data?.filterNotNull() ?: emptyList()
                }
                else -> emptyList()
            }

            // MEJORADO: Filtrar items usando la función isValid() del modelo actualizado
            items.filter { item ->
                try {
                    // Usar la nueva función isValid() que maneja todos los nulls internamente
                    item.isValid()
                } catch (e: Exception) {
                    Log.e("InventoryItemsList", "Error validando item: ${e.message}")
                    false
                }
            }
        }
    }

    // Información de paginación estable
    val stablePaginationInfo by remember {
        derivedStateOf {
            when (stableCurrentState) {
                is InventoryViewModel.InventoryState.Success -> {
                    Triple(
                        maxOf(1, (stableCurrentState as InventoryViewModel.InventoryState.Success).response.currentPage),
                        maxOf(1, (stableCurrentState as InventoryViewModel.InventoryState.Success).response.totalPages),
                        maxOf(0, (stableCurrentState as InventoryViewModel.InventoryState.Success).response.totalItems)
                    )
                }
                is InventoryViewModel.SearchState.Success -> {
                    Triple(
                        maxOf(1, (stableCurrentState as InventoryViewModel.SearchState.Success).response.currentPage),
                        maxOf(1, (stableCurrentState as InventoryViewModel.SearchState.Success).response.totalPages),
                        maxOf(0, (stableCurrentState as InventoryViewModel.SearchState.Success).response.totalItems)
                    )
                }
                else -> Triple(1, 1, 0)
            }
        }
    }

    // Función para obtener el filtro de categoría
    val getCategoryFilter = remember {
        {
            when (category.name) {
                "Todo" -> null
                "Cubetas" -> "cubeta"
                "Telas" -> "tela"
                "Cascos" -> "casco"
                "Herramientas" -> "Llave"
                "Bisagras y Herrajes" -> "bisagra"
                "Pernos y Sujetadores" -> "perno"
                "Cintas y Adhesivos" -> "cinta"
                "Separadores y Accesorios de Cristal" -> "cristal"
                "Cubrecantos y Acabados" -> "cubre canto"
                else -> null
            }
        }
    }

    // Función para cargar datos con debouncing
    val loadCategoryData = remember {
        { page: Int ->
            val categoryFilter = getCategoryFilter()
            inventoryViewModel.getInventoryItems(
                page = maxOf(1, page),
                descripcion = categoryFilter
            )
        }
    }

    // Debounced search mejorado con manejo seguro de nulls
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().isNotBlank()) {
            delay(300) // Debounce de 300ms
            if (searchQuery.trim().isNotBlank()) { // Verificar de nuevo después del delay
                isSearching = true
                // Pasar el query de forma segura
                inventoryViewModel.searchInventoryItems(
                    query = searchQuery.trim().takeIf { it.isNotBlank() },
                    page = 1
                )
            }
        } else if (isSearching) {
            // Resetear inmediatamente cuando se limpia la búsqueda
            isSearching = false
            inventoryViewModel.resetSearchState()
            loadCategoryData(1)
        }
    }

    // Cargar datos iniciales con mejor control
    LaunchedEffect(category.id) {
        if (!hasLoadedInitialData) {
            // Resetear estados
            isSearching = false
            searchQuery = ""
            inventoryViewModel.resetSearchState()
            // Pequeño delay para asegurar que el reset se complete
            delay(50)
            loadCategoryData(1)
            hasLoadedInitialData = true
        }
    }

    // Resetear flag cuando cambia categoría
    LaunchedEffect(category.id) {
        hasLoadedInitialData = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Barra de búsqueda con manejo mejorado
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                // El LaunchedEffect se encarga del debouncing
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            placeholder = { Text("Buscar en ${category.name}...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            // El LaunchedEffect manejará el reset
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true
        )

        // Indicador de búsqueda
        if (isSearching && searchQuery.isNotBlank()) {
            Text(
                text = "Buscando: \"$searchQuery\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Información de paginación
        if (stablePaginationInfo.third > 0) {
            Text(
                text = "Página ${stablePaginationInfo.first} de ${stablePaginationInfo.second} - Total: ${stablePaginationInfo.third} items",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Contenido principal con mejor manejo de estados
        when (stableCurrentState) {
            is InventoryViewModel.InventoryState.Loading,
            is InventoryViewModel.SearchState.Loading -> {
                LoadingContent(isSearching = isSearching)
            }
            is InventoryViewModel.InventoryState.Error -> {
                ErrorContent(
                    message = (stableCurrentState as InventoryViewModel.InventoryState.Error).message,
                    onRetry = {
                        if (isSearching && searchQuery.isNotBlank()) {
                            inventoryViewModel.searchInventoryItems(
                                query = searchQuery.trim().takeIf { it.isNotBlank() },
                                page = 1
                            )
                        } else {
                            loadCategoryData(1)
                        }
                    }
                )
            }
            is InventoryViewModel.SearchState.Error -> {
                ErrorContent(
                    message = "Error en búsqueda: ${(stableCurrentState as InventoryViewModel.SearchState.Error).message}",
                    onRetry = {
                        if (searchQuery.isNotBlank()) {
                            inventoryViewModel.searchInventoryItems(
                                query = searchQuery.trim().takeIf { it.isNotBlank() },
                                page = 1
                            )
                        }
                    }
                )
            }
            is InventoryViewModel.InventoryState.Success,
            is InventoryViewModel.SearchState.Success -> {
                if (stableCurrentItems.isEmpty()) {
                    EmptyContent(
                        isSearching = isSearching,
                        searchQuery = searchQuery
                    )
                } else {
                    // Grid con items estables
                    StableInventoryGrid(
                        items = stableCurrentItems,
                        onItemClick = { item ->
                            selectedItem = item
                            onItemClick(item)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Controles de paginación (manteniendo tu implementación exacta)
                    if (stablePaginationInfo.second > 1) {
                        PaginationControls(
                            currentPage = stablePaginationInfo.first,
                            totalPages = stablePaginationInfo.second,
                            onPageChange = { newPage ->
                                if (isSearching && searchQuery.isNotBlank()) {
                                    inventoryViewModel.searchInventoryItems(
                                        query = searchQuery.trim().takeIf { it.isNotBlank() },
                                        page = newPage
                                    )
                                } else {
                                    loadCategoryData(newPage)
                                }
                            },
                            isLoading = stableCurrentState is InventoryViewModel.InventoryState.Loading ||
                                    stableCurrentState is InventoryViewModel.SearchState.Loading
                        )
                    }
                }
            }
            else -> {
                LoadingContent(isSearching = false)
            }
        }

        // Dialog de detalles
        selectedItem?.let { item ->
            ItemDetailDialog(
                item = item,
                onDismiss = { selectedItem = null }
            )
        }
    }
}

@Composable
fun StableInventoryGrid(
    items: List<InventoryItem>,
    onItemClick: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Crear una lista inmutable y estable
    val stableItems by remember(items) {
        derivedStateOf {
            items.toList() // Crear copia inmutable
        }
    }

    if (stableItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay elementos para mostrar")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        items(
            count = stableItems.size,
            key = { index ->
                // Key más robusta usando propiedades seguras y la función getSummary()
                when {
                    index >= 0 && index < stableItems.size -> {
                        val item = stableItems[index]
                        try {
                            // Usar getSummary() que maneja internamente todos los nulls
                            "${item.getSummary()}_$index"
                        } catch (e: Exception) {
                            "fallback_safe_item_$index"
                        }
                    }
                    else -> "fallback_item_$index"
                }
            }
        ) { index ->
            // Triple validación antes de renderizar
            if (index >= 0 &&
                index < stableItems.size &&
                stableItems.isNotEmpty()) {
                val item = stableItems.getOrNull(index)
                // MEJORADO: Usar isValid() que maneja todos los nulls
                if (item != null && item.isValid()) {
                    InventoryItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                } else {
                    PlaceholderItemCard()
                }
            } else {
                PlaceholderItemCard()
            }
        }
    }
}

@Composable
fun LoadingContent(isSearching: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearching) "Buscando..." else "Cargando...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun EmptyContent(
    isSearching: Boolean,
    searchQuery: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearching) {
                    "No se encontraron resultados para \"$searchQuery\""
                } else {
                    "No hay items en esta categoría"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlaceholderItemCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    isLoading: Boolean = false
) {
    if (totalPages <= 1) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón anterior
            IconButton(
                onClick = {
                    if (currentPage > 1) {
                        onPageChange(currentPage - 1)
                    }
                },
                enabled = currentPage > 1 && !isLoading
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Página anterior"
                )
            }

            // Páginas
            val pagesToShow = remember(currentPage, totalPages) {
                generateSafePaginationSequence(currentPage, totalPages)
            }

            pagesToShow.forEach { page ->
                if (page == -1) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (page == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        onClick = {
                            if (!isLoading && page != currentPage && page > 0 && page <= totalPages) {
                                onPageChange(page)
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = page.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (page == currentPage)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Botón siguiente
            IconButton(
                onClick = {
                    if (currentPage < totalPages) {
                        onPageChange(currentPage + 1)
                    }
                },
                enabled = currentPage < totalPages && !isLoading
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Página siguiente"
                )
            }
        }
    }
}

// Función mejorada con mejor manejo de edge cases (manteniendo tu implementación)
fun generateSafePaginationSequence(currentPage: Int, totalPages: Int): List<Int> {
    val validCurrentPage = maxOf(1, minOf(currentPage, totalPages))
    val validTotalPages = maxOf(1, totalPages)
    val result = mutableListOf<Int>()

    try {
        if (validTotalPages <= 7) {
            for (i in 1..validTotalPages) {
                result.add(i)
            }
        } else {
            result.add(1)

            if (validCurrentPage > 3) {
                result.add(-1)
            }

            val rangeStart = maxOf(2, validCurrentPage - 1)
            val rangeEnd = minOf(validTotalPages - 1, validCurrentPage + 1)

            for (i in rangeStart..rangeEnd) {
                if (i != 1 && i != validTotalPages) {
                    result.add(i)
                }
            }

            if (validCurrentPage < validTotalPages - 2) {
                result.add(-1)
            }

            if (validTotalPages > 1) {
                result.add(validTotalPages)
            }
        }
    } catch (e: Exception) {
        // Fallback en caso de error
        Log.e("PaginationControls", "Error generando secuencia de paginación", e)
        result.clear()
        result.add(validCurrentPage)
    }

    return result.distinct()
}
