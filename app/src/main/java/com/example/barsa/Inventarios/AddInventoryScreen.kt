package com.example.barsa.Body.Inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.barsa.Inventarios.ImageUtils
import com.example.barsa.Models.InventoryCategory
import com.example.barsa.Models.InventoryItemfake
import com.example.barsa.data.retrofit.models.ImageInfo
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.ui.InventoryViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryScreen(
    categories: List<InventoryCategory> = emptyList(),
    onCancel: () -> Unit = {},
    onSave: (InventoryItem) -> Unit,
    inventoryViewModel: InventoryViewModel
) {
    var codigoMat by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var min by remember { mutableStateOf("") }
    var max by remember { mutableStateOf("") }
    var cantXUnidad by remember { mutableStateOf("1") }
    var descripcion by remember { mutableStateOf("") }
    var pCompra by remember { mutableStateOf("") }
    var proceso by remember { mutableStateOf("") }
    var existencia by remember { mutableStateOf("0") }
    var unidad by remember { mutableStateOf("") }
    var inventarioInicial by remember { mutableStateOf("0") }
    var unidadEntrada by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf<InventoryCategory?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnidadDropdown by remember { mutableStateOf(false) }
    var showUnidadEntradaDropdown by remember { mutableStateOf(false) }
    var showProcesoDropdown by remember { mutableStateOf(false) }
    var isProcessingImages by remember { mutableStateOf(false) }
    var showImageLimitWarning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maxImages = 5

    // Estados del ViewModel
    val createMaterialState by inventoryViewModel.createMaterialState.collectAsState()

    // Opciones para dropdowns
    val unidadOptions = listOf("PZA", "PZAS", "MTR", "KG", "LT", "M2", "M3")
    val procesoOptions = listOf("M", "E", "T", "P")

    // Launcher para seleccionar múltiples imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val totalImages = selectedImages.size + uris.size
                if (totalImages > maxImages) {
                    val availableSlots = maxImages - selectedImages.size
                    if (availableSlots > 0) {
                        selectedImages = selectedImages + uris.take(availableSlots)
                    }
                    showImageLimitWarning = true
                } else {
                    selectedImages = selectedImages + uris
                }

                android.util.Log.d("AddInventoryScreen",
                    "Seleccionadas ${selectedImages.size} imagen(es)")
            }
        }
    )

    // Manejar el resultado de crear material
    LaunchedEffect(createMaterialState) {
        when (createMaterialState) {
            is InventoryViewModel.CreateMaterialState.Success -> {
                // Si el servidor devuelve el item creado, usarlo directamente
                val createdItem = (createMaterialState as InventoryViewModel.CreateMaterialState.Success).response.data

                if (createdItem != null) {
                    // Usar el item que devolvió el servidor
                    onSave(createdItem)
                } else {
                    // Si no hay data en la respuesta, crear un item básico para la UI
                    // Nota: Dependiendo de la opción elegida para el modelo, ajustar aquí
                    val newItem = createInventoryItemFromForm(
                        codigoMat = codigoMat,
                        descripcion = descripcion,
                        unidad = unidad,
                        pCompra = pCompra.toDoubleOrNull() ?: 0.0,
                        existencia = existencia.toDoubleOrNull() ?: 0.0,
                        max = max.toDoubleOrNull() ?: 0.0,
                        min = min.toDoubleOrNull() ?: 0.0,
                        inventarioInicial = inventarioInicial.toDoubleOrNull() ?: 0.0,
                        unidadEntrada = unidadEntrada,
                        cantXUnidad = cantXUnidad.toDoubleOrNull() ?: 1.0,
                        proceso = proceso,
                        selectedImages = selectedImages
                    )
                    onSave(newItem)
                }

                inventoryViewModel.resetCreateMaterialState()
            }
            is InventoryViewModel.CreateMaterialState.Error -> {
                isProcessingImages = false
            }
            else -> { /* No hacer nada */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
                text = "Agregar Material",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (onCancel != {}) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar"
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Código Material
                OutlinedTextField(
                    value = codigoMat,
                    onValueChange = { codigoMat = it.uppercase() },
                    label = { Text("Código Material *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ej: ASDASO15") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    placeholder = { Text("Ej: Tachuela Tachonada Escamada") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Unidad y Precio de Compra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unidad (Dropdown)
                    Box(modifier = Modifier.weight(1f)) {
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
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Seleccionar") }
                        )

                        DropdownMenu(
                            expanded = showUnidadDropdown,
                            onDismissRequest = { showUnidadDropdown = false }
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
                        label = { Text("Precio Compra *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("0.00") }
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
                        label = { Text("Existencia *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("0") }
                    )

                    // Unidad de Entrada (Dropdown)
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unidadEntrada,
                            onValueChange = { },
                            label = { Text("Unidad Entrada *") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showUnidadEntradaDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar unidad de entrada"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Seleccionar") }
                        )

                        DropdownMenu(
                            expanded = showUnidadEntradaDropdown,
                            onDismissRequest = { showUnidadEntradaDropdown = false }
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
                        label = { Text("Máximo *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("100") }
                    )

                    OutlinedTextField(
                        value = min,
                        onValueChange = { min = it },
                        label = { Text("Mínimo *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("10") }
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
                        label = { Text("Inventario Inicial *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("0") }
                    )

                    OutlinedTextField(
                        value = cantXUnidad,
                        onValueChange = { cantXUnidad = it },
                        label = { Text("Cant. por Unidad *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("1") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // *** AQUÍ ESTÁ EL DROPDOWN DE PROCESO QUE FALTABA ***
                // Proceso (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = proceso,
                        onValueChange = { },
                        label = { Text("Proceso *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showProcesoDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar proceso"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Seleccionar proceso") }
                    )

                    DropdownMenu(
                        expanded = showProcesoDropdown,
                        onDismissRequest = { showProcesoDropdown = false }
                    ) {
                        procesoOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    proceso = option
                                    showProcesoDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de imágenes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Imágenes (Máx. $maxImages)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (selectedImages.isEmpty()) "Opcional" else "${selectedImages.size}/$maxImages",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedImages.size >= maxImages) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para agregar imágenes
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedImages.size < maxImages
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Agregar Imágenes")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedImages.isEmpty()) "Seleccionar Imágenes"
                        else if (selectedImages.size >= maxImages) "Límite alcanzado"
                        else "Agregar Más Imágenes"
                    )
                }

                // Preview de imágenes seleccionadas
                if (selectedImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages.withIndex().toList()) { (index, uri) ->
                            Box {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Imagen ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                // Botón para eliminar imagen
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filter { it != uri }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar imagen",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Las imágenes se enviarán como archivos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onCancel != {}) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = createMaterialState !is InventoryViewModel.CreateMaterialState.Loading && !isProcessingImages
                ) {
                    Text("Cancelar")
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        isProcessingImages = true

                        android.util.Log.d("AddInventoryScreen", "Iniciando creación de material")
                        android.util.Log.d("AddInventoryScreen", "Código: $codigoMat")
                        android.util.Log.d("AddInventoryScreen", "Descripción: $descripcion")
                        android.util.Log.d("AddInventoryScreen", "Proceso: $proceso")
                        android.util.Log.d("AddInventoryScreen", "Imágenes: ${selectedImages.size}")

                        // Llamar al ViewModel para crear el material con archivos
                        inventoryViewModel.createMaterial(
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
                            imageUris = selectedImages
                        )

                        isProcessingImages = false
                    }
                },
                enabled = codigoMat.isNotEmpty() &&
                        descripcion.isNotEmpty() &&
                        unidad.isNotEmpty() &&
                        pCompra.isNotEmpty() &&
                        existencia.isNotEmpty() &&
                        max.isNotEmpty() &&
                        min.isNotEmpty() &&
                        inventarioInicial.isNotEmpty() &&
                        unidadEntrada.isNotEmpty() &&
                        cantXUnidad.isNotEmpty() &&
                        proceso.isNotEmpty() &&
                        createMaterialState !is InventoryViewModel.CreateMaterialState.Loading &&
                        !isProcessingImages,
                modifier = Modifier.weight(1f)
            ) {
                if (createMaterialState is InventoryViewModel.CreateMaterialState.Loading || isProcessingImages) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isProcessingImages) "Procesando..." else "Guardando...")
                } else {
                    Text("Guardar Material")
                }
            }
        }

        // Mostrar error si existe
        if (createMaterialState is InventoryViewModel.CreateMaterialState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Error al crear material",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (createMaterialState as InventoryViewModel.CreateMaterialState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Advertencia de límite de imágenes
        if (showImageLimitWarning) {
            AlertDialog(
                onDismissRequest = { showImageLimitWarning = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Advertencia",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Límite de imágenes")
                    }
                },
                text = {
                    Text("Solo se pueden agregar máximo $maxImages imágenes. Se han seleccionado las primeras $maxImages imágenes.")
                },
                confirmButton = {
                    Button(onClick = { showImageLimitWarning = false }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

// Función helper para crear InventoryItem desde el formulario
private fun createInventoryItemFromForm(
    codigoMat: String,
    descripcion: String,
    unidad: String,
    pCompra: Double,
    existencia: Double,
    max: Double,
    min: Double,
    inventarioInicial: Double,
    unidadEntrada: String,
    cantXUnidad: Double,
    proceso: String,
    selectedImages: List<Uri>
): InventoryItem {
    return InventoryItem(
        codigoMat = codigoMat,
        imagenesInfo = selectedImages.map { ImageInfo(url = it.toString()) },
        min = min,
        max = max,
        cantXUnidad = cantXUnidad,
        descripcion = descripcion,
        borrado = false,
        pcompra = pCompra,
        proceso = proceso,
        existencia = existencia,
        unidad = unidad,
        inventarioInicial = inventarioInicial,
        unidadEntrada = unidadEntrada
    )
}