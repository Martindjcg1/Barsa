package com.example.barsa.Body.Inventory


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItemfake
import com.example.barsa.data.retrofit.ui.InventoryViewModel

@Composable
fun CategoryList(
    categories: List<InventoryCategory>,
    onCategorySelected: (InventoryCategory) -> Unit,
    inventoryState: InventoryViewModel.InventoryState
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { category ->
            CategoryCard(
                category = category,
                itemCount = when (inventoryState) {
                    is InventoryViewModel.InventoryState.Success -> {
                        if (category.name == "Todo") {
                            inventoryState.response.data.size
                        } else {
                            inventoryState.response.data.count {
                                categorizarMaterial(it.descripcion) == category.name
                            }
                        }
                    }
                    else -> 0
                },
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: InventoryCategory,
    itemCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de la categoría (puedes usar un icono por defecto)
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = category.name,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.title1,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "$itemCount items",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
    }
}

// Función helper para categorizar materiales
fun categorizarMaterial(descripcion: String): String {
    return when {
        descripcion.contains("cubeta", ignoreCase = true) -> "Cubetas"
        descripcion.contains("tela", ignoreCase = true) -> "Telas"
        descripcion.contains("casco", ignoreCase = true) -> "Cascos"
        descripcion.contains("herramienta", ignoreCase = true) -> "Herramientas"
        descripcion.contains("bisagra", ignoreCase = true) ||
                descripcion.contains("herraje", ignoreCase = true) -> "Bisagras y Herrajes"
        descripcion.contains("perno", ignoreCase = true) ||
                descripcion.contains("tornillo", ignoreCase = true) -> "Pernos y Sujetadores"
        descripcion.contains("cinta", ignoreCase = true) ||
                descripcion.contains("adhesivo", ignoreCase = true) -> "Cintas y Adhesivos"
        descripcion.contains("cristal", ignoreCase = true) ||
                descripcion.contains("vidrio", ignoreCase = true) -> "Separadores y Accesorios de Cristal"
        descripcion.contains("cubrecanto", ignoreCase = true) -> "Cubrecantos y Acabados"
        else -> "Otros Materiales de Construcción"
    }
}
