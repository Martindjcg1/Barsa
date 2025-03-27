package com.example.barsa.Body.Inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.barsa.Models.InventoryItem

@Composable
fun ItemDetailDialog(
    item: InventoryItem,
    onDismiss: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }

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
                        text = "Detalles del Producto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Actualizar la visualización de la imagen en el diálogo para hacerla interactuable
                if (!item.imagenUrl.isNullOrEmpty() || item.imagenesUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar la imagen principal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showImageViewer = true }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (item.imagenesUrls.isNotEmpty()) item.imagenesUrls[0] else item.imagenUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.descripcion,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Si hay múltiples imágenes, mostrar miniaturas
                    if (item.imagenesUrls.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Galería de imágenes (${item.imagenesUrls.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.imagenesUrls.forEachIndexed { index, url ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            showImageViewer = true
                                        }
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(url)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Miniatura ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem("Código", item.codigoMat)
                DetailItem("Descripción", item.descripcion)
                DetailItem("Unidad", item.unidad)
                DetailItem("Precio Compra", "%.2f".format(item.pCompra))
                DetailItem("Existencia", "%.2f".format(item.existencia))
                DetailItem("Máximo", item.max.toString())
                DetailItem("Mínimo", item.min.toString())
                DetailItem("Inventario Inicial", "%.2f".format(item.inventarioInicial))
                DetailItem("Unidad Entrada", item.unidadEntrada)
                DetailItem("Cantidad por Unidad", item.cantXUnidad.toString())
                DetailItem("Proceso", item.proceso)
                DetailItem("Estado", if (item.borrado) "Borrado" else "Activo")
                if (!item.imagenUrl.isNullOrEmpty()) {
                    DetailItem("URL de Imagen", item.imagenUrl)
                }
            }
        }
    }

    // Mostrar el visor de imágenes si se hace clic en la imagen
    if (showImageViewer) {
        if (item.imagenesUrls.isNotEmpty()) {
            ImageViewerDialog(
                imageUrls = item.imagenesUrls,
                onDismiss = { showImageViewer = false }
            )
        } else if (!item.imagenUrl.isNullOrEmpty()) {
            ImageViewerDialog(
                imageUrls = listOf(item.imagenUrl),
                onDismiss = { showImageViewer = false }
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

