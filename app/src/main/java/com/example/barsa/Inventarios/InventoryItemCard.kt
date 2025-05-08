package com.example.barsa.Body.Inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    // Determinar el color basado en el nivel de stock
    val stockColor = when {
        item.existencia >= item.max -> Color(0xFF2196F3) // Azul - Buen stock
        item.existencia >= item.min -> Color(0xFFFFC107) // Amarillo - Stock moderado
        item.existencia > 0 -> Color(0xFFFF9800)         // Naranja - Poco stock
        else -> Color(0xFFF44336)                        // Rojo - Sin stock
    }

    // Determinar el texto de estado del stock
    val stockStatus = when {
        item.existencia >= item.max -> "Buen stock"
        item.existencia >= item.min -> "Stock moderado"
        item.existencia > 0 -> "Stock bajo"
        else -> "Sin stock"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, stockColor) // Borde con el color según nivel de stock
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Indicador de nivel de stock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(stockColor)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stockStatus,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        color = stockColor
                    )
                }
                Column {
                    Text(
                        text = "Existencia",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.existencia.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = stockColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Añadir botón de reabastecimiento si el stock está bajo o no hay stock
            if (item.existencia < item.min) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Sin funcionalidad por ahora */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = stockColor
                    )
                ) {
                    Text(
                        text = "Reabastecer",
                        color = Color.White
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
