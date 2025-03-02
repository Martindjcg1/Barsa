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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.barsa.Models.Produccion

@Composable
// Recibir onNavigate
fun ProduccionesScreen(onNavigate: (String) -> Unit) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()

    // Pendiente: Hacer dinamica de acuerdo a la API
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
            query = searchText.text, // Convertimos el TextFieldValue a String
            onQueryChange = { searchText = TextFieldValue(it) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Mostrar los procesos filtrados
            filteredProducciones.forEach { produccion ->
                ProduccionCard(produccion, onNavigate)
            }

            // Si no hay coincidencias
            if (filteredProducciones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier.padding(16.dp).fillMaxSize().padding(horizontal = 16.dp)
        ) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.End) {
                Text(text = "Fecha: ${produccion.fecha}", style = MaterialTheme.typography.labelSmall)
            }
            Text(text = "Folio: ${produccion.folio}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cantidad: ${produccion.cantidad}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                /*Button(onClick = { /* Acción para más detalles */ },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)) {
                    Text("Ver detalles")
                }*/
                // Navegar a la vista composable
                Button(onClick = {
                            //onNavigate("cronometro")
                    val route = "cronometro/${produccion.folio}°${produccion.cantidad}°${produccion.fecha}"
                    onNavigate(route)
                                 },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBrown)) {
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
}