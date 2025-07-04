package com.example.barsa.Producciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
//import com.example.barsa.Models.DetallePapeleta
//import com.example.barsa.Models.Papeleta
import com.example.barsa.R
import com.example.barsa.data.retrofit.models.DetallePapeleta
import com.example.barsa.data.retrofit.models.Papeleta
import com.example.barsa.data.retrofit.ui.PapeletaViewModel

/*
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
                                "selector/${papeleta.Tipold}°${papeleta.Folio}°${papeleta.Fecha}°${papeleta.Status}"
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
}*/
@Composable
fun PapeletaCard(
    papeleta: Papeleta,
    detalles: List<DetallePapeleta>,
    onNavigate: (String) -> Unit,
    papeletaViewModel: PapeletaViewModel
) {
    val accentBrown = Color(0xFF654321)
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Folio: ${papeleta.folio}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = try { papeleta.fecha.substring(0, 10) } catch (e: Exception) { "Fecha inválida" },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Tipo: ${papeleta.tipoId}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Status: ${papeleta.status}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Obs: ${papeleta.observacionGeneral ?: "Sin observaciones"}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(
                        onClick = { showDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(Color.Black)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.detalles), contentDescription = "Ver detalles")
                    }
                    IconButton(
                        onClick = {
                            papeletaViewModel.setDetalleActual(detalles)
                            val route =
                                "selector/${papeleta.tipoId}°${papeleta.folio}°${papeleta.fecha}°${papeleta.status}"
                            onNavigate(route)
                        },
                        colors = IconButtonDefaults.iconButtonColors(Color.Black)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.cronometro), contentDescription = "Tiempos")
                    }
                }
            }
        }
    }
    if (showDialog) {
        DetallePapeletaDialog(detalles) { showDialog = false }
    }
}

@Composable
fun DetallePapeletaDialog(
    detalles: List<DetallePapeleta>,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles de Papeleta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                detalles.forEach { detalle ->
                    Text(
                        text = "Código: ${detalle.codigo}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Producto: ${detalle.descripcionProducto}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Cantidad: ${detalle.cantidad}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Color: ${detalle.nombreColor}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Cliente: ${detalle.nombreCliente}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Observación: ${detalle.observacion}", style = MaterialTheme.typography.bodySmall)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}


@Composable
fun FiltroDropdown(
    onTipoSelected: (String) -> Unit,
    onOrdenSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = !expanded }) { // Alternar en lugar de solo abrir
            Icon(painter = painterResource(id = R.drawable.filtrar), contentDescription = "Filtrar")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Text("Filtrar por Tipo", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            listOf("Todos", "A", "B").forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false // Asegurar que se cierre correctamente
                    }
                )
            }
            Divider()
            Text("Ordenar por Fecha", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            listOf("Recientes", "Antiguas").forEach { orden ->
                DropdownMenuItem(
                    text = { Text(orden) },
                    onClick = {
                        onOrdenSelected(orden)
                        expanded = false // Asegurar que se cierre correctamente
                    }
                )
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(55.dp),
            placeholder = { Text("Buscar por Folio") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentBrown,
                unfocusedBorderColor = lightBrown,
                focusedTextColor = Color.Black
            )
        )
        IconButton(
            onClick = { onQueryChange(query) }, // Puede forzar la búsqueda manual
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = accentBrown
            )
        }
    }
}
