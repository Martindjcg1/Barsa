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
fun InventoryEntryScreen(
    onCancel: () -> Unit,
    onSave: (InventoryEntry) -> Unit,
    currentUser: String // Parámetro para el nombre del usuario actual
) {
    var selectedItems by remember { mutableStateOf<List<InventoryTransactionItem>>(emptyList()) }
    var showItemSelector by remember { mutableStateOf(false) }
    var supplierName by remember { mutableStateOf("") }
    var supplierContact by remember { mutableStateOf("") }
    var supplierAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showSupplierSelector by remember { mutableStateOf(false) }

    // Lista de proveedores de ejemplo
    val suppliers = remember {
        listOf(
            Supplier("1", "Ferretería El Martillo", "555-1234", "Calle Principal #123"),
            Supplier("2", "Materiales Constructores", "555-5678", "Av. Central #456"),
            Supplier("3", "Distribuidora Industrial", "555-9012", "Blvd. Norte #789")
        )
    }

    // Proveedor seleccionado
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }

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
                text = "Registrar Entrada de Inventario",
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
                        text = "Fecha de Entrada",
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

            // Sección de proveedor
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
                        text = "Proveedor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedSupplier == null) {
                        // Formulario para nuevo proveedor
                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("Nombre del Proveedor *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = supplierContact,
                            onValueChange = { supplierContact = it },
                            label = { Text("Contacto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = supplierAddress,
                            onValueChange = { supplierAddress = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para seleccionar proveedor existente
                        TextButton(
                            onClick = { showSupplierSelector = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seleccionar Proveedor Existente")
                        }
                    } else {
                        // Mostrar proveedor seleccionado
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
                            Text(
                                text = selectedSupplier!!.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Contacto: ${selectedSupplier!!.contactInfo}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Dirección: ${selectedSupplier!!.address}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para cambiar proveedor
                        TextButton(
                            onClick = { selectedSupplier = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cambiar Proveedor")
                        }
                    }
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
                                TransactionItemRow(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        selectedItems = selectedItems.toMutableList().apply {
                                            this[index] = item.copy(quantity = newQuantity)
                                        }
                                    },
                                    onPriceChange = { newPrice ->
                                        selectedItems = selectedItems.toMutableList().apply {
                                            this[index] = item.copy(unitPrice = newPrice)
                                        }
                                    },
                                    onRemove = {
                                        selectedItems = selectedItems.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
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

            // Resumen de la entrada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Entrada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total de Productos:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${selectedItems.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monto Total:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${selectedItems.sumOf { it.quantity * it.unitPrice }.format(2)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Mostrar el usuario que está realizando la entrada
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Registrado por:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = currentUser,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    if ((selectedSupplier != null || supplierName.isNotBlank()) && selectedItems.isNotEmpty()) {
                        // Crear proveedor si no se seleccionó uno existente
                        val supplier = selectedSupplier ?: Supplier(
                            id = UUID.randomUUID().toString(),
                            name = supplierName,
                            contactInfo = supplierContact,
                            address = supplierAddress
                        )

                        // Crear entrada de inventario
                        val entry = InventoryEntry(
                            id = UUID.randomUUID().toString(),
                            date = currentDate,
                            supplier = supplier,
                            items = selectedItems,
                            totalAmount = selectedItems.sumOf { it.quantity * it.unitPrice },
                            notes = notes,
                            createdBy = currentUser // Usar el nombre del usuario actual
                        )

                        onSave(entry)
                    } else {
                        // En una implementación real, mostrar mensaje de error
                        println("Por favor complete todos los campos obligatorios")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Entrada")
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

    // Diálogo para seleccionar proveedor
    if (showSupplierSelector) {
        SupplierSelectorDialog(
            suppliers = suppliers,
            onDismiss = { showSupplierSelector = false },
            onSupplierSelected = { supplier ->
                selectedSupplier = supplier
                showSupplierSelector = false
            }
        )
    }
}

@Composable
fun TransactionItemRow(
    item: InventoryTransactionItem,
    onQuantityChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Campo de cantidad
            OutlinedTextField(
                value = item.quantity.toString(),
                onValueChange = {
                    val newValue = it.toDoubleOrNull() ?: 0.0
                    onQuantityChange(newValue)
                },
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Campo de precio unitario
            OutlinedTextField(
                value = item.unitPrice.toString(),
                onValueChange = {
                    val newValue = it.toDoubleOrNull() ?: 0.0
                    onPriceChange(newValue)
                },
                label = { Text("Precio Unitario") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("$") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtotal
        Text(
            text = "Subtotal: ${(item.quantity * item.unitPrice).format(2)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectorDialog(
    onDismiss: () -> Unit,
    onItemsSelected: (List<InventoryItem>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allItems = remember { getAllInventoryItems() }
    var filteredItems by remember { mutableStateOf(allItems) }
    var selectedItems by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Productos") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        filteredItems = if (query.isEmpty()) {
                            allItems
                        } else {
                            allItems.filter { item ->
                                item.descripcion.contains(query, ignoreCase = true) ||
                                        item.codigoMat.contains(query, ignoreCase = true)
                            }
                        }
                    },
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                // Lista de productos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val isSelected = selectedItems.contains(item)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedItems = if (isSelected) {
                                        selectedItems - item
                                    } else {
                                        selectedItems + item
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedItems = if (checked) {
                                        selectedItems + item
                                    } else {
                                        selectedItems - item
                                    }
                                }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = item.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Código: ${item.codigoMat} | Unidad: ${item.unidad}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "${item.pCompra}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider()
                    }
                }

                // Contador de seleccionados
                Text(
                    text = "${selectedItems.size} productos seleccionados",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onItemsSelected(selectedItems) }
            ) {
                Text("Seleccionar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SupplierSelectorDialog(
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSupplierSelected: (Supplier) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Proveedor") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(suppliers) { supplier ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSupplierSelected(supplier) }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = supplier.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Contacto: ${supplier.contactInfo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Text(
                                text = "Dirección: ${supplier.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

// Función de extensión para formatear números con decimales
fun Double.format(digits: Int) = "%.${digits}f".format(this)
