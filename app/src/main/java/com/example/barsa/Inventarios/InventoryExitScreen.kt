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
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryExitScreen(
    onCancel: () -> Unit,
    onSave: (InventoryExit) -> Unit,
    currentUser: String // Parámetro para el nombre del usuario actual
) {
    var selectedItems by remember { mutableStateOf<List<InventoryTransactionItem>>(emptyList()) }
    var showItemSelector by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf<ExitReason?>(null) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var otherReason by remember { mutableStateOf("") }



    // Fecha actual
    val currentDate = remember { Date() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                text = "Registrar Salida de Inventario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

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

        // Contenido principal
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Sección de fecha
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
                        text = "Fecha de Salida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = dateFormatter.format(currentDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Sección de razón de salida
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
                        text = "Razón de Salida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de razón
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedReason?.displayName ?: "",
                            onValueChange = { },
                            label = { Text("Seleccione una razón *") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showReasonDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar razón"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showReasonDropdown,
                            onDismissRequest = { showReasonDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            ExitReason.values().forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason.displayName) },
                                    onClick = {
                                        selectedReason = reason
                                        showReasonDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Campo para "Otra razón" si se selecciona OTHER
                    if (selectedReason == ExitReason.OTHER) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = otherReason,
                            onValueChange = { otherReason = it },
                            label = { Text("Especifique la razón *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo de destino
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destino") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Sección de productos
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
                        text = "Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedItems.isEmpty()) {
                        // Mensaje cuando no hay productos seleccionados
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay productos seleccionados",
                                color = Color.Gray
                            )
                        }
                    } else {
                        // Lista de productos seleccionados
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        ) {
                            selectedItems.forEachIndexed { index, item ->
                                ExitItemRow(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        selectedItems = selectedItems.toMutableList().apply {
                                            this[index] = item.copy(quantity = newQuantity)
                                        }
                                    },
                                    onRemove = {
                                        selectedItems = selectedItems.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    },
                                    maxQuantity = item.inventoryItem.existencia
                                )

                                if (index < selectedItems.size - 1) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón para agregar productos
                    Button(
                        onClick = { showItemSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Productos")
                    }
                }
            }

            // Sección de notas
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
                        text = "Notas Adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            }

            // Resumen de la salida
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Salida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total de Productos:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "${selectedItems.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total de Unidades:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "${selectedItems.sumOf { it.quantity }}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    // Mostrar el usuario que está realizando la salida
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Registrado por:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = currentUser,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // Validar campos obligatorios
                    if (selectedReason != null &&
                        (selectedReason != ExitReason.OTHER || otherReason.isNotBlank()) &&
                        selectedItems.isNotEmpty()) {

                        // Crear salida de inventario
                        val exit = InventoryExit(
                            id = UUID.randomUUID().toString(),
                            date = currentDate,
                            reason = if (selectedReason == ExitReason.OTHER) otherReason else selectedReason!!.displayName,
                            items = selectedItems,
                            destination = destination,
                            notes = notes,
                            createdBy = currentUser // Usar el nombre del usuario actual
                        )

                        onSave(exit)
                    } else {
                        // En una implementación real, mostrar mensaje de error
                        println("Por favor complete todos los campos obligatorios")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Guardar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Salida")
            }
        }
    }

    // Diálogo para seleccionar productos
    if (showItemSelector) {
        ItemSelectorDialog(
            onDismiss = { showItemSelector = false },
            onItemsSelected = { items ->
                // Agregar solo los items que no están ya en la lista
                val newItems = items.filter { newItem ->
                    selectedItems.none { it.inventoryItem.codigoMat == newItem.codigoMat }
                }.map {
                    InventoryTransactionItem(
                        inventoryItem = it,
                        quantity = 1.0,
                        unitPrice = it.pCompra
                    )
                }

                selectedItems = selectedItems + newItems
                showItemSelector = false
            }
        )
    }
}

@Composable
fun ExitItemRow(
    item: InventoryTransactionItem,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit,
    maxQuantity: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.inventoryItem.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Código: ${item.inventoryItem.codigoMat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Text(
                    text = "Disponible: ${item.inventoryItem.existencia} ${item.inventoryItem.unidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.quantity > item.inventoryItem.existencia) MaterialTheme.colorScheme.error else Color.Gray
                )
            }

            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de cantidad
            OutlinedTextField(
                value = item.quantity.toString(),
                onValueChange = {
                    val newValue = it.toDoubleOrNull() ?: 0.0
                    // Limitar la cantidad a la existencia disponible
                    val limitedValue = newValue.coerceAtMost(maxQuantity)
                    onQuantityChange(limitedValue)
                },
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = item.quantity > maxQuantity
            )

            Text(
                text = item.inventoryItem.unidad,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Mensaje de error si la cantidad excede la existencia
        if (item.quantity > maxQuantity) {
            Text(
                text = "La cantidad excede la existencia disponible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
