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
import com.example.barsa.data.retrofit.models.InventoryItem


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

                // Mostrar imágenes si existen - usar la propiedad computada
                if (item.imagenes.isNotEmpty()) {
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
                                .data(item.imagenes.first()) // Usar la propiedad computada
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
                    if (item.imagenes.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Galería de imágenes (${item.imagenes.size})",
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
                            item.imagenes.forEachIndexed { index, url ->
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

                DetailItem("Código", item.codigoMatSafe)
                DetailItem("Descripción", item.descripcionSafe)
                DetailItem("Unidad", item.unidadSafe)
                DetailItem("Precio Compra", String.format("%.2f", item.pcompraSafe))
                DetailItem("Existencia", String.format("%.2f", item.existenciaSafe))
                DetailItem("Máximo", String.format("%.2f", item.max))
                DetailItem("Mínimo", String.format("%.2f", item.min))
                DetailItem("Inventario Inicial", String.format("%.2f", item.inventarioInicialSafe))
                DetailItem("Unidad Entrada", item.unidadEntradaSafe)
                DetailItem("Cantidad por Unidad", String.format("%.2f", item.cantXUnidad))
                DetailItem("Proceso", item.procesoSafe)
                DetailItem("Estado", if (item.borradoSafe) "Borrado" else "Activo")
                if (item.imagenes.isNotEmpty()) {
                    DetailItem("Imágenes", "${item.imagenes.size} imagen(es)")
                }
            }
        }
    }

    // Mostrar el visor de imágenes si se hace clic en la imagen
    if (showImageViewer && item.imagenes.isNotEmpty()) {
        ImageViewerDialog(
            imageUrls = item.imagenes, // Usar la propiedad computada
            onDismiss = { showImageViewer = false }
        )
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
