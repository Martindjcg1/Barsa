package com.example.barsa.Body.Inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun InventoryItemsList(
    category: InventoryCategory,
    onBackClick: () -> Unit,
    onItemClick: (InventoryItem) -> Unit = {}
) {
    // Obtener todos los items y filtrarlos según la categoría seleccionada
    var allItems by remember { mutableStateOf(getAllInventoryItems()) }
    var filteredItems by remember {
        mutableStateOf(
            if (category.name == "Todo") {
                allItems
            } else {
                allItems.filter { categorizarMaterial(it.descripcion) == category.name }
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    // Variables para la paginación
    val itemsPerPage = 5
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = ceil(filteredItems.size.toFloat() / itemsPerPage).toInt().coerceAtLeast(1)

    // Calcular los elementos a mostrar en la página actual
    val startIndex = (currentPage - 1) * itemsPerPage
    val endIndex = min(startIndex + itemsPerPage, filteredItems.size)
    val currentPageItems = filteredItems.subList(startIndex, endIndex)

    // Función para cambiar de página
    fun changePage(page: Int) {
        currentPage = page.coerceIn(1, totalPages)
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
            onValueChange = {
                searchQuery = it
                // Filtrar items basados en la búsqueda
                filteredItems = if (category.name == "Todo") {
                    allItems.filter { item ->
                        item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                item.codigoMat.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    allItems.filter { item ->
                        categorizarMaterial(item.descripcion) == category.name &&
                                (item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                        item.codigoMat.contains(searchQuery, ignoreCase = true))
                    }
                }
                // Resetear a la primera página cuando se realiza una búsqueda
                currentPage = 1
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            placeholder = { Text("Buscar en ${category.name}...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Información de paginación
        Text(
            text = "Mostrando ${startIndex + 1} a ${endIndex} de ${filteredItems.size} items",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Grid de items
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 250.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(currentPageItems) { item ->
                InventoryItemCard(
                    item = item,
                    onClick = {
                        selectedItem = item
                        onItemClick(item)
                    }
                )
            }
        }

        // Controles de paginación
        PaginationControls(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = { changePage(it) }
        )

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
    onPageChange: (Int) -> Unit
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
                enabled = currentPage > 1
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Página anterior",
                    tint = if (currentPage > 1) MaterialTheme.colorScheme.primary else Color.Gray
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
                        modifier = Modifier
                            .size(40.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (page == currentPage) MaterialTheme.colorScheme.primary else Color.Transparent,
                        onClick = { onPageChange(page) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = page.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                                color = if (page == currentPage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Botón para ir a la página siguiente
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Página siguiente",
                    tint = if (currentPage < totalPages) MaterialTheme.colorScheme.primary else Color.Gray
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
