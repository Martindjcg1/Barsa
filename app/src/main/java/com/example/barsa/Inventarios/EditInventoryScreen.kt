package com.example.barsa.Body.Inventory

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInventoryScreen(
    category: InventoryCategory,
    onCancel: () -> Unit,
    onItemSelected: (InventoryItem) -> Unit,
    inventoryViewModel: InventoryViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var hasLoadedInitialData by remember { mutableStateOf(false) }

    // Estados del ViewModel - IGUAL que en InventoryItemsList y DeleteInventoryScreen
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val searchState by inventoryViewModel.searchState.collectAsState()

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
                    Log.e("EditInventoryScreen", "Error validando item: ${e.message}")
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
                "Herramientas" -> "herramienta"
                "Bisagras y Herrajes" -> "bisagra"
                "Pernos y Sujetadores" -> "perno"
                "Cintas y Adhesivos" -> "cinta"
                "Separadores y Accesorios de Cristal" -> "cristal"
                "Cubrecantos y Acabados" -> "cubrecanto"
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
                limit = 100,
                descripcion = categoryFilter
            )
        }
    }

    // Debounced search mejorado con manejo seguro de nulls - AUMENTADO A 800ms
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().isNotBlank()) {
            delay(800) // Debounce de 800ms para que el usuario termine de escribir
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Editar Inventario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancelar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
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
                .padding(bottom = 16.dp),
            placeholder = { Text("Buscar por código o descripción...") },
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
            singleLine = true,
            enabled = stableCurrentState !is InventoryViewModel.InventoryState.Loading &&
                    stableCurrentState !is InventoryViewModel.SearchState.Loading
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

        // Mostrar contador de items
        if (stablePaginationInfo.third > 0) {
            Text(
                text = "${stableCurrentItems.size} item(s) encontrado(s) - Total: ${stablePaginationInfo.third}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Contenido principal con mejor manejo de estados
        when (stableCurrentState) {
            is InventoryViewModel.InventoryState.Loading,
            is InventoryViewModel.SearchState.Loading -> {
                LoadingContentEdit(isSearching = isSearching)
            }
            is InventoryViewModel.InventoryState.Error -> {
                ErrorContentEdit(
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
                ErrorContentEdit(
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
                    EmptyContentEdit(
                        isSearching = isSearching,
                        searchQuery = searchQuery
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = stableCurrentItems.size,
                            key = { index ->
                                // Key más robusta usando propiedades seguras
                                when {
                                    index >= 0 && index < stableCurrentItems.size -> {
                                        val item = stableCurrentItems[index]
                                        try {
                                            "${item.getSummary()}_edit_$index"
                                        } catch (e: Exception) {
                                            "fallback_edit_item_$index"
                                        }
                                    }
                                    else -> "fallback_edit_item_$index"
                                }
                            }
                        ) { index ->
                            // Triple validación antes de renderizar
                            if (index >= 0 &&
                                index < stableCurrentItems.size &&
                                stableCurrentItems.isNotEmpty()) {
                                val item = stableCurrentItems.getOrNull(index)
                                if (item != null && item.isValid()) {
                                    EditableItemCard(
                                        item = item,
                                        onClick = { onItemSelected(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                LoadingContentEdit(isSearching = false)
            }
        }
    }
}

@Composable
fun LoadingContentEdit(isSearching: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearching) "Buscando..." else "Cargando inventario...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorContentEdit(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error al cargar el inventario",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun EmptyContentEdit(
    isSearching: Boolean,
    searchQuery: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                    "No se encontraron items que coincidan con \"$searchQuery\""
                } else {
                    "No se encontraron items en esta categoría"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            if (isSearching) {
                Text(
                    text = "Intenta con otros términos de búsqueda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EditableItemCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostrar imagen si existe
            if (item.imagenes.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imagenes.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = item.descripcionSafe,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                // Placeholder cuando no hay imagen
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sin imagen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Información del item
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.codigoMatSafe,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.descripcionSafe,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de stock con color
                    val stockColor = when {
                        item.existenciaSafe >= item.maxSafe -> Color(0xFF4CAF50) // Verde
                        item.existenciaSafe >= item.minSafe -> Color(0xFFFF9800) // Naranja
                        item.existenciaSafe > 0 -> Color(0xFFFF5722) // Rojo claro
                        else -> Color(0xFFF44336) // Rojo
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(stockColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stock: ${item.existenciaSafe.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = stockColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Precio: $${String.format("%.2f", item.pcompraSafe)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Segunda fila de información
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.unidadSafe,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    if (item.procesoSafe.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Proceso: ${item.procesoSafe}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Información de rango de stock
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Min: ${item.minSafe.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Max: ${item.maxSafe.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Icono de editar con fondo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
