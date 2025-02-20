package com.example.barsa.Inventarios

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem
import androidx.compose.ui.res.painterResource
import com.example.barsa.R

@Composable
fun InventoryScreen(onNavigate: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = remember {
        listOf(
            InventoryCategory(1, "Muebles", "Catálogo de muebles", R.drawable.ic_muebles),
            InventoryCategory(2, "Aranceles", "Catálogo de aranceles", R.drawable.ic_aranceles),
            InventoryCategory(3, "Pintura", "Catálogo de pinturas", R.drawable.ic_pintura),
            InventoryCategory(4, "Herramientas", "Catálogo de herramientas", R.drawable.ic_herramientas),
            InventoryCategory(5, "Materiales", "Catálogo de materiales", R.drawable.ic_materiales)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (selectedCategory == null) {
            CategoryList(
                categories = categories,
                onCategorySelected = { selectedCategory = it }
            )
        } else {
            InventoryItemsList(
                category = selectedCategory!!,
                onBackClick = { selectedCategory = null }
            )
        }
    }
}

@Composable
fun InventoryItemsList(
    category: InventoryCategory,
    onBackClick: () -> Unit
) {
    var items by remember { mutableStateOf(getItemsForCategory(category)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    Column {
        // Barra superior con botón de regreso y título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            placeholder = { Text("Buscar en ${category.name}...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Grid de items
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 250.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val filteredItems = items.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.id.toString().contains(searchQuery)
            }

            items(filteredItems) { item ->
                InventoryItemCard(
                    item = item,
                    showImage = category.name == "Aranceles",
                    onClick = { selectedItem = item }
                )
            }
        }

        // Dialog para editar item
        selectedItem?.let { item ->
            EditItemDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onSave = { updatedItem ->
                    items = items.map { if (it.id == updatedItem.id) updatedItem else it }
                    selectedItem = null
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    showImage: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Entradas",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.entries.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Salidas",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.exits.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column {
                    Text(
                        text = "Stock",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.stock.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(
    item: InventoryItem,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    var entries by remember { mutableStateOf(item.entries.toString()) }
    var exits by remember { mutableStateOf(item.exits.toString()) }
    var isError by remember { mutableStateOf(false) }

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
                    text = "Editar ${item.description}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = entries,
                    onValueChange = {
                        entries = it
                        isError = false
                    },
                    label = { Text("Entradas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = exits,
                    onValueChange = {
                        exits = it
                        isError = false
                    },
                    label = { Text("Salidas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )

                if (isError) {
                    Text(
                        text = "Por favor, ingrese números válidos",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            try {
                                val newEntries = entries.toInt()
                                val newExits = exits.toInt()
                                val newStock = newEntries - newExits

                                if (newStock >= 0) {
                                    onSave(item.copy(
                                        entries = newEntries,
                                        exits = newExits,
                                        stock = newStock
                                    ))
                                } else {
                                    isError = true
                                }
                            } catch (e: NumberFormatException) {
                                isError = true
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(
    categories: List<InventoryCategory>,
    onCategorySelected: (InventoryCategory) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category = category, onClick = { onCategorySelected(category) })
        }
    }
}

@Composable
fun CategoryCard(
    category: InventoryCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = category.iconResId),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Función auxiliar para generar datos de ejemplo
private fun getItemsForCategory(category: InventoryCategory): List<InventoryItem> {
    return when (category.name) {
        "Muebles" -> listOf(
            InventoryItem(1, "Silla de comedor", 50, 30, 20),
            InventoryItem(2, "Mesa de centro", 30, 15, 15),
            InventoryItem(3, "Sofá 3 plazas", 20, 10, 10)
        )
        "Aranceles" -> listOf(
            InventoryItem(1, "Arancel tipo A", 100, 50, 50, "https://i.postimg.cc/jSYP4wph/edaabf7d86eb9620917caba8023773ca.jpg"),
            InventoryItem(2, "Arancel tipo B", 80, 30, 50, "https://postimg.cc/qzQNkRMt"),
            InventoryItem(3, "Arancel tipo C", 60, 20, 40, "url_imagen_3")
        )
        "Pintura" -> listOf(
            InventoryItem(1, "Pintura blanca", 200, 150, 50),
            InventoryItem(2, "Barniz", 150, 100, 50),
            InventoryItem(3, "Laca", 100, 60, 40)
        )
        "Herramientas" -> listOf(
            InventoryItem(1, "Martillo", 30, 10, 20),
            InventoryItem(2, "Destornillador", 50, 20, 30),
            InventoryItem(3, "Sierra", 25, 15, 10)
        )
        "Materiales" -> listOf(
            InventoryItem(1, "Madera de pino", 1000, 800, 200),
            InventoryItem(2, "Clavos", 5000, 3000, 2000),
            InventoryItem(3, "Tornillos", 4000, 2500, 1500)
        )
        else -> emptyList()
    }
}