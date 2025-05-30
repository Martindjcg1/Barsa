package com.example.barsa.Inventarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryChangesScreen(
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEntriesOnly by remember { mutableStateOf(false) }
    var showExitsOnly by remember { mutableStateOf(false) }

    // Lista de movimientos (en una aplicación real, esto vendría de una base de datos)
    val movements = remember { getSampleMovements() }
    val movementDetails = remember { getSampleMovementDetails() }

    // Filtrar movimientos por fecha, tipo y búsqueda
    val filteredMovements = remember(movements, startDate, endDate, searchQuery, showEntriesOnly, showExitsOnly) {
        movements.filter { movement ->
            val matchesSearch = movement.folio.contains(searchQuery, ignoreCase = true) ||
                    movement.usuario.contains(searchQuery, ignoreCase = true) ||
                    movement.observacion.contains(searchQuery, ignoreCase = true) ||
                    getDescripcionMovimiento(movement.movId).contains(searchQuery, ignoreCase = true)

            val movementDate = parseDate(movement.fecha)
            val matchesDateRange = when {
                startDate != null && endDate != null -> {
                    movementDate?.let { date ->
                        !date.before(startDate) && !date.after(endDate)
                    } ?: false
                }
                startDate != null -> {
                    movementDate?.let { date ->
                        !date.before(startDate)
                    } ?: false
                }
                endDate != null -> {
                    movementDate?.let { date ->
                        !date.after(endDate)
                    } ?: false
                }
                else -> true
            }

            val matchesType = when {
                showEntriesOnly -> movimientoAumentaStock(movement.movId)
                showExitsOnly -> movimientoDisminuyeStock(movement.movId)
                else -> true
            }

            matchesSearch && matchesDateRange && matchesType
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
                text = "Historial de Movimientos",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar movimientos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Filtros de fecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fecha inicial
            OutlinedTextField(
                value = startDate?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "",
                onValueChange = { },
                placeholder = { Text("Fecha inicial") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha inicial")
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Fecha final
            OutlinedTextField(
                value = endDate?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "",
                onValueChange = { },
                placeholder = { Text("Fecha final") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha final")
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Botón para limpiar filtros de fecha
            if (startDate != null || endDate != null) {
                IconButton(
                    onClick = {
                        startDate = null
                        endDate = null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar filtros de fecha"
                    )
                }
            }
        }

        // Filtros de tipo de movimiento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showEntriesOnly && !showExitsOnly,
                onClick = {
                    showEntriesOnly = false
                    showExitsOnly = false
                },
                label = { Text("Todos") },
                leadingIcon = if (!showEntriesOnly && !showExitsOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )

            FilterChip(
                selected = showEntriesOnly,
                onClick = {
                    showEntriesOnly = !showEntriesOnly
                    if (showEntriesOnly) showExitsOnly = false
                },
                label = { Text("Entradas") },
                leadingIcon = if (showEntriesOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )

            FilterChip(
                selected = showExitsOnly,
                onClick = {
                    showExitsOnly = !showExitsOnly
                    if (showExitsOnly) showEntriesOnly = false
                },
                label = { Text("Salidas") },
                leadingIcon = if (showExitsOnly) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }

        // Lista de movimientos
        if (filteredMovements.isEmpty()) {
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
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay movimientos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (startDate != null || endDate != null ||
                        showEntriesOnly || showExitsOnly || searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Prueba a cambiar los filtros aplicados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMovements) { movement ->
                    MovementCard(
                        movement = movement,
                        details = movementDetails[movement.consecutivo] ?: emptyList()
                    )
                }
            }
        }
    }

    // DatePicker para fecha inicial - CORREGIDO CON LA SOLUCIÓN QUE FUNCIONA
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
                            // Crear un calendario con la zona horaria local
                            val selectedCalendar = Calendar.getInstance()
                            // Establecer los milisegundos seleccionados
                            selectedCalendar.timeInMillis = it
                            // SOLUCIÓN: Ajustar para compensar el offset de zona horaria
                            selectedCalendar.add(Calendar.DATE, 1) // Añadir un día para compensar
                            // Establecer la hora a 00:00:00
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            selectedCalendar.set(Calendar.MINUTE, 0)
                            selectedCalendar.set(Calendar.SECOND, 0)
                            selectedCalendar.set(Calendar.MILLISECOND, 0)
                            startDate = selectedCalendar.time
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Seleccionar fecha inicial") }
            )
        }
    }

    // DatePicker para fecha final - CORREGIDO CON LA SOLUCIÓN QUE FUNCIONA
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
                            // Crear un calendario con la zona horaria local
                            val selectedCalendar = Calendar.getInstance()
                            // Establecer los milisegundos seleccionados
                            selectedCalendar.timeInMillis = it
                            // SOLUCIÓN: Ajustar para compensar el offset de zona horaria
                            selectedCalendar.add(Calendar.DATE, 1) // Añadir un día para compensar
                            // Establecer la hora a 23:59:59
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, 23)
                            selectedCalendar.set(Calendar.MINUTE, 59)
                            selectedCalendar.set(Calendar.SECOND, 59)
                            selectedCalendar.set(Calendar.MILLISECOND, 999)
                            endDate = selectedCalendar.time
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Seleccionar fecha final") }
            )
        }
    }
}

@Composable
fun MovementCard(
    movement: MovimientosMateria,
    details: List<DetalleMovimientoMateria>
) {
    var expanded by remember { mutableStateOf(false) }
    val aumentaStock = movimientoAumentaStock(movement.movId)

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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (aumentaStock) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (aumentaStock) "Entrada" else "Salida",
                        tint = if (aumentaStock) MaterialTheme.colorScheme.primary else Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${movement.consecutivo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = movement.fecha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tipo: ${getDescripcionMovimiento(movement.movId)} (${getEfectoEnStock(movement.movId)} Stock)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (aumentaStock) MaterialTheme.colorScheme.primary else Color.Red
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Folio: ${movement.folio}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Registrado por: ${movement.usuario}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Productos: ${details.size}",
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Mostrar menos" else "Mostrar más"
                    )
                }
            }

            if (expanded && details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                details.forEach { detail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = detail.codigoMat,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )
                        Text(
                            text = "Cant: ${detail.cantidad}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f)
                        )
                        Text(
                            text = "$${detail.pCosto}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.3f),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            if (movement.observacion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observaciones: ${movement.observacion}",
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
                    imageVector = if (movement.procesada) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (movement.procesada) Color.Green else Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (movement.procesada) "Procesada" else "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (movement.procesada) Color.Green else Color.Yellow
                )
            }
        }
    }
}

// Función para parsear fechas
private fun parseDate(dateString: String): Date? {
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString)
    } catch (e: Exception) {
        null
    }
}

// Funciones helper
private fun movimientoAumentaStock(movId: Int): Boolean {
    return getMovimientosInventario().find { it.movId == movId }?.aumenta ?: false
}

private fun movimientoDisminuyeStock(movId: Int): Boolean {
    return !movimientoAumentaStock(movId)
}

private fun getDescripcionMovimiento(movId: Int): String {
    return getMovimientosInventario().find { it.movId == movId }?.descripcion ?: "Desconocido"
}

private fun getEfectoEnStock(movId: Int): String {
    return if (movimientoAumentaStock(movId)) "Aumenta" else "Disminuye"
}

// Datos de ejemplo
private fun getSampleMovements(): List<MovimientosMateria> {
    val calendar = Calendar.getInstance()
    val movements = mutableListOf<MovimientosMateria>()

    // Movimiento más reciente (entrada)
    movements.add(
        MovimientosMateria(
            consecutivo = 1001,
            movId = 4, // ENTRADA A ALMACEN
            fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time),
            folio = "ENT-001",
            usuario = "Martin",
            procesada = true,
            observacion = "Entrada de materiales de proveedor principal"
        )
    )

    // Movimiento de ayer (salida)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    movements.add(
        MovimientosMateria(
            consecutivo = 1002,
            movId = 5, // SALIDA DE ALMACEN
            fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time),
            folio = "SAL-001",
            usuario = "Martin",
            procesada = true,
            observacion = "Salida para producción de sillones"
        )
    )

    // Movimiento de hace 3 días (devolución de cliente)
    calendar.add(Calendar.DAY_OF_MONTH, -2)
    movements.add(
        MovimientosMateria(
            consecutivo = 1003,
            movId = 1, // DEVOLUCION DE CLIENTE
            fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time),
            folio = "DEV-001",
            usuario = "Martin",
            procesada = false,
            observacion = "Devolución por defecto en producto"
        )
    )

    // Movimiento de la semana pasada (devolución a proveedor)
    calendar.add(Calendar.DAY_OF_MONTH, -4)
    movements.add(
        MovimientosMateria(
            consecutivo = 1004,
            movId = 2, // DEVOLUCION A PROVEEDOR
            fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time),
            folio = "DEV-002",
            usuario = "Martin",
            procesada = true,
            observacion = "Devolución por material defectuoso"
        )
    )

    // Movimiento de hace dos semanas (devolución a almacén)
    calendar.add(Calendar.DAY_OF_MONTH, -7)
    movements.add(
        MovimientosMateria(
            consecutivo = 1005,
            movId = 3, // DEVOLUCION A ALMACEN
            fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time),
            folio = "DEV-003",
            usuario = "Martin",
            procesada = true,
            observacion = "Devolución de material no utilizado"
        )
    )

    return movements
}

private fun getSampleMovementDetails(): Map<Int, List<DetalleMovimientoMateria>> {
    val details = mutableMapOf<Int, List<DetalleMovimientoMateria>>()

    // Detalles para el movimiento 1001
    details[1001] = listOf(
        DetalleMovimientoMateria(
            id = 1,
            consecutivo = 1001,
            codigoMat = "TEL001",
            cantidad = 10.0,
            existenciaAnt = 35.0,
            pCosto = 120.0,
            procesada = true
        ),
        DetalleMovimientoMateria(
            id = 2,
            consecutivo = 1001,
            codigoMat = "CUB001",
            cantidad = 5.0,
            existenciaAnt = 10.0,
            pCosto = 350.0,
            procesada = true
        )
    )

    // Detalles para el movimiento 1002
    details[1002] = listOf(
        DetalleMovimientoMateria(
            id = 1,
            consecutivo = 1002,
            codigoMat = "TEL001",
            cantidad = 5.0,
            existenciaAnt = 45.0,
            pCosto = 120.0,
            procesada = true
        )
    )

    // Detalles para el movimiento 1003
    details[1003] = listOf(
        DetalleMovimientoMateria(
            id = 1,
            consecutivo = 1003,
            codigoMat = "CAS001",
            cantidad = 2.0,
            existenciaAnt = 4.0,
            pCosto = 85.0,
            procesada = false
        )
    )

    // Detalles para el movimiento 1004
    details[1004] = listOf(
        DetalleMovimientoMateria(
            id = 1,
            consecutivo = 1004,
            codigoMat = "00176035",
            cantidad = 20.0,
            existenciaAnt = 9542.0,
            pCosto = 0.52,
            procesada = true
        )
    )

    // Detalles para el movimiento 1005
    details[1005] = listOf(
        DetalleMovimientoMateria(
            id = 1,
            consecutivo = 1005,
            codigoMat = "CAS001",
            cantidad = 3.0,
            existenciaAnt = 15.0,
            pCosto = 85.0,
            procesada = true
        )
    )

    return details
}
