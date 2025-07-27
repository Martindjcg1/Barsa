package com.example.barsa.Inventarios

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.example.barsa.data.retrofit.ui.UserViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.barsa.Producciones.DatePickerInput
import com.example.barsa.data.retrofit.models.ListadoInventario
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraInventario(
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val bitacoraInventarioState by userViewModel.bitacoraInventarioState.collectAsState()
    val currentPage by userViewModel.currentInventarioPage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf<LocalDate?>(null) }
    var fechaFin by remember { mutableStateOf<LocalDate?>(null) }

    val listState = rememberLazyListState()

    /*LaunchedEffect(bitacoraInventarioState) {
        (bitacoraInventarioState as? UserViewModel.BitacoraInventarioState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            userViewModel.resetBitacoraInventarioState()
        }
    }*/

    LaunchedEffect(Unit) {
        userViewModel.getListadoBitacoraInventario()
    }

    Scaffold(
        bottomBar = {
            if (bitacoraInventarioState is UserViewModel.BitacoraInventarioState.Success) {
                val successState = bitacoraInventarioState as UserViewModel.BitacoraInventarioState.Success
                val totalPages = successState.totalPages

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { userViewModel.previousInventarioPage() },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Anterior")
                    }

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
                            onClick = { userViewModel.getListadoBitacoraInventario(page = page) },
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

                    IconButton(
                        onClick = { userViewModel.nextInventarioPage() },
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
            when (bitacoraInventarioState) {
                is UserViewModel.BitacoraInventarioState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UserViewModel.BitacoraInventarioState.Success -> {
                    val bitacoras = (bitacoraInventarioState as UserViewModel.BitacoraInventarioState.Success).response

                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Filtrar por fechas",
                            tint = Color.Black
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Código", Modifier.weight(2f), fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Descripción", Modifier.weight(3f), fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Movimiento", Modifier.weight(2f), fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Cantidad", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                                Text("Fecha", Modifier.weight(2f), fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                            }
                        }

                        items(bitacoras, key = { it.id }) { item ->
                            BitacoraInventarioItemRow(item)
                        }
                    }
                }

                is UserViewModel.BitacoraInventarioState.Error -> {
                    val errorMessage = (bitacoraInventarioState as UserViewModel.BitacoraInventarioState.Error).message
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            userViewModel.resetBitacoraInventarioState()
                            userViewModel.getListadoBitacoraInventario()
                        }) {
                            androidx.compose.material3.Text("Reintentar")
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Seleccionar rango de fechas", color = Color.Black) },
                    text = {
                        Column {
                            DatePickerInput("Fecha de inicio", fechaInicio) { fechaInicio = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            DatePickerInput("Fecha de fin", fechaFin) { fechaFin = it }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                userViewModel.getListadoBitacoraInventario(
                                    fechaInicio = fechaInicio.toString(),
                                    fechaFin = fechaFin.toString()
                                )
                            },
                            enabled = fechaInicio != null && fechaFin != null
                        ) {
                            Text("Aceptar", color = if (fechaInicio != null && fechaFin != null) Color.Black else Color.Gray)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar", color = Color.Black)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BitacoraInventarioItemRow(item: ListadoInventario) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.codigo, Modifier.weight(2f), fontSize = 14.sp, color = Color.Black)
        Text(item.descripcionCod, Modifier.weight(3f), fontSize = 14.sp, color = Color.Black)
        Text(item.movimiento, Modifier.weight(2f), fontSize = 14.sp, color = Color.Black)
        Text(item.cantidad.toString(), Modifier.weight(1f), fontSize = 14.sp, color = Color.Black, textAlign = TextAlign.End)
        Text(item.fecha, Modifier.weight(2f), fontSize = 14.sp, color = Color.Black, textAlign = TextAlign.End)
    }
}

