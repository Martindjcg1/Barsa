package com.example.barsa.Body.Inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.example.barsa.Models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showDateFilter by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Datos de ejemplo
    val entries = remember { generateSampleEntries() }
    val exits = remember { generateSampleExits() }

    // Modificar la declaración de las variables filteredEntries y filteredExits para que sean mutableState
    var filteredEntries by remember { mutableStateOf(entries) }
    var filteredExits by remember { mutableStateOf(exits) }

    // Definir las funciones de filtrado
    val updateFilteredEntries = {
        filteredEntries = entries.filter { entry ->
            val matchesSearch = searchQuery.isEmpty() ||
                    entry.supplier.name.contains(searchQuery, ignoreCase = true) ||
                    entry.items.any { it.inventoryItem.descripcion.contains(searchQuery, ignoreCase = true) } ||
                    entry.createdBy.contains(searchQuery, ignoreCase = true)

            val matchesStartDate = if (startDate != null) {
                val entryCalendar = Calendar.getInstance()
                entryCalendar.time = entry.date
                entryCalendar.set(Calendar.HOUR_OF_DAY, 0)
                entryCalendar.set(Calendar.MINUTE, 0)
                entryCalendar.set(Calendar.SECOND, 0)
                entryCalendar.set(Calendar.MILLISECOND, 0)

                val startCalendar = Calendar.getInstance()
                startCalendar.time = startDate!!
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)

                entryCalendar.timeInMillis >= startCalendar.timeInMillis
            } else true

            val matchesEndDate = if (endDate != null) {
                val entryCalendar = Calendar.getInstance()
                entryCalendar.time = entry.date
                entryCalendar.set(Calendar.HOUR_OF_DAY, 23)
                entryCalendar.set(Calendar.MINUTE, 59)
                entryCalendar.set(Calendar.SECOND, 59)
                entryCalendar.set(Calendar.MILLISECOND, 999)

                val endCalendar = Calendar.getInstance()
                endCalendar.time = endDate!!
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)

                entryCalendar.timeInMillis <= endCalendar.timeInMillis
            } else true

            matchesSearch && matchesStartDate && matchesEndDate
        }
    }

    val updateFilteredExits = {
        filteredExits = exits.filter { exit ->
            val matchesSearch = searchQuery.isEmpty() ||
                    exit.reason.contains(searchQuery, ignoreCase = true) ||
                    exit.items.any { it.inventoryItem.descripcion.contains(searchQuery, ignoreCase = true) } ||
                    exit.createdBy.contains(searchQuery, ignoreCase = true)

            val matchesStartDate = if (startDate != null) {
                val exitCalendar = Calendar.getInstance()
                exitCalendar.time = exit.date
                exitCalendar.set(Calendar.HOUR_OF_DAY, 0)
                exitCalendar.set(Calendar.MINUTE, 0)
                exitCalendar.set(Calendar.SECOND, 0)
                exitCalendar.set(Calendar.MILLISECOND, 0)

                val startCalendar = Calendar.getInstance()
                startCalendar.time = startDate!!
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                startCalendar.set(Calendar.MILLISECOND, 0)

                exitCalendar.timeInMillis >= startCalendar.timeInMillis
            } else true

            val matchesEndDate = if (endDate != null) {
                val exitCalendar = Calendar.getInstance()
                exitCalendar.time = exit.date
                exitCalendar.set(Calendar.HOUR_OF_DAY, 23)
                exitCalendar.set(Calendar.MINUTE, 59)
                exitCalendar.set(Calendar.SECOND, 59)
                exitCalendar.set(Calendar.MILLISECOND, 999)

                val endCalendar = Calendar.getInstance()
                endCalendar.time = endDate!!
                endCalendar.set(Calendar.HOUR_OF_DAY, 23)
                endCalendar.set(Calendar.MINUTE, 59)
                endCalendar.set(Calendar.SECOND, 59)
                endCalendar.set(Calendar.MILLISECOND, 999)

                exitCalendar.timeInMillis <= endCalendar.timeInMillis
            } else true

            matchesSearch && matchesStartDate && matchesEndDate
        }
    }

    // Inicializar los filtros cuando cambia la búsqueda o las fechas
    LaunchedEffect(searchQuery, startDate, endDate) {
        updateFilteredEntries()
        updateFilteredExits()
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
            Text(
                text = "Historial de Movimientos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Barra de búsqueda y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    // No es necesario llamar a updateFilteredEntries() aquí porque el LaunchedEffect lo hará
                },
                placeholder = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = { showDateFilter = !showDateFilter },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (startDate != null || endDate != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Filtrar por fecha",
                    tint = if (startDate != null || endDate != null)
                        Color.White
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }

        // Filtro de fechas (visible solo cuando showDateFilter es true)
        AnimatedVisibility(
            visible = showDateFilter,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtrar por Fecha",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Selector de fecha de inicio
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    // Mostrar DatePicker para fecha de inicio
                                    showStartDatePicker = true
                                },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (startDate != null)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Fecha Inicio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = startDate?.let { dateFormatter.format(it) } ?: "Seleccionar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (startDate != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Selector de fecha de fin
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    // Mostrar DatePicker para fecha de fin
                                    showEndDatePicker = true
                                },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (endDate != null)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Fecha Fin",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = endDate?.let { dateFormatter.format(it) } ?: "Seleccionar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (endDate != null) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                startDate = null
                                endDate = null
                                // No es necesario llamar a updateFilteredEntries() aquí porque el LaunchedEffect lo hará
                            }
                        ) {
                            Text("Limpiar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                showDateFilter = false
                                // No es necesario llamar a updateFilteredEntries() aquí porque el LaunchedEffect lo hará
                            }
                        ) {
                            Text("Aplicar")
                        }
                    }
                }
            }
        }

        // DatePicker para fecha de inicio
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = startDate?.time ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Obtener la fecha seleccionada
                            datePickerState.selectedDateMillis?.let {
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.timeInMillis = it
                                startDate = selectedCalendar.time
                                // No es necesario llamar a updateFilteredEntries() aquí porque el LaunchedEffect lo hará
                            }
                            showStartDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showStartDatePicker = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

        // DatePicker para fecha de fin
        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Obtener la fecha seleccionada
                            datePickerState.selectedDateMillis?.let {
                                val selectedCalendar = Calendar.getInstance()
                                selectedCalendar.timeInMillis = it
                                endDate = selectedCalendar.time
                                // No es necesario llamar a updateFilteredEntries() aquí porque el LaunchedEffect lo hará
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEndDatePicker = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

        // Pestañas para alternar entre entradas y salidas
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Entradas") },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Salidas") },
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según la pestaña seleccionada
        when (selectedTab) {
            0 -> EntriesHistoryList(filteredEntries)
            1 -> ExitsHistoryList(filteredExits)
        }
    }
}

@Composable
fun EntriesHistoryList(entries: List<InventoryEntry>) {
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    if (entries.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay entradas registradas",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                val isExpanded = expandedItemId == entry.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedItemId = if (isExpanded) null else entry.id
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Entrada: ${entry.id.takeLast(8).uppercase()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Fecha: ${dateFormatter.format(entry.date)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = "${entry.totalAmount.format(2)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Proveedor: ${entry.supplier.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Productos: ${entry.items.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Mostrar el usuario que realizó la entrada
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = entry.createdBy,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Divider()

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Detalle de Productos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            entry.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.inventoryItem.descripcion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Código: ${item.inventoryItem.codigoMat}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "${item.quantity} ${item.inventoryItem.unidad}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            text = "${item.unitPrice} c/u",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 0.5.dp
                                )
                            }

                            if (entry.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Notas:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = entry.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Icono para expandir/contraer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExitsHistoryList(exits: List<InventoryExit>) {
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    if (exits.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay salidas registradas",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exits) { exit ->
                val isExpanded = expandedItemId == exit.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedItemId = if (isExpanded) null else exit.id
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Salida: ${exit.id.takeLast(8).uppercase()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Fecha: ${dateFormatter.format(exit.date)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Chip(
                                onClick = { },
                                label = { Text(exit.reason) },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (exit.destination.isNotEmpty()) {
                            Text(
                                text = "Destino: ${exit.destination}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Productos: ${exit.items.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Mostrar el usuario que realizó la salida
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = exit.createdBy,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Divider()

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Detalle de Productos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            exit.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.inventoryItem.descripcion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Código: ${item.inventoryItem.codigoMat}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    Text(
                                        text = "${item.quantity} ${item.inventoryItem.unidad}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    thickness = 0.5.dp
                                )
                            }

                            if (exit.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Notas:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = exit.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Icono para expandir/contraer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funciones para generar datos de ejemplo
private fun generateSampleEntries(): List<InventoryEntry> {
    val allItems = getAllInventoryItems()
    val suppliers = listOf(
        Supplier("1", "Ferretería El Martillo", "555-1234", "Calle Principal #123"),
        Supplier("2", "Materiales Constructores", "555-5678", "Av. Central #456"),
        Supplier("3", "Distribuidora Industrial", "555-9012", "Blvd. Norte #789")
    )

    val calendar = Calendar.getInstance()

    return listOf(
        InventoryEntry(
            id = UUID.randomUUID().toString(),
            date = calendar.time,
            supplier = suppliers[0],
            items = listOf(
                InventoryTransactionItem(allItems[0], 10.0, 1.5),
                InventoryTransactionItem(allItems[1], 5.0, 2.0)
            ),
            totalAmount = 25.0,
            notes = "Entrega regular mensual",
            createdBy = "Martin Castañeda"
        ),
        InventoryEntry(
            id = UUID.randomUUID().toString(),
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            supplier = suppliers[1],
            items = listOf(
                InventoryTransactionItem(allItems[2], 20.0, 3.0),
                InventoryTransactionItem(allItems[3], 15.0, 1.0),
                InventoryTransactionItem(allItems[4], 8.0, 5.0)
            ),
            totalAmount = 115.0,
            notes = "Pedido especial para proyecto XYZ",
            createdBy = "Ana García"
        ),
        InventoryEntry(
            id = UUID.randomUUID().toString(),
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -7) }.time,
            supplier = suppliers[2],
            items = listOf(
                InventoryTransactionItem(allItems[5], 30.0, 0.5),
                InventoryTransactionItem(allItems[6], 25.0, 0.75)
            ),
            totalAmount = 33.75,
            notes = "",
            createdBy = "Roberto Fernández"
        )
    )
}

private fun generateSampleExits(): List<InventoryExit> {
    val allItems = getAllInventoryItems()
    val calendar = Calendar.getInstance()

    return listOf(
        InventoryExit(
            id = UUID.randomUUID().toString(),
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            reason = ExitReason.PRODUCTION.displayName,
            items = listOf(
                InventoryTransactionItem(allItems[0], 5.0),
                InventoryTransactionItem(allItems[1], 3.0)
            ),
            destination = "Área de Producción",
            notes = "Para fabricación de muebles modelo A",
            createdBy = "Carlos López"
        ),
        InventoryExit(
            id = UUID.randomUUID().toString(),
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
            reason = ExitReason.DAMAGE.displayName,
            items = listOf(
                InventoryTransactionItem(allItems[2], 2.0)
            ),
            destination = "",
            notes = "Material dañado durante el transporte",
            createdBy = "Laura Martínez"
        ),
        InventoryExit(
            id = UUID.randomUUID().toString(),
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -5) }.time,
            reason = ExitReason.TRANSFER.displayName,
            items = listOf(
                InventoryTransactionItem(allItems[3], 10.0),
                InventoryTransactionItem(allItems[4], 5.0),
                InventoryTransactionItem(allItems[5], 8.0)
            ),
            destination = "Sucursal Norte",
            notes = "Transferencia por falta de stock en sucursal",
            createdBy = "Martin Castañeda"
        )
    )
}

@Composable
fun Chip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    colors: ChipColors
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.backgroundColor,
        contentColor = colors.contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
        }
    }
}

object ChipDefaults {
    fun chipColors(
        backgroundColor: Color,
        contentColor: Color
    ): ChipColors = ChipColors(backgroundColor, contentColor)
}

data class ChipColors(
    val backgroundColor: Color,
    val contentColor: Color
)
