package com.example.barsa.Body.Inventory

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerDialog(
    imageUrls: List<String>,
    initialImageIndex: Int = 0,
    onDismiss: () -> Unit
) {
    // Si solo hay una imagen, la mostramos directamente
    if (imageUrls.size == 1) {
        SingleImageViewer(imageUrl = imageUrls[0], onDismiss = onDismiss)
        return
    }

    // Para múltiples imágenes, usamos un pager
    val pagerState = rememberPagerState(initialPage = initialImageIndex) { imageUrls.size }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            // Pager para navegar entre imágenes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageUrl = imageUrls[page],
                    contentDescription = "Imagen ${page + 1} de ${imageUrls.size}"
                )
            }

            // Controles de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón anterior
                IconButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                try {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                } catch (e: Exception) {
                                    Log.e("ImageViewer", "Error al navegar: ${e.message}")
                                }
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Imagen anterior",
                        tint = if (pagerState.currentPage > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                // Indicador de página
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Botón siguiente
                IconButton(
                    onClick = {
                        if (pagerState.currentPage < imageUrls.size - 1) {
                            coroutineScope.launch {
                                try {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } catch (e: Exception) {
                                    Log.e("ImageViewer", "Error al navegar: ${e.message}")
                                }
                            }
                        }
                    },
                    enabled = pagerState.currentPage < imageUrls.size - 1,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Imagen siguiente",
                        tint = if (pagerState.currentPage < imageUrls.size - 1)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            // Botón de cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SingleImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            ZoomableImage(
                imageUrl = imageUrl,
                contentDescription = "Imagen ampliada"
            )

            // Botón de cerrar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String, contentDescription: String) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Imagen con zoom
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        try {
                            // Calcular el nuevo valor de escala
                            val newScale = (scale * zoom).coerceIn(1f, 5f)

                            // Si estamos cerca del valor original (1f), resetear a 1f exactamente
                            if (newScale < 1.05f) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                                return@detectTransformGestures
                            }

                            // Aplicar la nueva escala
                            scale = newScale

                            // Solo permitir desplazamiento si hay zoom
                            if (scale > 1f) {
                                // Calcular límites de desplazamiento basados en el zoom
                                val maxX = (size.width * (scale - 1)) / 2
                                val maxY = (size.height * (scale - 1)) / 2

                                // Aplicar el desplazamiento con límites
                                offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                            } else {
                                // Si no hay zoom, no permitir desplazamiento
                                offsetX = 0f
                                offsetY = 0f
                            }
                        } catch (e: Exception) {
                            // Manejar cualquier excepción y resetear a valores seguros
                            Log.e("ZoomableImage", "Error en zoom: ${e.message}")
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            contentScale = ContentScale.Fit
        )

        // Botón para resetear zoom (visible solo cuando hay zoom)
        AnimatedVisibility(
            visible = scale > 1f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Resetear zoom",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

