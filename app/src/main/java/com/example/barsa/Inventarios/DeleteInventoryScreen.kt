package com.example.barsa.Body.Inventory

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

    // Observar estados del ViewModel
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val deleteMaterialState by inventoryViewModel.deleteMaterialState.collectAsState()

    // Cargar datos cuando se abre la pantalla
    LaunchedEffect(category) {
        val categoryFilter = when (category.name) {
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
        inventoryViewModel.getInventoryItems(page = 1, limit = 100, descripcion = categoryFilter)
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
                val categoryFilter = when (category.name) {
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
                inventoryViewModel.getInventoryItems(page = 1, limit = 100, descripcion = categoryFilter)
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

    // Obtener items y filtrarlos
    val allItems = when (inventoryState) {
        is InventoryViewModel.InventoryState.Success -> (inventoryState as InventoryViewModel.InventoryState.Success).response.data
        else -> emptyList()
    }

    val filteredItems = remember(allItems, searchQuery, category) {
        if (searchQuery.isEmpty()) {
            if (category.name == "Todo") {
                allItems
            } else {
                allItems.filter { categorizarMaterial(it.descripcion) == category.name }
            }
        } else {
            val baseItems = if (category.name == "Todo") {
                allItems
            } else {
                allItems.filter { categorizarMaterial(it.descripcion) == category.name }
            }
            baseItems.filter { item ->
                item.descripcion.contains(searchQuery, ignoreCase = true) ||
                        item.codigoMat.contains(searchQuery, ignoreCase = true)
            }
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

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Buscar por código o descripción...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            singleLine = true,
            enabled = inventoryState !is InventoryViewModel.InventoryState.Loading &&
                    deleteMaterialState !is InventoryViewModel.DeleteMaterialState.Loading
        )

        // Mostrar contador de items
        if (inventoryState is InventoryViewModel.InventoryState.Success) {
            Text(
                text = "${filteredItems.size} item(s) encontrado(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Contenido principal
        when (inventoryState) {
            is InventoryViewModel.InventoryState.Loading -> {
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
                            text = "Cargando inventario...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is InventoryViewModel.InventoryState.Error -> {
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
                            text = (inventoryState as InventoryViewModel.InventoryState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val categoryFilter = when (category.name) {
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
                                inventoryViewModel.getInventoryItems(page = 1, limit = 100, descripcion = categoryFilter)
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }

            is InventoryViewModel.InventoryState.Success -> {
                if (filteredItems.isEmpty()) {
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
                                text = if (searchQuery.isEmpty()) {
                                    "No se encontraron items en esta categoría"
                                } else {
                                    "No se encontraron items que coincidan con \"$searchQuery\""
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems) { item ->
                            DeletableItemCard(
                                item = item,
                                onDeleteClick = {
                                    itemToDelete = item
                                    showDeleteDialog = true
                                },
                                isDeleting = deleteMaterialState is InventoryViewModel.DeleteMaterialState.Loading
                            )
                        }
                    }
                }
            }

            else -> {
                // Estado inicial
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
                        text = "Código: ${itemToDelete?.codigoMat}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Descripción: ${itemToDelete?.descripcion}",
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
                            inventoryViewModel.deleteMaterial(item.codigoMat)
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
    isDeleting: Boolean = false
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
                    contentDescription = item.descripcion,
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
                Text(
                    text = item.codigoMat,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeleting)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDeleting)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de stock con color
                    val stockColor = when {
                        item.existencia >= item.max -> Color(0xFF4CAF50)
                        item.existencia >= item.min -> Color(0xFFFF9800)
                        item.existencia > 0 -> Color(0xFFFF5722)
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
                        text = "Stock: ${item.existencia.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDeleting) stockColor.copy(alpha = 0.6f) else stockColor
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Precio: $${String.format("%.2f", item.pcompra)}",
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
                        text = item.unidad,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDeleting)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.primary,
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

                    if (item.proceso.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Proceso: ${item.proceso}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDeleting)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
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

