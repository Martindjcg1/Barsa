package com.example.barsa.Body.Inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem

@Composable
fun InventoryItemsList(
    category: InventoryCategory,
    onBackClick: () -> Unit
) {
    // Obtener todos los items y filtrarlos según la categoría seleccionada
    var allItems by remember { mutableStateOf(getAllInventoryItems()) }
    var filteredItems by remember {
        mutableStateOf(
            if (category.name == "Todo") {
                allItems
            } else {
                allItems.filter { categorizarMaterial(it.descripcion) == category.name }
            }
        )
    }

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
            onValueChange = {
                searchQuery = it
                // Filtrar items basados en la búsqueda
                filteredItems = if (category.name == "Todo") {
                    allItems.filter { item ->
                        item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                item.codigoMat.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    allItems.filter { item ->
                        categorizarMaterial(item.descripcion) == category.name &&
                                (item.descripcion.contains(searchQuery, ignoreCase = true) ||
                                        item.codigoMat.contains(searchQuery, ignoreCase = true))
                    }
                }
            },
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
            items(filteredItems) { item ->
                InventoryItemCard(
                    item = item,
                    onClick = { selectedItem = item }
                )
            }
        }

        // Dialog para ver detalles del item
        selectedItem?.let { item ->
            ItemDetailDialog(
                item = item,
                onDismiss = { selectedItem = null }
            )
        }
    }
}

