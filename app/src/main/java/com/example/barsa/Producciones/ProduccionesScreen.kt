package com.example.barsa.Producciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.barsa.Models.CantidadDetalle
import com.example.barsa.Models.Cliente
import com.example.barsa.Models.ClienteDetalle
//import com.example.barsa.Models.Data
import com.example.barsa.Models.DetallePapeleta
import com.example.barsa.Models.Papeleta
import com.example.barsa.Models.PapeletaModels
import com.example.barsa.Models.Produccion
import com.example.barsa.Models.Producto
import com.example.barsa.Models.ProductoDetalle
import com.example.barsa.Models.color
import com.example.barsa.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
@Composable
fun ProduccionesScreen(onNavigate: (String) -> Unit) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Pendiente: Hacer dinámica de acuerdo a la API
    val producciones = listOf(
        Produccion("1254", 120, "20/02/2025"),
        Produccion("2303", 80, "23/12/2024"),
        Produccion("2421", 50, "07/10/2024"),
        Produccion("1591", 150, "01/02/2025"),
        Produccion("4873", 30, "17/02/2025")
    )

    // Filtrar los procesos según la búsqueda
    val filteredProducciones = producciones.filter {
        it.folio.contains(searchText.text, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Producción",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Barra de búsqueda
        SearchBar(
            query = searchText.text,
            onQueryChange = { searchText = TextFieldValue(it) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducciones) { produccion ->
                ProduccionCard(produccion, onNavigate)
            }

            if (filteredProducciones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron procesos.",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProduccionCard(produccion: Produccion, onNavigate: (String) -> Unit) {
    val accentBrown = Color(0xFF654321)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = "Fecha: ${produccion.fecha}", style = MaterialTheme.typography.labelSmall)
            }
            Text(text = "Folio: ${produccion.folio}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cantidad: ${produccion.cantidad}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        val route = "cronometro/${produccion.folio}°${produccion.cantidad}°${produccion.fecha}"
                        onNavigate(route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)
                ) {
                    Text("Tiempos")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val lightBrown = Color(0xFFDEB887)
    val accentBrown = Color(0xFF654321)
    OutlinedTextField(
        value = query,
        onValueChange = { onQueryChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Buscar") },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = accentBrown,
            unfocusedBorderColor = lightBrown,
        ),
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
        }
    )
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduccionesScreen(onNavigate: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf("Todos") }
    var selectedOrden by remember { mutableStateOf("Recientes") }
    val listState = rememberLazyListState()

    /*
    Llenado de datos con antiguo data class
    // 150 datos simulados
    val statusValues = listOf("S", "P", "A", "B")
    val papeletas = remember {
        PapeletaModels(
            totalItems = 150,
            totalPages = 15,
            currentPage = 1,
            data = List(150) { index ->
                val tipoId = when {
                    index < 50 -> "A"
                    index < 100 -> "B"
                    else -> "C"
                }
                val status = statusValues[index % statusValues.size]
                Data(
                    tipoId,
                    index + 1000,
                    "${(index % 28 + 1).toString().padStart(2, '0')}-${
                        (index % 12 + 1).toString().padStart(2, '0')}-2025",
                    status,
                    "loremipsum${index + 1}"
                )
            }
        )
    }

    val productos = listOf("ABD110", "BCD220", "CDE330", "DEF440")
    val colores = listOf("NEGRO", "NOGAL", "TABACO", "Avellana")
    val descripcion = listOf("ARMARIO BARSA MOD. DAVOZ 110", "ALACENA BARSA MOD. DAVOZ 60CM 4/P", "ANTECOMEDOR BARSA VENECIA 4/SILLAS", "ARMARIO BARSA MOD. SMART 2020")
    val clientes = listOf("Cliente A", "Cliente B", "Cliente C", "Cliente D")

    val detallesPapeletas = remember {
        papeletas.data.map { papeleta ->
            DetallePapeleta(
                codigo = productos.random(),
                descripcion = descripcion.random(),
                color = colores.random(),
                Tipold = papeleta.Tipold,
                Folio = papeleta.Folio,
                Fecha = papeleta.Fecha,
                Status = papeleta.Status,
                cantidad = (1..100).random(),
                cliente = clientes.random()
            )
        }
    }*/

    /*
    val statusValues = listOf("S", "P", "A", "B")
    val productos = listOf("ABD110", "BCD220", "CDE330", "DEF440")
    val colores = listOf("NEGRO", "NOGAL", "TABACO", "Avellana")
    val descripcion = listOf(
        "ARMARIO BARSA MOD. DAVOZ 110",
        "ALACENA BARSA MOD. DAVOZ 60CM 4/P",
        "ANTECOMEDOR BARSA VENECIA 4/SILLAS",
        "ARMARIO BARSA MOD. SMART 2020"
    )
    val clientes = listOf("Cliente A", "Cliente B", "Cliente C", "Cliente D")

    val papeletas = remember {
        List(150) { index ->
            val tipoId = when {
                index < 50 -> "A"
                index < 100 -> "B"
                else -> "C"
            }
            val status = statusValues[index % statusValues.size]

            val detalles = List((1..5).random()) { // Generar entre 1 y 5 detalles por papeleta
                DetallePapeleta(
                    codigo = productos.random(),
                    descripcion = descripcion.random(),
                    color = colores.random(),
                    Tipold = tipoId,
                    Folio = index + 1000,
                    Fecha = "${(index % 28 + 1).toString().padStart(2, '0')}-${(index % 12 + 1).toString().padStart(2, '0')}-2025",
                    Status = status,
                    cantidad = (1..100).random(),
                    cliente = clientes.random()
                )
            }

            Data(
                Tipold = tipoId,
                Folio = index + 1000,
                Fecha = "${(index % 28 + 1).toString().padStart(2, '0')}-${(index % 12 + 1).toString().padStart(2, '0')}-2025",
                Status = status,
                ObservacionGeneral = "loremipsum${index + 1}",
                detallePapeleta = detalles // Asignar los detalles directamente a la papeleta
            )
        }
    }*/

    val statusValues = listOf("S", "P", "A", "B")

// Generación de valores de ejemplo para productos, colores y clientes
    val productos = listOf(
        Producto(codigo = "ABD110", descripcion = "ARMARIO BARSA MOD. DAVOZ 110"),
        Producto(codigo = "BCD220", descripcion = "ALACENA BARSA MOD. DAVOZ 60CM 4/P"),
        Producto(codigo = "CDE330", descripcion = "ANTECOMEDOR BARSA VENECIA 4/SILLAS"),
        Producto(codigo = "DEF440", descripcion = "ARMARIO BARSA MOD. SMART 2020")
    )

    val colores = listOf(
        color(id = 1, nombre = "NEGRO"),
        color(id = 2, nombre = "NOGAL"),
        color(id = 3, nombre = "TABACO"),
        color(id = 4, nombre = "Avellana")
    )

    val clientes = listOf(
        Cliente(id = 1, nombre = "Cliente A"),
        Cliente(id = 2, nombre = "Cliente B"),
        Cliente(id = 3, nombre = "Cliente C"),
        Cliente(id = 4, nombre = "Cliente D")
    )

// Lista de observaciones asociadas a productos
    val observaciones = listOf(
        "TELA DUBLIN BEIGE",
        "TELA TERCIOPELO",
        "MELAMINA MONARCA",
        "TELA OSLO"
    )

    val papeletas = remember {
        List(150) { index ->
            val tipoId = when {
                index < 50 -> "A"
                index < 100 -> "B"
                else -> "C"
            }

            val status = statusValues[index % statusValues.size]

            // Generar los detalles de una papeleta
            val detalles = List((1..5).random()) { // Cada papeleta tiene de 1 a 5 detalles
                val detalleProductos = productos.shuffled().mapIndexed { idx, producto ->
                    ProductoDetalle(
                        producto = producto,
                        color = colores.shuffled().first(), // Seleccionar un único color para el producto
                        observaciones = listOf(observaciones[idx % observaciones.size]) // Solo una observación por producto
                    )
                }
                val detalleClientes = clientes.shuffled().map { cliente ->
                    ClienteDetalle(
                        cliente = cliente,
                        cantidades = List((1..3).random()) { // Generar de 1 a 3 cantidades por cliente
                            CantidadDetalle(
                                cantidad = (1..100).random(),
                                surtida = (0..1).random(),
                                backOrder = (1000..9999).random()
                            )
                        }
                    )
                }

                DetallePapeleta(
                    productos = detalleProductos,
                    clientes = detalleClientes
                )
            }

            Papeleta(
                Tipold = tipoId,
                Folio = index + 1000,
                Fecha = "${(index % 28 + 1).toString().padStart(2, '0')}-${(index % 12 + 1).toString().padStart(2, '0')}-2025",
                Status = status,
                ObservacionGeneral = "Observación General ${index + 1}",
                detalles = detalles
            )
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    val filteredPapeletas by remember(query, selectedTipo, selectedOrden, papeletas) {
        derivedStateOf {
            papeletas
                .filter { papeleta ->
                    // Filtrar por Folio y TipoId
                    papeleta.Folio.toString().contains(query, ignoreCase = true) &&
                            (selectedTipo == "Todos" || papeleta.Tipold == selectedTipo)
                }
                .sortedBy { LocalDate.parse(it.Fecha, dateFormatter) } // Ordenar por fecha
                .let { if (selectedOrden == "Recientes") it.reversed() else it } // Invertir si es reciente
        }
    }

    // Resetear la posición al cambiar filtros
    LaunchedEffect(selectedTipo, selectedOrden) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Papeletas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(18.dp)
        )

        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FiltroDropdown(
                selectedTipo = selectedTipo,
                selectedOrden = selectedOrden,
                onTipoSelected = { selectedTipo = it },
                onOrdenSelected = { selectedOrden = it }
            )
            SearchBar(
                query = query,
                onQueryChange = { query = it }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredPapeletas,
                key = { it.Folio }
            ) { papeleta ->
                // Pasar la lista completa de detalles (papeleta.detalles)
                PapeletaCard(papeleta, papeleta.detalles, onNavigate)
            }

            if (filteredPapeletas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron papeletas.",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun PapeletaCard(
    papeleta: Papeleta,
    detalles: List<DetallePapeleta>,
    onNavigate: (String) -> Unit
) {
    val accentBrown = Color(0xFF654321)
    var showDialog by remember { mutableStateOf(false) }
    var selectedDetalle by remember { mutableStateOf<DetallePapeleta?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Información general de la papeleta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Folio: ${papeleta.Folio}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = papeleta.Fecha,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tipo: ${papeleta.Tipold}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Status: ${papeleta.Status}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Obs: ${papeleta.ObservacionGeneral}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(
                        onClick = {
                            // Seleccionar el primer detalle para mostrar en el diálogo
                            selectedDetalle = detalles.firstOrNull()
                            showDialog = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = accentBrown)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.detalles),
                            contentDescription = "Detalle"
                        )
                    }
                    IconButton(
                        onClick = {
                            val route =
                                "cronometro/${papeleta.Tipold}°${papeleta.Folio}°${papeleta.Fecha}°${papeleta.Status}"
                            onNavigate(route)
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = accentBrown)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cronometro),
                            contentDescription = "Tiempos"
                        )
                    }
                }
            }
        }
    }
    if (showDialog && selectedDetalle != null) {
        DetallePapeletaDialog(selectedDetalle!!) { showDialog = false }
    }
}

@Composable
fun DetallePapeletaDialog(
    detalle: DetallePapeleta,
    onDismiss: () -> Unit
) {
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
                // Encabezado del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle de Papeleta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar información por producto incluyendo clientes y cantidades
                detalle.productos.forEach { productoDetalle ->
                    // Información del producto
                    Text(
                        text = "Producto: ${productoDetalle.producto.codigo}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Descripción: ${productoDetalle.producto.descripcion}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Observación: ${productoDetalle.observaciones.firstOrNull() ?: "Sin observación"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Color: ${productoDetalle.color.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clientes y cantidades asociadas al producto
                    Text(
                        text = "Clientes y Cantidades:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    detalle.clientes.forEach { clienteDetalle ->
                        Text(
                            text = "- Cliente: ${clienteDetalle.cliente.nombre}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        clienteDetalle.cantidades.forEach { cantidadDetalle ->
                            Text(
                                text = "    - Cantidad: ${cantidadDetalle.cantidad} (Surtida: ${if (cantidadDetalle.surtida == 1) "Sí" else "No"}, BackOrder: ${cantidadDetalle.backOrder})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DetalleItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val lightBrown = Color(0xFFDEB887)
    val accentBrown = Color(0xFF654321)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .height(55.dp)
            .width(120.dp),
        placeholder = { Text("Folio") },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = accentBrown,
            unfocusedBorderColor = lightBrown,
        ),
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
        }
    )
}

@Composable
fun FiltroDropdown(
    selectedTipo: String,
    selectedOrden: String,
    onTipoSelected: (String) -> Unit,
    onOrdenSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painter = painterResource(id = R.drawable.filtrar), contentDescription = "Filtrar")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Sección de Tipos
            Text("Filtrar por Tipo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            listOf("Todos", "A", "B", "C").forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
            Divider()
            // Sección de Orden
            Text("Ordenar por Fecha", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            listOf("Recientes", "Antiguas").forEach { orden ->
                DropdownMenuItem(
                    text = { Text(orden) },
                    onClick = {
                        onOrdenSelected(orden)
                        expanded = false
                    }
                )
            }
        }
    }
}





/* Barra de búsqueda con funcionalidad completa
SearchBar(
query = query,
onQueryChange = { query = it },
onSearch = { active = false }, // Cierra el estado activo al buscar
active = active,
onActiveChange = { active = it },
modifier = Modifier
.fillMaxWidth()
.padding(bottom = 8.dp),
placeholder = { Text(text = "Buscar por folio") },
leadingIcon = {
    Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Buscar"
    )
},
trailingIcon = {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Cerrar",
        modifier = Modifier.clickable {
            query = "" // Limpia el texto de búsqueda
            active = false // Cierra la barra
            onNavigate("producciones")
        }
    )
}
) {
    // Espacio para sugerencias (opcional)
    if (query.isNotEmpty()) {
        Text(
            text = "Resultados para: $query",
            modifier = Modifier.padding(8.dp)
        )
    }
}*/
