package com.example.barsa.Producciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProduccionesScreen(onNavigate: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf("Todos") }
    var selectedOrden by remember { mutableStateOf("Recientes") }
    val listState = rememberLazyListState()

    val papeletas = remember { generarPapeletas() }

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