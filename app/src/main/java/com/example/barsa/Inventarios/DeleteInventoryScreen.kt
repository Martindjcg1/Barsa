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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteInventoryScreen(
    category: InventoryCategory,
    onCancel: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(getAllInventoryItems()) }
    var filteredItems by remember {
        mutableStateOf(
            if (category.name == "Todo") {
                items
            } else {
                items.filter { categorizarMaterial(it.descripcion) == category.name }
            }
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

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
                text = "Eliminar Inventario: ${category.name}",
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

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredItems = if (category.name == "Todo") {
                    items.filter { item ->
                        item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                item.codigoMat.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    items.filter { item ->
                        categorizarMaterial(item.descripcion) == category.name &&
                                (item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                        item.codigoMat.contains(searchQuery, ignoreCase = true))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Buscar por código o descripción...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Lista de items
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron items en esta categoría",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
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
                        }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro que desea eliminar el item '${itemToDelete?.descripcion}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Aquí se eliminaría el item de la base de datos
                        // Por ahora solo lo eliminamos de la lista local
                        items = items.filter { it.codigoMat != itemToDelete?.codigoMat }
                        filteredItems = filteredItems.filter { it.codigoMat != itemToDelete?.codigoMat }
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DeletableItemCard(
    item: InventoryItem,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del item
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.codigoMat,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = item.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Existencia: ${item.existencia}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.existencia < item.min) MaterialTheme.colorScheme.error else Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Precio: $${item.pCompra}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            // Botón de eliminar
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar"
                )
            }
        }
    }
}

