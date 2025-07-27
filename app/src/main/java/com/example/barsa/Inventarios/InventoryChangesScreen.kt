package com.example.barsa.Inventarios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.barsa.Models.*
import com.example.barsa.data.retrofit.models.InventoryMovementDetail
import com.example.barsa.data.retrofit.models.InventoryMovementHeader


import com.example.barsa.data.retrofit.ui.InventoryViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryChangesScreen(
    inventoryViewModel: InventoryViewModel,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateStart by remember { mutableStateOf<String?>(null) }
    var selectedDateEnd by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var hasLoadedInitialData by remember { mutableStateOf(false) }
    var showDebugInfo by remember { mutableStateOf(false) }

    // NUEVOS ESTADOS PARA EL DIÁLOGO DE DETALLES
    var showMovementDetailDialog by remember { mutableStateOf(false) }
    var selectedMovementForDetail by remember { mutableStateOf<InventoryMovementHeader?>(null) }

    // Estado del ViewModel
    val inventoryMovementsState by inventoryViewModel.inventoryMovementsState.collectAsState() // Usando collectAsState para consistencia

    // Datos estables - evita recomposiciones innecesarias
    val stableMovementsData by remember {
        derivedStateOf {
            when (inventoryMovementsState) {
                is InventoryViewModel.InventoryMovementsState.Success -> {
                    (inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Success).response.data.filterNotNull()
                }
                else -> emptyList()
            }
        }
    }

    // Información de paginación estable
    val stablePagination by remember {
        derivedStateOf {
            when (inventoryMovementsState) {
                is InventoryViewModel.InventoryMovementsState.Success -> {
                    Triple(
                        maxOf(1, (inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Success).response.currentPage),
                        maxOf(1, (inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Success).response.totalPages),
                        maxOf(0, (inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Success).response.totalItems)
                    )
                }
                else -> Triple(1, 1, 0)
            }
        }
    }

    // Función para aplicar filtros
    val applyFilters = remember {
        { page: Int ->
            try {
                // CAMBIO: searchQuery solo aplica a folio
                val folioQuery = searchQuery.trim().takeIf { it.isNotBlank() }
                inventoryViewModel.getInventoryMovements(
                    page = maxOf(1, page),
                    limit = 10,
                    fechaInicio = selectedDateStart,
                    fechaFin = selectedDateEnd,
                    folio = folioQuery,
                    usuario = null, // Se elimina la búsqueda por usuario con searchQuery
                    notes = null,
                    descripcion = null,
                    codigoMat = null
                )
            } catch (e: Exception) {
                println("Error en applyFilters: ${e.message}")
            }
        }
    }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        if (!hasLoadedInitialData) {
            inventoryViewModel.resetInventoryMovementsState()
            delay(50)
            applyFilters(1)
            hasLoadedInitialData = true
        }
    }

    // Debounced search
    LaunchedEffect(searchQuery, selectedDateStart, selectedDateEnd) {
        if (hasLoadedInitialData) {
            delay(500)
            applyFilters(1)
        }
    }

    // Función para filtrar movimientos localmente
    val getFilteredMovements = remember {
        { movements: List<InventoryMovementHeader> ->
            try {
                val filteredByType = when (selectedFilter) {
                    "Solo Entradas" -> movements.filter { it.isEntry }
                    "Solo Salidas" -> movements.filter { it.isExit }
                    "Devolución Cliente" -> movements.filter { it.movIdSafe == 1 }
                    "Devolución Proveedor" -> movements.filter { it.movIdSafe == 2 }
                    "Devolución Almacén" -> movements.filter { it.movIdSafe == 3 }
                    "Entrada Almacén" -> movements.filter { it.movIdSafe == 4 }
                    "Salida Almacén" -> movements.filter { it.movIdSafe == 5 }
                    else -> movements
                }
                // Ordenar por fecha (más recientes primero)
                filteredByType.sortedByDescending { movement ->
                    parseDate(movement.fechaSafe)?.time ?: 0L
                }
            } catch (e: Exception) {
                println("Error en getFilteredMovements: ${e.message}")
                emptyList()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con botón de regreso
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Historial",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            // Botón de debug info
            IconButton(
                onClick = { showDebugInfo = !showDebugInfo }
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Debug Info",
                    tint = if (showDebugInfo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
            },
            label = { Text("Buscar por folio...") }, // CAMBIO: Etiqueta de búsqueda
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Filtros de fecha y tipo
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filtro de fecha inicio
            item {
                FilterChip(
                    onClick = { showStartDatePicker = true },
                    label = {
                        Text(
                            text = selectedDateStart?.let { "Desde: $it" } ?: "Fecha inicio",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    selected = selectedDateStart != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Fecha inicio",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = if (selectedDateStart != null) {
                        {
                            IconButton(
                                onClick = { selectedDateStart = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else null
                )
            }
            // Filtro de fecha fin
            item {
                FilterChip(
                    onClick = { showEndDatePicker = true },
                    label = {
                        Text(
                            text = selectedDateEnd?.let { "Hasta: $it" } ?: "Fecha fin",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    selected = selectedDateEnd != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Fecha fin",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = if (selectedDateEnd != null) {
                        {
                            IconButton(
                                onClick = { selectedDateEnd = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else null
                )
            }
            // Filtros de tipo
            items(listOf("Todos", "Solo Entradas", "Solo Salidas", "Devolución Cliente", "Devolución Proveedor", "Devolución Almacén", "Entrada Almacén", "Salida Almacén")) { filter ->
                FilterChip(
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    selected = selectedFilter == filter
                )
            }
            // Botón de actualizar
            item {
                IconButton(
                    onClick = { applyFilters(1) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Debug info
        if (showDebugInfo) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "🔍 Debug Info:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Movements State: ${inventoryMovementsState::class.simpleName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Headers cargados: ${stableMovementsData.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val stats = inventoryViewModel.getMovementsStats()
                    Text(
                        text = "Entradas: ${stats["entradas"]}, Salidas: ${stats["salidas"]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (inventoryMovementsState is InventoryViewModel.InventoryMovementsState.Error) {
                        Text(
                            text = "❌ Error: ${(inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Error).message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Indicador de búsqueda
        if (searchQuery.trim().isNotBlank()) {
            Text(
                text = "Buscando: \"${searchQuery.trim()}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Información de paginación
        if (stablePagination.third > 0) {
            Text(
                text = "Página ${stablePagination.first} de ${stablePagination.second} - Total: ${stablePagination.third} movimientos",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Contenido principal
        when {
            // Estado de carga
            inventoryMovementsState is InventoryViewModel.InventoryMovementsState.Loading -> {
                LoadingContent(isSearching = searchQuery.trim().isNotBlank())
            }
            // Estado de error
            inventoryMovementsState is InventoryViewModel.InventoryMovementsState.Error -> {
                ErrorContent(
                    message = (inventoryMovementsState as InventoryViewModel.InventoryMovementsState.Error).message,
                    onRetry = { applyFilters(1) }
                )
            }
            // Estado de éxito
            else -> {
                val filteredMovements = getFilteredMovements(stableMovementsData)
                if (filteredMovements.isEmpty()) {
                    EmptyContent(
                        isSearching = searchQuery.trim().isNotBlank(),
                        searchQuery = searchQuery.trim(),
                        selectedFilter = selectedFilter
                    )
                } else {
                    // Lista de movimientos
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredMovements,
                            key = { movement ->
                                "header_${movement.consecutivoSafe}_${movement.movIdSafe}"
                            }
                        ) { movement ->
                            MovementHeaderCard(
                                movement = movement,
                                onClick = {
                                    selectedMovementForDetail = movement
                                    showMovementDetailDialog = true
                                }
                            )
                        }
                    }
                    // Controles de paginación
                    if (stablePagination.second > 1) {
                        PaginationControls(
                            currentPage = stablePagination.first,
                            totalPages = stablePagination.second,
                            onPageChange = { newPage -> applyFilters(newPage) },
                            isLoading = inventoryMovementsState is InventoryViewModel.InventoryMovementsState.Loading
                        )
                    }
                }
            }
        }
    }

    // DatePickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDateStart = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDateEnd = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // DIÁLOGO DE DETALLES DEL MOVIMIENTO
    if (showMovementDetailDialog && selectedMovementForDetail != null) {
        MovementDetailDialog(
            movement = selectedMovementForDetail!!,
            onDismiss = {
                showMovementDetailDialog = false
                selectedMovementForDetail = null
            }
        )
    }
}

// ==================== COMPOSABLES MODIFICADOS Y NUEVOS ====================
@Composable
fun MovementHeaderCard(movement: InventoryMovementHeader, onClick: () -> Unit) {
    val isEntry = movement.isEntry
    val containerColor = if (isEntry) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    }
    val borderColor = if (isEntry) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
    }
    val iconColor = if (isEntry) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // HACEMOS LA TARJETA CLICKEABLE
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con tipo y consecutivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movement.iconoMovimiento,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = movement.tipoMovimiento,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                Text(
                    text = "Consecutivo: ${movement.consecutivoSafe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Información principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movement.descripcionInventarioSafe,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Usuario: ${movement.usuarioSafe}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Folio: ${movement.folioSafe}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = movement.fechaFormateada,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Observación (si existe)
            if (movement.tieneObservacion) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${movement.observacionSafe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Indicador de procesado
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (movement.procesadaSafe) Icons.Default.CheckCircle else Icons.Default.Refresh,
                    contentDescription = null,
                    tint = if (movement.procesadaSafe) Color.Green else Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (movement.procesadaSafe) "Procesada" else "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (movement.procesadaSafe) Color.Green else Color.Yellow
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementDetailDialog(
    movement: InventoryMovementHeader,
    onDismiss: () -> Unit
) {
    val isEntry = movement.isEntry
    val iconColor = if (isEntry) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val scrollState = rememberScrollState() // Estado para el scroll
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Color de fondo más claro
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState) // Habilitar scroll vertical
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles del Movimiento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Folio y Fecha (como en la imagen, pero separados para claridad)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Folio:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = movement.folioSafe.toString(), // Convertir a String
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fecha:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = movement.fechaFormateada,
                            style = MaterialTheme.typography.bodyMedium, // CAMBIO: Tamaño de fuente más pequeño
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // Tipo de Movimiento (grande y con color)
                Text(
                    text = movement.tipoMovimiento,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Consecutivo: ${movement.consecutivoSafe}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // Más detalles del encabezado
                InfoRow(label = "Registrado por:", value = movement.usuarioSafe)
                if (movement.tieneObservacion) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Observaciones:", value = movement.observacionSafe)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (movement.procesadaSafe) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (movement.procesadaSafe) Color.Green else Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (movement.procesadaSafe) "Procesada" else "Pendiente",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (movement.procesadaSafe) Color.Green else Color.Yellow,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // Detalles de los productos
                Text(
                    text = "Productos (${movement.detallesSafe.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (movement.detallesSafe.isEmpty()) {
                    Text(
                        text = "No hay detalles de productos para este movimiento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        movement.detallesSafe.forEach { detail ->
                            MovementDetailItem(detail = detail)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                // Totales
                InfoRow(
                    label = "Cantidad Total:",
                    value = movement.cantidadTotal.toString(),
                    valueColor = iconColor,
                    valueFontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "Valor Total:",
                    value = movement.valorTotalFormateado,
                    valueColor = iconColor,
                    valueFontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, valueFontWeight: FontWeight = FontWeight.Normal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = valueFontWeight
        )
    }
}

@Composable
fun MovementDetailItem(detail: InventoryMovementDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = detail.codigoMatSafe,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${detail.idSafe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail.descripcionSafe,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cantidad: ${detail.cantidadFormateada}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Existencia anterior: ${detail.existenciaAnteriorFormateada}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = detail.valorTotalFormateado,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Precio: ${detail.pcostoFormateado}", // ← Usa la propiedad formateada
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (detail.procesadaSafe) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ Procesado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Resto de composables auxiliares (LoadingContent, ErrorContent, EmptyContent, etc.)
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
                text = if (isSearching) "Buscando movimientos..." else "Cargando movimientos...",
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
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error al cargar movimientos",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun EmptyContent(
    isSearching: Boolean,
    searchQuery: String,
    selectedFilter: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearching) {
                    "No se encontraron resultados para \"$searchQuery\""
                } else {
                    when (selectedFilter) {
                        "Solo Entradas" -> "No hay entradas registradas"
                        "Solo Salidas" -> "No hay salidas registradas"
                        else -> "No hay movimientos registrados"
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearching) {
                    "Intenta con otros términos de búsqueda"
                } else {
                    "Los movimientos de inventario aparecerán aquí cuando se registren"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.height(32.dp)
    )
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
            .padding(16.dp),
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
        println("Error generando secuencia de paginación: ${e.message}")
        result.clear()
        result.add(validCurrentPage)
    }
    return result.distinct()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Añadir 24 horas (un día) en milisegundos
                        val adjustedMillis = millis + (24 * 60 * 60 * 1000L)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = Date(adjustedMillis)
                        onDateSelected(formatter.format(date))
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Función auxiliar para parsear fechas
private fun parseDate(dateString: String): Date? {
    return try {
        val formats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSSSSS"
        )
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(dateString)
            } catch (e: Exception) {
                continue
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}
