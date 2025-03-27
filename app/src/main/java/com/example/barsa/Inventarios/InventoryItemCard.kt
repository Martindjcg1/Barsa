package com.example.barsa.Body.Inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.barsa.Models.InventoryItem

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    var showImageViewer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Actualizar la visualización de la imagen para hacerla interactuable
            if (!item.imagenUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageViewer = true }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imagenUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.descripcion,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.codigoMat,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.unidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Máximo",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.max.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "Mínimo",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.min.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.existencia < item.min) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "Cant/Unidad",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.cantXUnidad.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
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

