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
fun DeleteInventoryScreen(
    category: InventoryCategory,
    onCancel: () -> Unit,
    inventoryViewModel: InventoryViewModel // Agregar el ViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var hasLoadedInitialData by remember { mutableStateOf(false) }

    // Observar estados del ViewModel - IGUAL QUE InventoryItemsList
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val searchState by inventoryViewModel.searchState.collectAsState()
    val deleteMaterialState by inventoryViewModel.deleteMaterialState.collectAsState()

    // Estado derivado más estable - IGUAL QUE InventoryItemsList
    val stableCurrentState by remember {
        derivedStateOf {
            if (isSearching) searchState else inventoryState
        }
    }

    // Items con validación - IGUAL QUE InventoryItemsList
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
            // Filtrar items usando la función isValid() del modelo actualizado
            items.filter { item ->
                try {
                    item.isValid()
                } catch (e: Exception) {
                    Log.e("DeleteInventoryScreen", "Error validando item: ${e.message}")
                    false
                }
            }
        }
    }

    // Función para obtener el filtro de categoría - IGUAL QUE InventoryItemsList
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

    // Función para cargar datos - IGUAL QUE InventoryItemsList
    val loadCategoryData = remember {
        { page: Int ->
            val categoryFilter = getCategoryFilter()
            inventoryViewModel.getInventoryItems(
                page = maxOf(1, page),
                descripcion = categoryFilter
            )
        }
    }

    // Debounced search - IGUAL QUE InventoryItemsList
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().isNotBlank()) {
            delay(800) // Debounce de 300ms
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

    // Cargar datos iniciales - IGUAL QUE InventoryItemsList
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

    // Manejar el resultado de la eliminación
    LaunchedEffect(deleteMaterialState) {
        when (deleteMaterialState) {
            is InventoryViewModel.DeleteMaterialState.Success -> {
                successMessage = (deleteMaterialState as InventoryViewModel.DeleteMaterialState.Success).response.body.message
                showSuccessDialog = true
                showDeleteDialog = false
                itemToDelete = null
                // Recargar datos después de eliminar
                if (isSearching && searchQuery.isNotBlank()) {
                    inventoryViewModel.searchInventoryItems(
                        query = searchQuery.trim().takeIf { it.isNotBlank() },
                        page = 1
                    )
                } else {
                    loadCategoryData(1)
                }
                inventoryViewModel.resetDeleteMaterialState()
            }
            is InventoryViewModel.DeleteMaterialState.Error -> {
                errorMessage = (deleteMaterialState as InventoryViewModel.DeleteMaterialState.Error).message
                showErrorDialog = true
                showDeleteDialog = false
            }
            else -> { /* No hacer nada */ }
        }
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
                    text = "Eliminar Inventario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
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

        // Advertencia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Advertencia",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡Cuidado! La eliminación de items es permanente y no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Barra de búsqueda - IGUAL QUE InventoryItemsList
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                // El LaunchedEffect se encarga del debouncing
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
            singleLine = true,
            enabled = stableCurrentState !is InventoryViewModel.InventoryState.Loading &&
                    stableCurrentState !is InventoryViewModel.SearchState.Loading &&
                    deleteMaterialState !is InventoryViewModel.DeleteMaterialState.Loading
        )

        // Indicador de búsqueda - IGUAL QUE InventoryItemsList
        if (isSearching && searchQuery.isNotBlank()) {
            Text(
                text = "Buscando: \"$searchQuery\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Mostrar contador de items
        Text(
            text = "${stableCurrentItems.size} item(s) encontrado(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Contenido principal - IGUAL QUE InventoryItemsList
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = stableCurrentItems.size,
                            key = { index ->
                                // Key segura usando propiedades seguras
                                if (index >= 0 && index < stableCurrentItems.size) {
                                    val item = stableCurrentItems[index]
                                    "${item.getSummary()}_delete_$index"
                                } else {
                                    "delete_item_$index"
                                }
                            }
                        ) { index ->
                            if (index >= 0 && index < stableCurrentItems.size) {
                                val item = stableCurrentItems[index]
                                DeletableItemCard(
                                    item = item,
                                    onDeleteClick = {
                                        itemToDelete = item
                                        showDeleteDialog = true
                                    },
                                    isDeleting = deleteMaterialState is InventoryViewModel.DeleteMaterialState.Loading,
                                    searchQuery = searchQuery.trim()
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                LoadingContent(isSearching = false)
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (deleteMaterialState !is InventoryViewModel.DeleteMaterialState.Loading) {
                    showDeleteDialog = false
                    itemToDelete = null
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar eliminación")
                }
            },
            text = {
                Column {
                    Text("¿Está seguro que desea eliminar el siguiente item?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Código: ${itemToDelete?.codigoMatSafe ?: "N/A"}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Descripción: ${itemToDelete?.descripcionSafe ?: "N/A"}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta acción no se puede deshacer.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { item ->
                            inventoryViewModel.deleteMaterial(item.codigoMatSafe)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteMaterialState !is InventoryViewModel.DeleteMaterialState.Loading
                ) {
                    if (deleteMaterialState is InventoryViewModel.DeleteMaterialState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminando...")
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    enabled = deleteMaterialState !is InventoryViewModel.DeleteMaterialState.Loading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Material Eliminado")
                }
            },
            text = { Text(successMessage) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Error al eliminar")
                }
            },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun DeletableItemCard(
    item: InventoryItem,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean = false,
    searchQuery: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleting)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
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
                        imageVector = Icons.Default.AddCircle,
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
                // Código con highlighting si hay búsqueda
                Text(
                    text = item.codigoMatSafe,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeleting) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else if (searchQuery.isNotEmpty() && item.codigoMatSafe.contains(searchQuery, ignoreCase = true)) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // Descripción con highlighting si hay búsqueda
                Text(
                    text = item.descripcionSafe,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDeleting) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else if (searchQuery.isNotEmpty() && item.descripcionSafe.contains(searchQuery, ignoreCase = true)) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de stock con color
                    val stockColor = when {
                        item.existenciaSafe >= item.maxSafe -> Color(0xFF4CAF50)
                        item.existenciaSafe >= item.minSafe -> Color(0xFFFF9800)
                        item.existenciaSafe > 0 -> Color(0xFFFF5722)
                        else -> Color(0xFFF44336)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isDeleting) stockColor.copy(alpha = 0.6f) else stockColor,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stock: ${item.existenciaSafe.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDeleting) stockColor.copy(alpha = 0.6f) else stockColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Precio: $${String.format("%.2f", item.pcompraSafe)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDeleting)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = if (isDeleting) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        } else if (searchQuery.isNotEmpty() && item.unidadSafe.contains(searchQuery, ignoreCase = true)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        },
                        modifier = Modifier
                            .background(
                                if (isDeleting)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                else
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
                            color = if (isDeleting) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else if (searchQuery.isNotEmpty() && item.procesoSafe.contains(searchQuery, ignoreCase = true)) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Botón de eliminar
            IconButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    }
}

