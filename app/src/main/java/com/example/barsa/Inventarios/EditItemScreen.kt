package com.example.barsa.Body.Inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

import com.example.barsa.Models.InventoryItemfake
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    item: InventoryItem,
    onCancel: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    inventoryViewModel: InventoryViewModel
) {
    var codigoMat by remember { mutableStateOf(item.codigoMat) }
    var descripcion by remember { mutableStateOf(item.descripcion) }
    var unidad by remember { mutableStateOf(item.unidad) }
    var pCompra by remember { mutableStateOf(item.pcompra.toString()) }
    var existencia by remember { mutableStateOf(item.existencia.toString()) }
    var max by remember { mutableStateOf(item.max.toString()) }
    var min by remember { mutableStateOf(item.min.toString()) }
    var inventarioInicial by remember { mutableStateOf(item.inventarioInicial.toString()) }
    var unidadEntrada by remember { mutableStateOf(item.unidadEntrada) }
    var cantXUnidad by remember { mutableStateOf(item.cantXUnidad.toString()) }
    var proceso by remember { mutableStateOf(item.proceso) }
    var showUnidadDropdown by remember { mutableStateOf(false) }
    var showUnidadEntradaDropdown by remember { mutableStateOf(false) }

    // Opciones para unidades
    val unidadOptions = listOf("PZA", "PZAS", "MTR", "KG", "LT", "M2", "M3")

    // Para manejar imágenes - usar las URLs directamente de item.imagenes (List<String>)
    var existingImages by remember { mutableStateOf(item.imagenes) } // Ya es List<String>
    var newImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    // Observar el estado de actualización
    val updateMaterialState by inventoryViewModel.updateMaterialState.collectAsState()

    // Variables para mostrar mensajes
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            newImages = uris
        }
    }

    // Manejar el resultado de la actualización
    LaunchedEffect(updateMaterialState) {
        when (updateMaterialState) {
            is InventoryViewModel.UpdateMaterialState.Success -> {
                // Simplemente llamar onSave con el item actualizado del response o el original
                val updatedItem = (updateMaterialState as InventoryViewModel.UpdateMaterialState.Success).response.data ?: item
                onSave(updatedItem)
                inventoryViewModel.resetUpdateMaterialState()
            }
            is InventoryViewModel.UpdateMaterialState.Error -> {
                errorMessage = (updateMaterialState as InventoryViewModel.UpdateMaterialState.Error).message
                showErrorDialog = true
            }
            else -> { /* No hacer nada */ }
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Error al actualizar")
                }
            },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Editar Item",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancelar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Código (no editable)
                OutlinedTextField(
                    value = codigoMat,
                    onValueChange = { },
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    enabled = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Unidad y Precio de Compra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unidad (Dropdown)
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unidad,
                            onValueChange = { },
                            label = { Text("Unidad *") },
                            readOnly = true,
                            enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showUnidadDropdown = true },
                                    enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar unidad"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showUnidadDropdown,
                            onDismissRequest = { showUnidadDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            unidadOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        unidad = option
                                        showUnidadDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pCompra,
                        onValueChange = { pCompra = it },
                        label = { Text("Precio Compra") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Existencia y Unidad de Entrada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = existencia,
                        onValueChange = { existencia = it },
                        label = { Text("Existencia") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )

                    // Unidad de Entrada (Dropdown)
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unidadEntrada,
                            onValueChange = { },
                            label = { Text("Unidad Entrada") },
                            readOnly = true,
                            enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showUnidadEntradaDropdown = true },
                                    enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar unidad de entrada"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showUnidadEntradaDropdown,
                            onDismissRequest = { showUnidadEntradaDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            unidadOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        unidadEntrada = option
                                        showUnidadEntradaDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Máximo y Mínimo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = max,
                        onValueChange = { max = it },
                        label = { Text("Máximo") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )

                    OutlinedTextField(
                        value = min,
                        onValueChange = { min = it },
                        label = { Text("Mínimo") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Inventario Inicial y Cantidad por Unidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inventarioInicial,
                        onValueChange = { inventarioInicial = it },
                        label = { Text("Inventario Inicial") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )

                    OutlinedTextField(
                        value = cantXUnidad,
                        onValueChange = { cantXUnidad = it },
                        label = { Text("Cant. por Unidad") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Proceso (Radio buttons)
                Text(
                    text = "Proceso:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                // Primera fila de radio buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = proceso == "M",
                        onClick = { proceso = "M" },
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                    Text("Manufactura (M)", modifier = Modifier.padding(start = 4.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = proceso == "E",
                        onClick = { proceso = "E" },
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                    Text("Ensamble (E)", modifier = Modifier.padding(start = 4.dp))
                }

                // Segunda fila de radio buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = proceso == "T",
                        onClick = { proceso = "T" },
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                    Text("Terminado (T)", modifier = Modifier.padding(start = 4.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = proceso == "P",
                        onClick = { proceso = "P" },
                        enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                    )
                    Text("Preparación (P)", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de imágenes existentes
                if (existingImages.isNotEmpty()) {
                    Text(
                        text = "Imágenes existentes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        existingImages.forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(url)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Imagen $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Botón para eliminar imagen (solo si no está cargando)
                                if (updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading) {
                                    IconButton(
                                        onClick = {
                                            existingImages = existingImages.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.error,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar imagen",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sección para agregar nuevas imágenes
                Text(
                    text = "Agregar nuevas imágenes",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para seleccionar imágenes
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Agregar imágenes"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar Imágenes")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar imágenes seleccionadas
                if (newImages.isNotEmpty()) {
                    Text(
                        text = "${newImages.size} imagen(es) nueva(s) seleccionada(s)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        newImages.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Nueva imagen $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Botón para eliminar imagen (solo si no está cargando)
                                if (updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading) {
                                    IconButton(
                                        onClick = {
                                            newImages = newImages.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.error,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar imagen",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel,
                enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // Validar campos obligatorios
                    if (descripcion.isNotBlank() && unidad.isNotBlank()) {
                        // Llamar a la función de actualización del ViewModel
                        // Solo pasamos las nuevas imágenes, no las existentes
                        inventoryViewModel.updateMaterial(
                            context = context,
                            codigoMat = codigoMat,
                            descripcion = descripcion,
                            unidad = unidad,
                            pcompra = pCompra.toDoubleOrNull() ?: 0.0,
                            existencia = existencia.toDoubleOrNull() ?: 0.0,
                            max = max.toDoubleOrNull() ?: 0.0,
                            min = min.toDoubleOrNull() ?: 0.0,
                            inventarioInicial = inventarioInicial.toDoubleOrNull() ?: 0.0,
                            unidadEntrada = unidadEntrada,
                            cantxunidad = cantXUnidad.toDoubleOrNull() ?: 1.0,
                            proceso = proceso,
                            borrado = item.borrado, // Mantener el estado actual de borrado
                            newImageUris = newImages // Solo las nuevas imágenes
                        )
                    } else {
                        errorMessage = "Por favor complete todos los campos obligatorios"
                        showErrorDialog = true
                    }
                },
                enabled = updateMaterialState !is InventoryViewModel.UpdateMaterialState.Loading
            ) {
                if (updateMaterialState is InventoryViewModel.UpdateMaterialState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Guardar"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (updateMaterialState is InventoryViewModel.UpdateMaterialState.Loading) "Guardando..." else "Guardar Cambios")
            }
        }
    }
}