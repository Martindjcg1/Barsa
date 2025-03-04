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

// Función para categorizar materiales basado en su descripción
private fun categorizarMaterial(descripcion: String): String {
    val desc = descripcion.lowercase()

    return when {
        desc.contains("cubeta") -> "Cubetas"
        desc.contains("tela") -> "Telas"
        desc.contains("casco") -> "Cascos"
        desc.contains("llave") || desc.contains("allen") || desc.contains("hexagonal") -> "Herramientas"
        desc.contains("bisagra") || desc.contains("herrajes") -> "Bisagras y Herrajes"
        desc.contains("perno") || desc.contains("union") -> "Pernos y Sujetadores"
        desc.contains("cinta") || desc.contains("adhesivo") -> "Cintas y Adhesivos"
        desc.contains("separador") || desc.contains("cristal") -> "Separadores y Accesorios de Cristal"
        desc.contains("cubre canto") || desc.contains("nogal") -> "Cubrecantos y Acabados"
        desc.contains("tachuela") || desc.contains("tira") || desc.contains("banda") -> "Otros Materiales de Construcción"
        else -> "Otros"
    }
}

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

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Actualizar la visualización de la imagen para hacerla interactuable
            if (!item.imagenUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageViewer = true }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imagenUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.descripcion,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.codigoMat,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.unidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Máximo",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.max.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Mínimo",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.min.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.existencia < item.min) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "Cant/Unidad",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.cantXUnidad.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Mostrar el visor de imágenes si se hace clic en la imagen
    if (showImageViewer && !item.imagenUrl.isNullOrEmpty()) {
        ImageViewerDialog(
            imageUrl = item.imagenUrl,
            onDismiss = { showImageViewer = false }
        )
    }
}

@Composable
fun ItemDetailDialog(
    item: InventoryItem,
    onDismiss: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }

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
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles del Producto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Actualizar la visualización de la imagen en el diálogo para hacerla interactuable
                if (!item.imagenUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showImageViewer = true }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imagenUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.descripcion,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem("Código", item.codigoMat)
                DetailItem("Descripción", item.descripcion)
                DetailItem("Unidad", item.unidad)
                DetailItem("Precio Compra", "%.2f".format(item.pCompra))
                DetailItem("Existencia", "%.2f".format(item.existencia))
                DetailItem("Máximo", item.max.toString())
                DetailItem("Mínimo", item.min.toString())
                DetailItem("Inventario Inicial", "%.2f".format(item.inventarioInicial))
                DetailItem("Unidad Entrada", item.unidadEntrada)
                DetailItem("Cantidad por Unidad", item.cantXUnidad.toString())
                DetailItem("Proceso", item.proceso)
                DetailItem("Estado", if (item.borrado) "Borrado" else "Activo")
                if (!item.imagenUrl.isNullOrEmpty()) {
                    DetailItem("URL de Imagen", item.imagenUrl)
                }
            }
        }
    }

    // Mostrar el visor de imágenes si se hace clic en la imagen
    if (showImageViewer && !item.imagenUrl.isNullOrEmpty()) {
        ImageViewerDialog(
            imageUrl = item.imagenUrl,
            onDismiss = { showImageViewer = false }
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
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

// Función para obtener todos los items del inventario
private fun getAllInventoryItems(): List<InventoryItem> {
    // Aquí se cargarían los datos desde una base de datos o API
    // Por ahora, usamos datos de ejemplo
    return listOf(
        // Datos de ejemplo basados en la imagen del Excel
        InventoryItem(
            codigoMat = "001",
            descripcion = "TACHUELA",
            unidad = "PIEZAS",
            pCompra = 1.00,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZ",
            cantXUnidad = 10,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00101024",
            descripcion = "***CONTRAPESO",
            unidad = "PZAS",
            pCompra = 1.96,
            existencia = -1474.57,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00102014",
            descripcion = "TIRA TROQUELADA",
            unidad = "PZA",
            pCompra = 3.72,
            existencia = -1562.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00146123",
            descripcion = "LLAVE ALLEN",
            unidad = "PZA",
            pCompra = 0.71,
            existencia = 4896.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00150119",
            descripcion = "BISAGRA",
            unidad = "PZAS",
            pCompra = 3.00,
            existencia = 0.0,
            max = 1000,
            min = 200,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00150153",
            descripcion = "BISAGRA",
            unidad = "PZAS",
            pCompra = 5.88,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00176035",
            descripcion = "PERNO UNION",
            unidad = "PZA",
            pCompra = 0.52,
            existencia = 9592.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "E",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00176040",
            descripcion = "PERNO UNION",
            unidad = "PZA",
            pCompra = 0.69,
            existencia = -118.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "E",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00640013",
            descripcion = "CUBRE CANTO",
            unidad = "MTR",
            pCompra = 0.00,
            existencia = -48.3,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "MTR",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "01",
            descripcion = "TACHUELA",
            unidad = "PIEZAS",
            pCompra = 0.00,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "100",
            cantXUnidad = 10,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "010-0041",
            descripcion = "SEPARADOR",
            unidad = "PZAS",
            pCompra = 31.97,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "010-0042",
            descripcion = "SEPARADOR",
            unidad = "PZAS",
            pCompra = 38.36,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "01013011",
            descripcion = "CINTA NEGRA",
            unidad = "PZAS",
            pCompra = 28.50,
            existencia = 0.0,
            max = 100,
            min = 20,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "E",
            borrado = true
        ),
        // Añadir más ejemplos para cubrir todas las categorías
        InventoryItem(
            codigoMat = "CUB001",
            descripcion = "CUBETA DE PINTURA 20L",
            unidad = "PZA",
            pCompra = 350.00,
            existencia = 15.0,
            max = 20,
            min = 5,
            inventarioInicial = 10.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "TEL001",
            descripcion = "TELA PARA TAPIZADO",
            unidad = "MTR",
            pCompra = 120.00,
            existencia = 45.0,
            max = 50,
            min = 10,
            inventarioInicial = 30.0,
            unidadEntrada = "MTR",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "CAS001",
            descripcion = "CASCO DE MUEBLE PINO",
            unidad = "PZA",
            pCompra = 85.00,
            existencia = 12.0,
            max = 15,
            min = 5,
            inventarioInicial = 10.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false,
            imagenUrl = "https://i.postimg.cc/L418PZzS/images.jpg"
        )
    )
}

@Composable
fun ImageViewerDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen Ampliada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

