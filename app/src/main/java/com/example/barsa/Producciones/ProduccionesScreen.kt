package com.example.barsa.Producciones

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.barsa.data.retrofit.ui.PapeletaViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import java.time.LocalDate
import java.time.format.DateTimeFormatter
/*
@Composable
fun ProduccionesScreen(onNavigate: (String) -> Unit, papeletaViewModel: PapeletaViewModel) {
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
 */
@OptIn(FlowPreview::class)
@Composable
fun ProduccionesScreen(
    onNavigate: (String) -> Unit,
    papeletaViewModel: PapeletaViewModel
) {
    var query by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf("Todos") }
    var selectedOrden by remember { mutableStateOf("Recientes") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val papeletaState by papeletaViewModel.papeletaState.collectAsState()

    val currentPage by papeletaViewModel.currentPage.collectAsState()

    /*LaunchedEffect(currentPage) {

        if (papeletaState !is PapeletaViewModel.PapeletaState.Success) {
            papeletaViewModel.getListadoPapeletas(page = currentPage)
        }
    }*/


    LaunchedEffect(papeletaState) {
        (papeletaState as? PapeletaViewModel.PapeletaState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            papeletaViewModel.resetPapeletaState()
        }
    }

    Scaffold(
        bottomBar = {
            if (papeletaState is PapeletaViewModel.PapeletaState.Success) {
                val successState = papeletaState as PapeletaViewModel.PapeletaState.Success
                //val currentPage = successState.currentPage
                val totalPages = successState.totalPages

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón anterior
                    IconButton(
                        onClick = { papeletaViewModel.previousPage() },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Anterior")
                    }

                    // Calcular rango dinámico de páginas a mostrar
                    val maxVisiblePages = 4
                    val halfRange = maxVisiblePages / 2

                    val startPage = when {
                        totalPages <= maxVisiblePages -> 1
                        currentPage <= halfRange -> 1
                        currentPage >= totalPages - halfRange -> totalPages - maxVisiblePages + 1
                        else -> currentPage - halfRange + 1
                    }.coerceAtLeast(1)

                    val endPage = (startPage + maxVisiblePages - 1).coerceAtMost(totalPages)

                    (startPage..endPage).forEach { page ->
                        val isSelected = currentPage == page
                        TextButton(
                            onClick = { papeletaViewModel.getListadoPapeletas(page) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .background(
                                    color = if (isSelected) Color.LightGray else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Text(
                                text = page.toString(),
                                color = Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    // Botón siguiente
                    IconButton(
                        onClick = { papeletaViewModel.nextPage() },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Siguiente")
                    }
                }

            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Papeletas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(18.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*FiltroDropdown(
                    onTipoSelected = { selectedTipo = it },
                    onOrdenSelected = { selectedOrden = it }
                )
                 */
                SearchBar(
                    query = query,
                    onQueryChange = { query = it }
                )

                // Debounce: espera 300ms antes de hacer la búsqueda
                LaunchedEffect(query) {
                    snapshotFlow { query }
                        .debounce(300) // tiempo en milisegundos
                        .collectLatest { folio ->
                            papeletaViewModel.getListadoPapeletas(page = 1, folio = folio)
                        }
                }
            }

            when (papeletaState) {
                is PapeletaViewModel.PapeletaState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PapeletaViewModel.PapeletaState.Success -> {
                    val papeletas = (papeletaState as PapeletaViewModel.PapeletaState.Success).response

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(papeletas, key = { it.folio }) { papeleta ->
                            PapeletaCard(papeleta, papeleta.detallepapeleta, onNavigate, papeletaViewModel)
                        }
                    }
                }

                is PapeletaViewModel.PapeletaState.Error -> {
                    /*val message = (papeletaState as PapeletaViewModel.PapeletaState.Error).message
                    LaunchedEffect(message) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }*/
                }
            }
        }
    }
}







