package com.example.barsa.Body.Inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryScreen(
    categories: List<InventoryCategory>,
    onCancel: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    var codigoMat by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("PZA") }
    var pCompra by remember { mutableStateOf("0.0") }
    var existencia by remember { mutableStateOf("0.0") }
    var max by remember { mutableStateOf("0") }
    var min by remember { mutableStateOf("0") }
    var inventarioInicial by remember { mutableStateOf("0.0") }
    var unidadEntrada by remember { mutableStateOf("PZA") }
    var cantXUnidad by remember { mutableStateOf("1") }
    var proceso by remember { mutableStateOf("M") }
    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnidadDropdown by remember { mutableStateOf(false) }
    var showUnidadEntradaDropdown by remember { mutableStateOf(false) }

    // Opciones para unidades
    val unidadOptions = listOf("PZA", "PZAS", "MTR")

    // Para manejar imágenes
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
        }
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
                text = "Agregar Nuevo Inventario",
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
                // Código y Categoría en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Código
                    OutlinedTextField(
                        value = codigoMat,
                        onValueChange = { codigoMat = it },
                        label = { Text("Código *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Categoría (Dropdown)
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = { },
                            label = { Text("Categoría *") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar categoría"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                            trailingIcon = {
                                IconButton(onClick = { showUnidadDropdown = true }) {
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
                        singleLine = true
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
                        singleLine = true
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
                            trailingIcon = {
                                IconButton(onClick = { showUnidadEntradaDropdown = true }) {
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = min,
                        onValueChange = { min = it },
                        label = { Text("Mínimo") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
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
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cantXUnidad,
                        onValueChange = { cantXUnidad = it },
                        label = { Text("Cant. por Unidad") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
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
                        onClick = { proceso = "M" }
                    )
                    Text("Manufactura (M)", modifier = Modifier.padding(start = 4.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = proceso == "E",
                        onClick = { proceso = "E" }
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
                        onClick = { proceso = "T" }
                    )
                    Text("Terminado (T)", modifier = Modifier.padding(start = 4.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = proceso == "P",
                        onClick = { proceso = "P" }
                    )
                    Text("Preparación (P)", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de imágenes
                Text(
                    text = "Imágenes (Opcional)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para seleccionar imágenes
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle ,
                        contentDescription = "Agregar imágenes"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar Imágenes")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mostrar imágenes seleccionadas
                if (selectedImages.isNotEmpty()) {
                    Text(
                        text = "${selectedImages.size} imagen(es) seleccionada(s)",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedImages.forEachIndexed { index, uri ->
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
                                    contentDescription = "Imagen $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Botón para eliminar imagen
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // Validar campos obligatorios
                    if (codigoMat.isNotBlank() && descripcion.isNotBlank() && unidad.isNotBlank() && selectedCategory != null) {
                        // Crear nuevo item
                        val newItem = InventoryItem(
                            codigoMat = codigoMat,
                            descripcion = descripcion,
                            unidad = unidad,
                            pCompra = pCompra.toDoubleOrNull() ?: 0.0,
                            existencia = existencia.toDoubleOrNull() ?: 0.0,
                            max = max.toIntOrNull() ?: 0,
                            min = min.toIntOrNull() ?: 0,
                            inventarioInicial = inventarioInicial.toDoubleOrNull() ?: 0.0,
                            unidadEntrada = unidadEntrada,
                            cantXUnidad = cantXUnidad.toIntOrNull() ?: 1,
                            proceso = proceso,
                            borrado = false,
                            // Aquí se guardarían las URLs de las imágenes después de subirlas al servidor
                            imagenUrl = if (selectedImages.isNotEmpty()) selectedImages[0].toString() else null,
                            imagenesUrls = selectedImages.map { it.toString() }
                        )
                        onSave(newItem)
                    } else {
                        // Mostrar mensaje de error (en una implementación real)
                        // Por ahora solo imprimimos en consola
                        println("Por favor complete todos los campos obligatorios")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Guardar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}
