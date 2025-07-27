package com.example.barsa.Body.Inventory

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.barsa.Models.*
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.models.InventoryMovementHeader
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import com.example.barsa.data.retrofit.ui.UserViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryExitsScreen(
    onBackClick: () -> Unit,
    inventoryViewModel: InventoryViewModel,
    userViewModel: UserViewModel
) {
    var showAddExitDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // NUEVO: Estados para diálogos de confirmación
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Estados del ViewModel
    val movementsState by inventoryViewModel.inventoryMovementsState.collectAsState()
    val createMovementState by inventoryViewModel.createMovementState.collectAsState()
    val inventoryState by inventoryViewModel.inventoryState.collectAsState()
    val infoUsuarioResult by userViewModel.infoUsuarioResult.collectAsState()

    // NUEVO: Estado para la paginación
    var hasLoadedInitialData by remember { mutableStateOf(false) }

    // Información de paginación estable
    val stablePagination by remember {
        derivedStateOf {
            when (movementsState) {
                is InventoryViewModel.InventoryMovementsState.Success -> {
                    Triple(
                        maxOf(1, (movementsState as InventoryViewModel.InventoryMovementsState.Success).response.currentPage),
                        maxOf(1, (movementsState as InventoryViewModel.InventoryMovementsState.Success).response.totalPages),
                        maxOf(0, (movementsState as InventoryViewModel.InventoryMovementsState.Success).response.totalItems)
                    )
                }
                else -> Triple(1, 1, 0)
            }
        }
    }

    // Función para aplicar filtros y paginación
    val applyFilters = remember {
        { page: Int ->
            try {
                val folioQuery = searchQuery.trim().takeIf { it.isNotBlank() }
                inventoryViewModel.getInventoryMovements(
                    page = maxOf(1, page),
                    limit = 5, // CAMBIO: Límite de 5 elementos por página
                    folio = folioQuery,
                    notes = null,
                    usuario = null,
                    codigoMat = null,
                    descripcion = null,
                    fechaInicio = null,
                    fechaFin = null
                )
            } catch (e: Exception) {
                println("Error en applyFilters (Exits): ${e.message}")
            }
        }
    }

    // Obtener información del usuario y cargar datos iniciales
    LaunchedEffect(Unit) {
        userViewModel.obtenerInfoUsuarioPersonal()
        if (!hasLoadedInitialData) {
            inventoryViewModel.resetInventoryMovementsState()
            delay(50)
            applyFilters(1) // Cargar la primera página
            hasLoadedInitialData = true
        }
        inventoryViewModel.getInventoryItems(page = 1, limit = 1000) // Para tener todos los items disponibles
    }

    // Debounced search y recarga al cambiar filtros
    LaunchedEffect(searchQuery) {
        if (hasLoadedInitialData) {
            delay(500) // Pequeño retardo para evitar llamadas excesivas
            applyFilters(1) // Reiniciar a la primera página al cambiar la búsqueda
        }
    }

    // ✅ LAUNCHEDEFFECT MEJORADO CON MANEJO ESPECÍFICO DE ERRORES
    LaunchedEffect(createMovementState) {
        when (createMovementState) {
            is InventoryViewModel.CreateMovementState.Success -> {
                Log.d("InventoryExitsScreen", "✅ RESPUESTA EXITOSA RECIBIDA")
                successMessage = "Salida registrada exitosamente"
                showSuccessDialog = true
                delay(500)
                Log.d("InventoryExitsScreen", "🔄 Iniciando recarga de movimientos...")
                inventoryViewModel.resetInventoryMovementsState()
                applyFilters(stablePagination.first) // Recargar la página actual
                delay(1500) // Pequeña pausa para asegurar la recarga
                applyFilters(stablePagination.first) // Asegurar recarga
                Log.d("InventoryExitsScreen", "✅ Recarga completada después de éxito")
                inventoryViewModel.resetCreateMovementState()
            }
            is InventoryViewModel.CreateMovementState.Error -> {
                val error = createMovementState as InventoryViewModel.CreateMovementState.Error
                Log.e("InventoryExitsScreen", "❌ RESPUESTA DE ERROR RECIBIDA: ${error.message}")
                // ✅ MANEJO ESPECÍFICO DEL ERROR DE FOLIO NO EXISTENTE Y STOCK INSUFICIENTE
                errorMessage = when {
                    error.message.contains("folio de papeleta no existe", ignoreCase = true) -> {
                        "❌ Folio Inexistente\n\nEl folio de papeleta ingresado no existe en el sistema.\n\nPor favor:\n• Verifica el número de folio\n• Consulta con el administrador si es necesario"
                    }
                    error.message.contains("Stock insuficiente", ignoreCase = true) -> {
                        "❌ Stock Insuficiente\n\n${error.message}\n\nPor favor, ajusta las cantidades o verifica el inventario."
                    }
                    else -> {
                        "❌ Error\n\n${error.message}\n\nSi el problema persiste, contacta al administrador."
                    }
                }
                showErrorDialog = true
                inventoryViewModel.resetCreateMovementState()
            }
            is InventoryViewModel.CreateMovementState.Loading -> {
                Log.d("InventoryExitsScreen", "⏳ Estado de carga activo...")
            }
            is InventoryViewModel.CreateMovementState.Initial -> {
                // Estado inicial, no hacer nada
            }
        }
    }

    // Tipos de movimiento para salidas (solo 2, 5)
    val tiposMovimientoSalida = remember {
        listOf(
            MovimientoInventario(2, "DEVOLUCION A PROVEEDOR", false),
            MovimientoInventario(5, "SALIDA DE ALMACEN", false)
        )
    }

    // Filtrar solo movimientos de salida (localmente, después de la paginación de la API)
    val exitMovements = remember(movementsState, searchQuery) {
        when (movementsState) {
            is InventoryViewModel.InventoryMovementsState.Success -> {
                (movementsState as InventoryViewModel.InventoryMovementsState.Success).response.data.filter { movement ->
                    movement.movIdSafe in listOf(2, 5)
                }.filter { movement ->
                    if (searchQuery.isBlank()) true
                    else {
                        (movement.folio?.toString()?.contains(searchQuery, ignoreCase = true) == true) ||
                                (movement.fecha?.contains(searchQuery, ignoreCase = true) == true) ||
                                (movement.usuario?.contains(searchQuery, ignoreCase = true) == true) ||
                                (movement.observacion?.contains(searchQuery, ignoreCase = true) == true) ||
                                (movement.consecutivo?.toString()?.contains(searchQuery, ignoreCase = true) == true)
                    }
                }
            }
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra superior con botón de regreso y título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text(
                text = "Registro de Salidas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Barra de búsqueda y botón para agregar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar salidas por folio...") }, // CAMBIO: Etiqueta de búsqueda
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { showAddExitDialog = true },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva Salida")
            }
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

        // Estado de carga
        when (movementsState) {
            is InventoryViewModel.InventoryMovementsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is InventoryViewModel.InventoryMovementsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            text = "Error al cargar movimientos",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = (movementsState as InventoryViewModel.InventoryMovementsState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                applyFilters(stablePagination.first) // Reintentar la página actual
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            else -> {
                // Lista de salidas
                if (exitMovements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay salidas registradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Haz clic en 'Nueva Salida' para registrar una salida de inventario",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(exitMovements) { movement ->
                            ExitMovementCard(
                                movement = movement,
                                tiposMovimiento = tiposMovimientoSalida
                            )
                        }
                    }
                    // Controles de paginación
                    if (stablePagination.second > 1) {
                        PaginationControls(
                            currentPage = stablePagination.first,
                            totalPages = stablePagination.second,
                            onPageChange = { newPage -> applyFilters(newPage) },
                            isLoading = movementsState is InventoryViewModel.InventoryMovementsState.Loading
                        )
                    }
                }
            }
        }
    }

    // Diálogo para agregar una nueva salida
    if (showAddExitDialog) {
        val availableItems = when (inventoryState) {
            is InventoryViewModel.InventoryState.Success -> (inventoryState as InventoryViewModel.InventoryState.Success).response.data
            else -> emptyList()
        }
        AddExitDialog(
            onDismiss = { showAddExitDialog = false },
            onExitAdded = { folio, movId, selectedItems, observacion ->
                Log.d("InventoryExitsScreen", "🚀 Iniciando creación de movimiento de salida...")
                Log.d("InventoryExitsScreen", "   Folio: $folio")
                Log.d("InventoryExitsScreen", "   MovId: $movId")
                Log.d("InventoryExitsScreen", "   Items: ${selectedItems.size}")
                Log.d("InventoryExitsScreen", "   Observación: '$observacion'")
                val currentUser = infoUsuarioResult?.getOrNull()?.nombreUsuario ?: "Usuario"
                inventoryViewModel.createMovementFromSelectedItems(
                    folio = folio,
                    movId = movId,
                    fecha = inventoryViewModel.getCurrentDateForMovement(),
                    selectedItems = selectedItems,
                    observacion = observacion,
                    autoriza = currentUser,
                    procesada = true // Se mantiene en true para que se guarde como procesada
                )
                showAddExitDialog = false
                Log.d("InventoryExitsScreen", "✅ Solicitud de creación enviada, esperando respuesta...")
            },
            tiposMovimiento = tiposMovimientoSalida,
            availableItems = availableItems,
            isLoading = createMovementState is InventoryViewModel.CreateMovementState.Loading
        )
    }

    // NUEVO: Diálogo de éxito
    if (showSuccessDialog) {
        SuccessDialog(
            title = "¡Éxito!",
            message = successMessage,
            onDismiss = {
                showSuccessDialog = false
                successMessage = ""
                Log.d("InventoryExitsScreen", "✅ Diálogo de éxito cerrado")
            }
        )
    }

    // NUEVO: Diálogo de error
    if (showErrorDialog) {
        ErrorDialog(
            title = "Error",
            message = errorMessage,
            onDismiss = {
                showErrorDialog = false
                errorMessage = ""
                // Opcional: reabrir el diálogo de agregar entrada
                showAddExitDialog = true
                Log.d("InventoryExitsScreen", "🔄 Reintentando desde diálogo de error")
            }
        )
    }
}

@Composable
fun ExitMovementCard(
    movement: InventoryMovementHeader,
    tiposMovimiento: List<MovimientoInventario>
) {
    var expanded by remember { mutableStateOf(false) }
    val tipoMovimiento = tiposMovimiento.find { it.movId == movement.movIdSafe }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Salida #${movement.consecutivoSafe}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = movement.fechaFormateada,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tipo: ${tipoMovimiento?.descripcion ?: "Desconocido"}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Red // Color rojo para salidas
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Folio: ${movement.folioSafe}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registrado por: ${movement.usuarioSafe}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            // CORREGIDO: Verificar si existe la propiedad autoriza (si es diferente de usuario)
            // Si 'autoriza' es un campo separado en InventoryMovementHeader, usarlo.
            // Si no, y se usa 'observacion' para implicar autorización, mantener la lógica.
            // Por ahora, se mantiene la lógica de la pantalla de entradas.
            if (movement.observacionSafe.isNotEmpty()) { // Asumiendo que observacion se usa para autoriza si no hay campo directo
                Text(
                    text = "Autorizado por: ${movement.usuarioSafe}", // Usar usuario como autoriza por ahora
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos: ${movement.detallesSafe.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: ${movement.valorTotalFormateado}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red // Color rojo para el total de salidas
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Mostrar menos" else "Mostrar más"
                    )
                }
            }
            if (expanded && movement.detallesSafe.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                movement.detallesSafe.forEach { detail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(0.4f)) {
                            Text(
                                text = detail.codigoMatSafe,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detail.descripcionSafe,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "Cant: ${detail.cantidadFormateada}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )
                        Text(
                            text = detail.pcostoFormateado,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f),
                            color = Color.Red, // Color rojo para costos de salida
                            textAlign = TextAlign.End
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            if (movement.observacionSafe.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observaciones: ${movement.observacionSafe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            // Estado de procesamiento
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (movement.procesadaSafe) Icons.Default.CheckCircle else Icons.Default.Info,
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
fun AddExitDialog(
    onDismiss: () -> Unit,
    onExitAdded: (Int, Int, List<Pair<InventoryItem, Double>>, String) -> Unit,
    tiposMovimiento: List<MovimientoInventario>,
    availableItems: List<InventoryItem>,
    isLoading: Boolean = false
) {
    var folio by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf<List<Pair<InventoryItem, Double>>>(emptyList()) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedTipoMovimiento by remember { mutableStateOf(tiposMovimiento.firstOrNull()) }
    var showTipoMovimientoDropdown by remember { mutableStateOf(false) }

    // Verificar si hay productos con stock insuficiente
    val hasInsufficientStock = selectedItems.any { (item, cantidad) ->
        cantidad > (item.existencia ?: 0.0)
    }

    Dialog(onDismissRequest = if (isLoading) { {} } else onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Salida",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Fecha (automática)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                OutlinedTextField(
                    value = currentDate,
                    onValueChange = { },
                    label = { Text("Fecha") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tipo de Movimiento
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTipoMovimiento?.descripcion ?: "Seleccione un tipo",
                        onValueChange = { },
                        label = { Text("Tipo de Movimiento") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTipoMovimientoDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar tipo"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        enabled = !isLoading
                    )
                    DropdownMenu(
                        expanded = showTipoMovimientoDropdown,
                        onDismissRequest = { showTipoMovimientoDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        tiposMovimiento.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.descripcion) },
                                onClick = {
                                    selectedTipoMovimiento = tipo
                                    showTipoMovimientoDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Folio
                OutlinedTextField(
                    value = folio,
                    onValueChange = { folio = it },
                    label = { Text("Folio") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, // CAMBIO: Icono a Edit
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Lista de productos seleccionados
                Text(
                    text = "Productos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay productos seleccionados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        items(selectedItems) { (item, cantidad) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.descripcionSafe,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Código: ${item.codigoMatSafe}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Cantidad: $cantidad ${item.unidadSafe}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Precio: ${item.precioFormateado}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (!isLoading) {
                                    IconButton(
                                        onClick = {
                                            selectedItems = selectedItems.filter { it.first != item }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Producto")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Advertencia si hay productos con stock insuficiente
                if (hasInsufficientStock) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Algunos productos tienen cantidad mayor al stock disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Observaciones
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isLoading) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            val folioInt = folio.toIntOrNull() ?: 0
                            onExitAdded(
                                folioInt,
                                selectedTipoMovimiento?.movId ?: 5,
                                selectedItems,
                                observaciones
                            )
                        },
                        enabled = !isLoading &&
                                folio.isNotEmpty() &&
                                selectedItems.isNotEmpty() &&
                                selectedTipoMovimiento != null
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Guardando..." else "Guardar")
                    }
                }
            }
        }
    }

    // Diálogo para agregar un producto
    if (showAddItemDialog) {
        AddItemToExitDialog(
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { item, cantidad ->
                selectedItems = selectedItems + Pair(item, cantidad)
                showAddItemDialog = false
            },
            availableItems = availableItems
        )
    }
}

@Composable
fun AddItemToExitDialog(
    onDismiss: () -> Unit,
    onItemAdded: (InventoryItem, Double) -> Unit,
    availableItems: List<InventoryItem>
) {
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var cantidad by remember { mutableStateOf("1.0") }
    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Agregar Producto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Buscador de productos
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar producto") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Lista de productos filtrados
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    items(availableItems.filter {
                        it.descripcionSafe.contains(searchQuery, ignoreCase = true) ||
                                it.codigoMatSafe.contains(searchQuery, ignoreCase = true)
                    }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    selectedItem = item
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedItem == item,
                                onClick = {
                                    selectedItem = item
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = item.descripcionSafe,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Código: ${item.codigoMatSafe} | Unidad: ${item.unidadSafe}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Stock disponible: ${item.existenciaFormateada}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if ((item.existencia ?: 0.0) > 0) Color.Green else Color.Red
                                )
                            }
                        }
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Cantidad con validación de stock
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        selectedItem?.let { item ->
                            val cantidadDouble = cantidad.toDoubleOrNull() ?: 0.0
                            val stockDisponible = item.existencia ?: 0.0
                            if (cantidadDouble > stockDisponible) {
                                Text(
                                    text = "Cantidad mayor al stock disponible ($stockDisponible)",
                                    color = Color.Red
                                )
                            }
                        }
                    },
                    isError = selectedItem?.let { item ->
                        val cantidadDouble = cantidad.toDoubleOrNull() ?: 0.0
                        cantidadDouble > (item.existencia ?: 0.0)
                    } ?: false
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedItem?.let { item ->
                                val cantidadDouble = cantidad.toDoubleOrNull() ?: 1.0
                                onItemAdded(item, cantidadDouble)
                            }
                        },
                        enabled = selectedItem != null && cantidad.toDoubleOrNull() != null
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

