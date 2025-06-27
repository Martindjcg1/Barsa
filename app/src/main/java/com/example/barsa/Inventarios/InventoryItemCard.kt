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
import com.example.barsa.Models.InventoryItemfake
import com.example.barsa.data.retrofit.models.InventoryItem


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

            // Mostrar la primera imagen si existe - usar la propiedad computada
            if (item.imagenes.isNotEmpty()) {
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
                            .data(item.imagenes.first()) // Usar la propiedad computada
                            .crossfade(true)
                            .build(),
                        contentDescription = item.descripcion,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    // Indicador de múltiples imágenes
                    if (item.imagenes.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${item.imagenes.size - 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                        text = String.format("%.2f", item.max),
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
                        text = String.format("%.2f", item.min),
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
                        text = String.format("%.2f", item.existencia),
                        style = MaterialTheme.typography.bodyLarge,
                        color = stockColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Información adicional del proceso
            if (item.proceso.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Proceso: ${item.proceso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Añadir botón de reabastecimiento si el stock está bajo o no hay stock
            if (item.existencia < item.min) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Funcionalidad de reabastecimiento */ },
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
    if (showImageViewer && item.imagenes.isNotEmpty()) {
        ImageViewerDialog(
            imageUrls = item.imagenes, // Usar la propiedad computada
            onDismiss = { showImageViewer = false }
        )
    }
}
