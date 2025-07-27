package com.example.barsa.Producciones

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.barsa.data.retrofit.models.ListadoProduccion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Bitacora(
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val bitacoraState by userViewModel.bitacoraState.collectAsState()
    val currentPage by userViewModel.currentBitacoraPage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var fechaInicio by remember { mutableStateOf<LocalDate?>(null) }
    var fechaFin by remember { mutableStateOf<LocalDate?>(null) }


    val listState = rememberLazyListState()

    /* Manejo de errores con Toast
    LaunchedEffect(bitacoraState) {
        (bitacoraState as? UserViewModel.BitacoraState.Error)?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            userViewModel.resetBitacoraState()
        }
    }*/

    LaunchedEffect(Unit) {
        userViewModel.getListadoBitacoraProduccion()
    }


    Scaffold(
        bottomBar = {
            if (bitacoraState is UserViewModel.BitacoraState.Success) {
                val successState = bitacoraState as UserViewModel.BitacoraState.Success
                val totalPages = successState.totalPages

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { userViewModel.previousBitacoraPage() },
                        enabled = currentPage > 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior")
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
                            onClick = { userViewModel.getListadoBitacoraProduccion(page = page) },
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
                        onClick = { userViewModel.nextBitacoraPage() },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente")
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

            when (bitacoraState) {
                is UserViewModel.BitacoraState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UserViewModel.BitacoraState.Success -> {
                    val bitacoras = (bitacoraState as UserViewModel.BitacoraState.Success).response

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
                        // Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Folio",
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Usuario",
                                    modifier = Modifier.weight(2f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Movimiento",
                                    modifier = Modifier.weight(2f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Etapa",
                                    modifier = Modifier.weight(2f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Fecha",
                                    modifier = Modifier.weight(2f),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    color = Color.Black
                                )
                            }
                        }

                        // Items
                        items(bitacoras, key = { it.id }) { item ->
                            BitacoraItemRow(item)
                        }
                    }
                }

                is UserViewModel.BitacoraState.Error -> {
                    val errorMessage = (bitacoraState as UserViewModel.BitacoraState.Error).message
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            userViewModel.resetBitacoraState()
                            userViewModel.getListadoBitacoraProduccion()
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
                                userViewModel.getListadoBitacoraProduccion(
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
fun BitacoraItemRow(item: ListadoProduccion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.folio.toString(),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = item.usuario,
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = item.movimiento,
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = item.etapa ?: "",
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = item.fecha,
            modifier = Modifier.weight(2f),
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            color = Color.Black
        )
    }
}

@Composable
fun DatePickerInput(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val calendar = Calendar.getInstance()

    val year = selectedDate?.year ?: calendar.get(Calendar.YEAR)
    val month = selectedDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
    val day = selectedDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        android.app.DatePickerDialog(context, { _, y, m, d ->
            onDateSelected(LocalDate.of(y, m + 1, d))
        }, year, month, day)
    }

    OutlinedTextField(
        value = selectedDate?.format(formatter) ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledContainerColor = Color.Transparent,
            disabledBorderColor = Color.Gray,
            focusedTextColor = Color.Black,
            disabledSupportingTextColor = Color.Black
        )
    )
}

