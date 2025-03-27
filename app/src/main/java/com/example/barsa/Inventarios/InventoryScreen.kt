package com.example.barsa.Body.Inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem
import androidx.compose.ui.res.painterResource
import com.example.barsa.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Composable
fun InventoryScreen(onNavigate: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Lista de todas las categorías posibles
    val categories = remember {
        listOf(
            InventoryCategory(1, "Cubetas", "Catálogo de cubetas", R.drawable.ic_pintura),
            InventoryCategory(2, "Telas", "Catálogo de telas", R.drawable.ic_fabric),
            InventoryCategory(3, "Cascos", "Catálogo de cascos", R.drawable.ic_helmet),
            InventoryCategory(4, "Herramientas", "Catálogo de herramientas", R.drawable.ic_herramientas),
            InventoryCategory(5, "Bisagras y Herrajes", "Catálogo de bisagras y herrajes", R.drawable.ic_hinge),
            InventoryCategory(6, "Pernos y Sujetadores", "Catálogo de pernos y sujetadores", R.drawable.ic_bolt),
            InventoryCategory(7, "Cintas y Adhesivos", "Catálogo de cintas y adhesivos", R.drawable.ic_tape),
            InventoryCategory(8, "Separadores y Accesorios de Cristal", "Catálogo de separadores", R.drawable.ic_glass),
            InventoryCategory(9, "Cubrecantos y Acabados", "Catálogo de cubrecantos", R.drawable.ic_edge),
            InventoryCategory(10, "Otros Materiales de Construcción", "Otros materiales de construcción", R.drawable.ic_materiales),
            InventoryCategory(11, "Todo", "Catálogo completo", R.drawable.ic_all)
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

