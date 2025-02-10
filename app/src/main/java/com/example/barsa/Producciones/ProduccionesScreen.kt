package com.example.barsa.Producciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ProduccionesScreen() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()

    // Pendiente: Hacer dinamica de acuerdo a la API
    val producciones = listOf(
        Produccion("Folio:1254", "120 unidades"),
        Produccion("Folio:1303", "200 unidades"),
        Produccion("Folio:1421", "80 unidades"),
        Produccion("Folio:1591", "150 unidades"),
        Produccion("Folio:1873", "300 unidades")
    )

    // Filtrar los procesos según la búsqueda
    val filteredProducciones = producciones.filter {
        it.nombre.contains(searchText.text, ignoreCase = true)
    }

    // Barra de búsqueda
    SearchBar(
        query = searchText.text, // Convertimos el TextFieldValue a String
        onQueryChange = { searchText = TextFieldValue(it) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Mostrar los procesos filtrados
        filteredProducciones.forEach { produccion ->
            ProduccionCard(produccion)
        }

        // Si no hay coincidencias
        if (filteredProducciones.isEmpty()) {
            Text(
                text = "No se encontraron procesos.",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProduccionCard(produccion: Produccion) {
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
            modifier = Modifier.padding(16.dp).fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Text(text = produccion.nombre, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cantidad: ${produccion.cantidad}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { /* Acción para más detalles */ },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)) {
                    Text("Ver detalles")
                }
                Button(onClick = { /* Pantalla de tiempos */ },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)) {
                    Text("Tiempos")
                }
            }
        }
    }
}

// Pendiente: Modificar de acuerdo a la API
data class Produccion(val nombre: String, val cantidad: String)

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
}