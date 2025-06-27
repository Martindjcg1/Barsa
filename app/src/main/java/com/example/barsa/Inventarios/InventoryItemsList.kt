package com.example.barsa.Body.Inventory

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
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory

import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel
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

    // Obtener estados del ViewModel
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val searchState by inventoryViewModel.searchState.collectAsState()
    val currentPage by inventoryViewModel.currentPage.collectAsState()

    // Determinar qué estado usar (búsqueda o navegación normal)
    val currentState = if (isSearching) searchState else inventoryState
    val currentItems = when (currentState) {
        is InventoryViewModel.InventoryState.Success -> currentState.response.data
        is InventoryViewModel.SearchState.Success -> currentState.response.data
        else -> emptyList()
    }

    // Obtener información de paginación
    val paginationInfo = when (currentState) {
        is InventoryViewModel.InventoryState.Success -> {
            Triple(currentState.response.currentPage, currentState.response.totalPages, currentState.response.totalItems)
        }
        is InventoryViewModel.SearchState.Success -> {
            Triple(currentState.response.currentPage, currentState.response.totalPages, currentState.response.totalItems)
        }
        else -> Triple(1, 1, 0)
    }

    // Función para obtener el filtro de categoría para la API
    fun getCategoryFilter(): String? {
        return when (category.name) {
            "Todo" -> null
            "Cubetas" -> "cubeta"
            "Telas" -> "tela"
            "Cascos" -> "casco"
            "Herramientas" -> "herramienta"
            "Bisagras y Herrajes" -> "bisagra"
            "Pernos y Sujetadores" -> "perno"
            "Cintas y Adhesivos" -> "cinta"
            "Separadores y Accesorios de Cristal" -> "cristal"
            "Cubrecantos y Acabados" -> "cubrecanto"
            else -> null
        }
    }

    // Cargar datos iniciales SOLO UNA VEZ cuando se monta el componente
    LaunchedEffect(category.id) { // Usar category.id como key para evitar recomposiciones innecesarias
        if (!hasLoadedInitialData) {
            isSearching = false
            searchQuery = ""
            inventoryViewModel.resetSearchState()

            val categoryFilter = getCategoryFilter()
            inventoryViewModel.getInventoryItems(
                page = 1,
                descripcion = categoryFilter
            )
            hasLoadedInitialData = true
        }
    }

    // Resetear el flag cuando se cambia de categoría
    LaunchedEffect(category.id) {
        hasLoadedInitialData = false
    }

    Column {
        // Barra superior con botón de regreso y título
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

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                if (newQuery.isNotBlank()) {
                    // Activar modo búsqueda
                    isSearching = true
                    inventoryViewModel.searchInventoryItems(newQuery, page = 1)
                } else {
                    // Volver al modo normal con filtro de categoría
                    isSearching = false
                    inventoryViewModel.resetSearchState()
                    val categoryFilter = getCategoryFilter()
                    inventoryViewModel.getInventoryItems(
                        page = 1,
                        descripcion = categoryFilter
                    )
                }
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
                            isSearching = false
                            inventoryViewModel.resetSearchState()
                            val categoryFilter = getCategoryFilter()
                            inventoryViewModel.getInventoryItems(
                                page = 1,
                                descripcion = categoryFilter
                            )
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true
        )

        // Indicador de modo de búsqueda
        if (isSearching) {
            Text(
                text = "Buscando: \"$searchQuery\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Información de paginación
        Text(
            text = "Página ${paginationInfo.first} de ${paginationInfo.second} - Total: ${paginationInfo.third} items",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Contenido principal según el estado
        when (currentState) {
            is InventoryViewModel.InventoryState.Loading,
            is InventoryViewModel.SearchState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is InventoryViewModel.InventoryState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${currentState.message}")
                        Button(
                            onClick = {
                                if (isSearching) {
                                    inventoryViewModel.searchInventoryItems(searchQuery, page = 1)
                                } else {
                                    val categoryFilter = getCategoryFilter()
                                    inventoryViewModel.getInventoryItems(
                                        page = 1,
                                        descripcion = categoryFilter
                                    )
                                }
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is InventoryViewModel.SearchState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error en búsqueda: ${currentState.message}")
                        Button(
                            onClick = {
                                inventoryViewModel.searchInventoryItems(searchQuery, page = 1)
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is InventoryViewModel.InventoryState.Success,
            is InventoryViewModel.SearchState.Success -> {
                if (currentItems.isEmpty()) {
                    // Mostrar mensaje cuando no hay resultados
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
                                imageVector = Icons.Default.Search,
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
                } else {
                    // Grid de items
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(currentItems) { item ->
                            InventoryItemCard(
                                item = item,
                                onClick = {
                                    selectedItem = item
                                    onItemClick(item)
                                }
                            )
                        }
                    }

                    // Controles de paginación - solo mostrar si hay más de una página
                    if (paginationInfo.second > 1) {
                        PaginationControls(
                            currentPage = paginationInfo.first,
                            totalPages = paginationInfo.second,
                            onPageChange = { newPage ->
                                if (isSearching) {
                                    inventoryViewModel.searchInventoryItems(searchQuery, page = newPage)
                                } else {
                                    val categoryFilter = getCategoryFilter()
                                    inventoryViewModel.getInventoryItems(
                                        page = newPage,
                                        descripcion = categoryFilter
                                    )
                                }
                            },
                            isLoading = currentState is InventoryViewModel.InventoryState.Loading ||
                                    currentState is InventoryViewModel.SearchState.Loading
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Dialog para ver detalles del item
        selectedItem?.let { item ->
            ItemDetailDialog(
                item = item,
                onDismiss = { selectedItem = null }
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
            // Botón para ir a la página anterior
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1 && !isLoading
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Página anterior",
                    tint = if (currentPage > 1 && !isLoading)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray
                )
            }

            // Mostrar números de página
            val pagesToShow = generatePaginationSequence(currentPage, totalPages)

            pagesToShow.forEach { page ->
                if (page == -1) {
                    // Mostrar puntos suspensivos
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    // Mostrar número de página
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (page == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        onClick = {
                            if (!isLoading && page != currentPage) {
                                onPageChange(page)
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading && page == currentPage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = page.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (page == currentPage)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Botón para ir a la página siguiente
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages && !isLoading
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Página siguiente",
                    tint = if (currentPage < totalPages && !isLoading)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray
                )
            }
        }
    }
}

// Función para generar la secuencia de páginas a mostrar
fun generatePaginationSequence(currentPage: Int, totalPages: Int): List<Int> {
    val result = mutableListOf<Int>()

    if (totalPages <= 7) {
        // Si hay 7 o menos páginas, mostrar todas
        for (i in 1..totalPages) {
            result.add(i)
        }
    } else {
        // Siempre mostrar la primera página
        result.add(1)

        if (currentPage > 3) {
            // Añadir puntos suspensivos después de la primera página
            result.add(-1)
        }

        // Determinar el rango de páginas alrededor de la página actual
        val rangeStart = maxOf(2, currentPage - 1)
        val rangeEnd = minOf(totalPages - 1, currentPage + 1)

        for (i in rangeStart..rangeEnd) {
            result.add(i)
        }

        if (currentPage < totalPages - 2) {
            // Añadir puntos suspensivos antes de la última página
            result.add(-1)
        }

        // Siempre mostrar la última página
        result.add(totalPages)
    }

    return result
}
