package com.example.barsa.Body.Inventory

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
import java.text.SimpleDateFormat
import java.util.*

//@Composable
//fun InventoryEntriesScreen(
//    onBackClick: () -> Unit
//) {
//    var showAddEntryDialog by remember { mutableStateOf(false) }
//    var showEditEntryDialog by remember { mutableStateOf(false) }
//    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
//    var searchQuery by remember { mutableStateOf("") }
//    var entryToEdit by remember { mutableStateOf<MovimientosMateria?>(null) }
//    var entryToDelete by remember { mutableStateOf<MovimientosMateria?>(null) }
//
//    // Lista de entradas (en una aplicación real, esto vendría de una base de datos)
//    val entries = remember { mutableStateListOf<MovimientosMateria>() }
//    val entryDetails = remember { mutableStateMapOf<Int, List<DetalleMovimientoMateria>>() }
//
//    // Filtrar solo los movimientos que aumentan stock (entradas)
//    val tiposMovimientoEntrada = remember {
//        getMovimientosInventario().filter { it.aumenta }.map { it.movId }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Barra superior con botón de regreso y título
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = onBackClick) {
//                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
//            }
//            Text(
//                text = "Registro de Entradas",
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(start = 16.dp)
//            )
//        }
//
//        // Barra de búsqueda y botón para agregar
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                placeholder = { Text("Buscar entradas...") },
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                singleLine = true,
//                modifier = Modifier.weight(1f)
//            )
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Button(
//                onClick = { showAddEntryDialog = true },
//                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = null,
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Nueva Entrada")
//            }
//        }
//
//        // Lista de entradas
//        if (entries.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.MailOutline,
//                        contentDescription = null,
//                        modifier = Modifier.size(64.dp),
//                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "No hay entradas registradas",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Haz clic en 'Nueva Entrada' para registrar una entrada de inventario",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
//                    )
//                }
//            }
//        } else {
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(entries.filter {
//                    (it.folio.contains(searchQuery, ignoreCase = true) ||
//                            it.fecha.contains(searchQuery, ignoreCase = true) ||
//                            it.usuario.contains(searchQuery, ignoreCase = true)) &&
//                            tiposMovimientoEntrada.contains(it.movId)
//                }) { entry ->
//                    EntryCard(
//                        entry = entry,
//                        details = entryDetails[entry.consecutivo] ?: emptyList(),
//                        onValidateEntry = { entryToValidate ->
//                            // Actualizar el estado de procesada a true
//                            val index = entries.indexOfFirst { it.consecutivo == entryToValidate.consecutivo }
//                            if (index != -1) {
//                                entries[index] = entryToValidate.copy(procesada = true)
//                                // También actualizar los detalles
//                                entryDetails[entryToValidate.consecutivo] =
//                                    entryDetails[entryToValidate.consecutivo]?.map {
//                                        it.copy(procesada = true)
//                                    } ?: emptyList()
//                            }
//                        },
//                        onEditEntry = { entryToEditParam ->
//                            entryToEdit = entryToEditParam
//                            showEditEntryDialog = true
//                        },
//                        onDeleteEntry = { entryToDeleteParam ->
//                            entryToDelete = entryToDeleteParam
//                            showDeleteConfirmDialog = true
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    // Diálogo para agregar una nueva entrada
//    if (showAddEntryDialog) {
//        AddEntryDialog(
//            onDismiss = { showAddEntryDialog = false },
//            onEntryAdded = { entry, details ->
//                entries.add(entry)
//                entryDetails[entry.consecutivo] = details
//                showAddEntryDialog = false
//            },
//            tiposMovimiento = tiposMovimientoEntrada.map { movId ->
//                getMovimientosInventario().find { it.movId == movId }!!
//            }
//        )
//    }
//
//    // Diálogo para editar entrada
//    if (showEditEntryDialog && entryToEdit != null) {
//        EditEntryDialog(
//            entry = entryToEdit!!,
//            details = entryDetails[entryToEdit!!.consecutivo] ?: emptyList(),
//            onDismiss = {
//                showEditEntryDialog = false
//                entryToEdit = null
//            },
//            onEntryUpdated = { updatedEntry, updatedDetails ->
//                val index = entries.indexOfFirst { it.consecutivo == updatedEntry.consecutivo }
//                if (index != -1) {
//                    entries[index] = updatedEntry
//                    entryDetails[updatedEntry.consecutivo] = updatedDetails
//                }
//                showEditEntryDialog = false
//                entryToEdit = null
//            },
//            tiposMovimiento = tiposMovimientoEntrada.map { movId ->
//                getMovimientosInventario().find { it.movId == movId }!!
//            }
//        )
//    }
//
//    // Diálogo de confirmación para eliminar
//    if (showDeleteConfirmDialog && entryToDelete != null) {
//        AlertDialog(
//            onDismissRequest = {
//                showDeleteConfirmDialog = false
//                entryToDelete = null
//            },
//            title = { Text("Confirmar eliminación") },
//            text = {
//                Text("¿Estás seguro de que deseas eliminar la entrada #${entryToDelete!!.consecutivo}? Esta acción no se puede deshacer.")
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        entries.removeIf { it.consecutivo == entryToDelete!!.consecutivo }
//                        entryDetails.remove(entryToDelete!!.consecutivo)
//                        showDeleteConfirmDialog = false
//                        entryToDelete = null
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
//                ) {
//                    Text("Eliminar")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = {
//                    showDeleteConfirmDialog = false
//                    entryToDelete = null
//                }) {
//                    Text("Cancelar")
//                }
//            }
//        )
//    }
//}

//@Composable
//fun EntryCard(
//    entry: MovimientosMateria,
//    details: List<DetalleMovimientoMateria>,
//    onValidateEntry: (MovimientosMateria) -> Unit,
//    onEditEntry: (MovimientosMateria) -> Unit,
//    onDeleteEntry: (MovimientosMateria) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Entrada #${entry.consecutivo}",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = entry.fecha,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Tipo: ${getDescripcionMovimiento(entry.movId)}",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = "Folio: ${entry.folio}",
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = "Registrado por: ${entry.usuario}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//
//            Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Productos: ${details.size}",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//
//                IconButton(onClick = { expanded = !expanded }) {
//                    Icon(
//                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                        contentDescription = if (expanded) "Mostrar menos" else "Mostrar más"
//                    )
//                }
//            }
//
//            if (expanded && details.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(8.dp))
//
//                details.forEach { detail ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 4.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = detail.codigoMat,
//                            style = MaterialTheme.typography.bodySmall,
//                            modifier = Modifier.weight(0.3f)
//                        )
//                        Text(
//                            text = "Cant: ${detail.cantidad}",
//                            style = MaterialTheme.typography.bodySmall,
//                            modifier = Modifier.weight(0.3f)
//                        )
//                        Text(
//                            text = "$${detail.pCosto}",
//                            style = MaterialTheme.typography.bodySmall,
//                            modifier = Modifier.weight(0.3f),
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//                }
//            }
//
//            if (entry.observacion.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Observaciones: ${entry.observacion}",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
//            }
//
//            // Estado de procesamiento y botones de acción
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = if (entry.procesada) Icons.Default.CheckCircle else Icons.Default.Refresh,
//                        contentDescription = null,
//                        tint = if (entry.procesada) Color.Green else Color.Yellow,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = if (entry.procesada) "Procesada" else "Pendiente",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = if (entry.procesada) Color.Green else Color.Yellow
//                    )
//                }
//
//                // Botones de acción (solo si está pendiente)
//                if (!entry.procesada) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        // Botón Editar
//                        IconButton(
//                            onClick = { onEditEntry(entry) },
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Edit,
//                                contentDescription = "Editar",
//                                tint = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//
//                        // Botón Eliminar
//                        IconButton(
//                            onClick = { onDeleteEntry(entry) },
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Delete,
//                                contentDescription = "Eliminar",
//                                tint = Color.Red,
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//
//                        // Botón Validar
//                        Button(
//                            onClick = { onValidateEntry(entry) },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Green
//                            ),
//                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = null,
//                                modifier = Modifier.size(16.dp)
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                text = "Validar",
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Resto del código permanece igual (AddEntryDialog, AddItemToEntryDialog)...
//// Pero agregamos el nuevo EditEntryDialog
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditEntryDialog(
//    entry: MovimientosMateria,
//    details: List<DetalleMovimientoMateria>,
//    onDismiss: () -> Unit,
//    onEntryUpdated: (MovimientosMateria, List<DetalleMovimientoMateria>) -> Unit,
//    tiposMovimiento: List<MovimientoInventario>
//) {
//    var folio by remember { mutableStateOf(entry.folio) }
//    var observaciones by remember { mutableStateOf(entry.observacion) }
//    var selectedItems by remember {
//        mutableStateOf(
//            details.map { detail ->
//                val item = getAllInventoryItems().find { it.codigoMat == detail.codigoMat }
//                if (item != null) {
//                    InventoryItemSelection(
//                        item = item,
//                        cantidad = detail.cantidad,
//                        pCosto = detail.pCosto
//                    )
//                } else null
//            }.filterNotNull()
//        )
//    }
//    var showAddItemDialog by remember { mutableStateOf(false) }
//    var selectedTipoMovimiento by remember {
//        mutableStateOf(tiposMovimiento.find { it.movId == entry.movId })
//    }
//    var showTipoMovimientoDropdown by remember { mutableStateOf(false) }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth()
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Editar Entrada #${entry.consecutivo}",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    IconButton(onClick = onDismiss) {
//                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Fecha (solo lectura)
//                OutlinedTextField(
//                    value = entry.fecha,
//                    onValueChange = { },
//                    label = { Text("Fecha") },
//                    readOnly = true,
//                    modifier = Modifier.fillMaxWidth(),
//                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Tipo de Movimiento
//                Box(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    OutlinedTextField(
//                        value = selectedTipoMovimiento?.descripcion ?: "Seleccione un tipo",
//                        onValueChange = { },
//                        label = { Text("Tipo de Movimiento") },
//                        readOnly = true,
//                        trailingIcon = {
//                            IconButton(onClick = { showTipoMovimientoDropdown = true }) {
//                                Icon(
//                                    imageVector = Icons.Default.ArrowDropDown,
//                                    contentDescription = "Seleccionar tipo"
//                                )
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
//                    )
//
//                    DropdownMenu(
//                        expanded = showTipoMovimientoDropdown,
//                        onDismissRequest = { showTipoMovimientoDropdown = false },
//                        modifier = Modifier.fillMaxWidth(0.9f)
//                    ) {
//                        tiposMovimiento.forEach { tipo ->
//                            DropdownMenuItem(
//                                text = { Text(tipo.descripcion) },
//                                onClick = {
//                                    selectedTipoMovimiento = tipo
//                                    showTipoMovimientoDropdown = false
//                                }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Folio
//                OutlinedTextField(
//                    value = folio,
//                    onValueChange = { folio = it },
//                    label = { Text("Folio") },
//                    modifier = Modifier.fillMaxWidth(),
//                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Lista de productos seleccionados
//                Text(
//                    text = "Productos",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                if (selectedItems.isEmpty()) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(100.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No hay productos seleccionados",
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                } else {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(150.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
//                    ) {
//                        items(selectedItems) { item ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(8.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        text = item.item.descripcion,
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                    Text(
//                                        text = "Código: ${item.item.codigoMat}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                    Text(
//                                        text = "Cantidad: ${item.cantidad} ${item.item.unidad}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                    Text(
//                                        text = "Precio: $${item.pCosto}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                }
//                                IconButton(
//                                    onClick = {
//                                        selectedItems = selectedItems.filter { it != item }
//                                    }
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Delete,
//                                        contentDescription = "Eliminar",
//                                        tint = MaterialTheme.colorScheme.error
//                                    )
//                                }
//                            }
//                            Divider()
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Button(
//                    onClick = { showAddItemDialog = true },
//                    modifier = Modifier.align(Alignment.End)
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Agregar Producto")
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Observaciones
//                OutlinedTextField(
//                    value = observaciones,
//                    onValueChange = { observaciones = it },
//                    label = { Text("Observaciones") },
//                    modifier = Modifier.fillMaxWidth(),
//                    minLines = 2
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Botones
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Cancelar")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = {
//                            // Actualizar la entrada
//                            val updatedEntry = entry.copy(
//                                movId = selectedTipoMovimiento?.movId ?: entry.movId,
//                                folio = folio,
//                                observacion = observaciones
//                            )
//
//                            // Actualizar los detalles
//                            val updatedDetails = selectedItems.mapIndexed { index, item ->
//                                DetalleMovimientoMateria(
//                                    id = index + 1,
//                                    consecutivo = entry.consecutivo,
//                                    codigoMat = item.item.codigoMat,
//                                    cantidad = item.cantidad,
//                                    existenciaAnt = item.item.existencia,
//                                    pCosto = item.pCosto,
//                                    procesada = false
//                                )
//                            }
//
//                            onEntryUpdated(updatedEntry, updatedDetails)
//                        },
//                        enabled = folio.isNotEmpty() && selectedItems.isNotEmpty() && selectedTipoMovimiento != null
//                    ) {
//                        Text("Actualizar")
//                    }
//                }
//            }
//        }
//    }
//
//    // Diálogo para agregar un producto
//    if (showAddItemDialog) {
//        AddItemToEntryDialog(
//            onDismiss = { showAddItemDialog = false },
//            onItemAdded = { item ->
//                selectedItems = selectedItems + item
//                showAddItemDialog = false
//            },
//            availableItems = getAllInventoryItems()
//        )
//    }
//}
//
//// Las funciones AddEntryDialog y AddItemToEntryDialog permanecen iguales...
//// (Copio el código anterior aquí para completar el archivo)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEntryDialog(
//    onDismiss: () -> Unit,
//    onEntryAdded: (MovimientosMateria, List<DetalleMovimientoMateria>) -> Unit,
//    tiposMovimiento: List<MovimientoInventario>
//) {
//    var folio by remember { mutableStateOf("") }
//    var observaciones by remember { mutableStateOf("") }
//    var selectedItems by remember { mutableStateOf<List<InventoryItemSelection>>(emptyList()) }
//    var showAddItemDialog by remember { mutableStateOf(false) }
//    var selectedTipoMovimiento by remember { mutableStateOf(tiposMovimiento.firstOrNull()) }
//    var showTipoMovimientoDropdown by remember { mutableStateOf(false) }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth()
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Nueva Entrada",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    IconButton(onClick = onDismiss) {
//                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Fecha (automática)
//                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                val currentDate = dateFormat.format(Date())
//
//                OutlinedTextField(
//                    value = currentDate,
//                    onValueChange = { },
//                    label = { Text("Fecha") },
//                    readOnly = true,
//                    modifier = Modifier.fillMaxWidth(),
//                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Tipo de Movimiento
//                Box(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    OutlinedTextField(
//                        value = selectedTipoMovimiento?.descripcion ?: "Seleccione un tipo",
//                        onValueChange = { },
//                        label = { Text("Tipo de Movimiento") },
//                        readOnly = true,
//                        trailingIcon = {
//                            IconButton(onClick = { showTipoMovimientoDropdown = true }) {
//                                Icon(
//                                    imageVector = Icons.Default.ArrowDropDown,
//                                    contentDescription = "Seleccionar tipo"
//                                )
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
//                    )
//
//                    DropdownMenu(
//                        expanded = showTipoMovimientoDropdown,
//                        onDismissRequest = { showTipoMovimientoDropdown = false },
//                        modifier = Modifier.fillMaxWidth(0.9f)
//                    ) {
//                        tiposMovimiento.forEach { tipo ->
//                            DropdownMenuItem(
//                                text = { Text(tipo.descripcion) },
//                                onClick = {
//                                    selectedTipoMovimiento = tipo
//                                    showTipoMovimientoDropdown = false
//                                }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Folio
//                OutlinedTextField(
//                    value = folio,
//                    onValueChange = { folio = it },
//                    label = { Text("Folio") },
//                    modifier = Modifier.fillMaxWidth(),
//                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Lista de productos seleccionados
//                Text(
//                    text = "Productos",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                if (selectedItems.isEmpty()) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(100.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No hay productos seleccionados",
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                } else {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(150.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
//                    ) {
//                        items(selectedItems) { item ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(8.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        text = item.item.descripcion,
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                    Text(
//                                        text = "Código: ${item.item.codigoMat}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                    Text(
//                                        text = "Cantidad: ${item.cantidad} ${item.item.unidad}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                    Text(
//                                        text = "Precio: $${item.pCosto}",
//                                        style = MaterialTheme.typography.bodySmall
//                                    )
//                                }
//                                IconButton(
//                                    onClick = {
//                                        selectedItems = selectedItems.filter { it != item }
//                                    }
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Delete,
//                                        contentDescription = "Eliminar",
//                                        tint = MaterialTheme.colorScheme.error
//                                    )
//                                }
//                            }
//                            Divider()
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Button(
//                    onClick = { showAddItemDialog = true },
//                    modifier = Modifier.align(Alignment.End)
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text("Agregar Producto")
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Observaciones
//                OutlinedTextField(
//                    value = observaciones,
//                    onValueChange = { observaciones = it },
//                    label = { Text("Observaciones") },
//                    modifier = Modifier.fillMaxWidth(),
//                    minLines = 2
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Botones
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Cancelar")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = {
//                            // Generar un consecutivo único (en una app real vendría de la base de datos)
//                            val consecutivo = (1..10000).random()
//
//                            // Crear la entrada - CORREGIDO: procesada = false por defecto
//                            val entry = MovimientosMateria(
//                                consecutivo = consecutivo,
//                                movId = selectedTipoMovimiento?.movId ?: 4,
//                                fecha = currentDate,
//                                folio = folio,
//                                usuario = "Martin",
//                                procesada = false, // CAMBIO PRINCIPAL: Ahora queda pendiente
//                                observacion = observaciones
//                            )
//
//                            // Crear los detalles - CORREGIDO: procesada = false por defecto
//                            val details = selectedItems.mapIndexed { index, item ->
//                                DetalleMovimientoMateria(
//                                    id = index + 1,
//                                    consecutivo = consecutivo,
//                                    codigoMat = item.item.codigoMat,
//                                    cantidad = item.cantidad,
//                                    existenciaAnt = item.item.existencia,
//                                    pCosto = item.pCosto,
//                                    procesada = false // CAMBIO PRINCIPAL: Ahora queda pendiente
//                                )
//                            }
//
//                            onEntryAdded(entry, details)
//                        },
//                        enabled = folio.isNotEmpty() && selectedItems.isNotEmpty() && selectedTipoMovimiento != null
//                    ) {
//                        Text("Guardar")
//                    }
//                }
//            }
//        }
//    }
//
//    // Diálogo para agregar un producto
//    if (showAddItemDialog) {
//        AddItemToEntryDialog(
//            onDismiss = { showAddItemDialog = false },
//            onItemAdded = { item ->
//                selectedItems = selectedItems + item
//                showAddItemDialog = false
//            },
//            availableItems = getAllInventoryItems()
//        )
//    }
//}

//@Composable
//fun AddItemToEntryDialog(
//    onDismiss: () -> Unit,
//    onItemAdded: (InventoryItemSelection) -> Unit,
//    availableItems: List<InventoryItemfake>
//) {
//    var selectedItem by remember { mutableStateOf<InventoryItemfake?>(null) }
//    var cantidad by remember { mutableStateOf("1.0") }
//    var precioUnitario by remember { mutableStateOf("0.0") }
//    var searchQuery by remember { mutableStateOf("") }
//
//    Dialog(onDismissRequest = onDismiss) {
////        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth()
//            ) {
//                Text(
//                    text = "Agregar Producto",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Buscador de productos
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    label = { Text("Buscar producto") },
//                    modifier = Modifier.fillMaxWidth(),
//                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Lista de productos filtrados
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
//                ) {
//                    items(availableItems.filter {
//                        it.descripcion.contains(searchQuery, ignoreCase = true) ||
//                                it.codigoMat.contains(searchQuery, ignoreCase = true)
//                    }) { item ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                                .clickable {
//                                    selectedItem = item
//                                    precioUnitario = item.pCompra.toString()
//                                },
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            RadioButton(
//                                selected = selectedItem == item,
//                                onClick = {
//                                    selectedItem = item
//                                    precioUnitario = item.pCompra.toString()
//                                }
//                            )
//                            Column(modifier = Modifier.padding(start = 8.dp)) {
//                                Text(
//                                    text = item.descripcion,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Text(
//                                    text = "Código: ${item.codigoMat} | Unidad: ${item.unidad}",
//                                    style = MaterialTheme.typography.bodySmall
//                                )
//                            }
//                        }
//                        Divider()
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Cantidad y precio
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    OutlinedTextField(
//                        value = cantidad,
//                        onValueChange = { cantidad = it },
//                        label = { Text("Cantidad") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.weight(1f)
//                    )
//
//                    OutlinedTextField(
//                        value = precioUnitario,
//                        onValueChange = { precioUnitario = it },
//                        label = { Text("Precio Unitario") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.weight(1f),
//                        prefix = { Text("$") }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Botones
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Cancelar")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = {
//                            selectedItem?.let { item ->
//                                val cantidadDouble = cantidad.toDoubleOrNull() ?: 1.0
//                                val precioDouble = precioUnitario.toDoubleOrNull() ?: 0.0
//
//                                val itemSelection = InventoryItemSelection(
//                                    item = item,
//                                    cantidad = cantidadDouble,
//                                    pCosto = precioDouble
//                                )
//
//                                onItemAdded(itemSelection)
//                            }
//                        },
//                        enabled = selectedItem != null &&
//                                cantidad.toDoubleOrNull() != null &&
//                                precioUnitario.toDoubleOrNull() != null
//                    ) {
//                        Text("Agregar")
//                    }
//                }
//            }
//        }
//    }
//}