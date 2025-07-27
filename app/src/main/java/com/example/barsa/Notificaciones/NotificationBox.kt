package com.example.barsa.Header

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Path

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.barsa.data.retrofit.models.NotificationPriority
import com.example.barsa.data.retrofit.models.NotificationType
import com.example.barsa.data.retrofit.models.UiNotification
import com.example.barsa.data.retrofit.ui.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernNotificationBox(
    notificationsState: NotificationViewModel.NotificationsState,
    unreadCount: Int,
    onDismissNotification: (String) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onCloseNotifications: () -> Unit,
    onRefresh: () -> Unit
) {
    Dialog(
        onDismissRequest = onCloseNotifications,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                ) {
                    // Decorative circles
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-20).dp, y = (-20).dp)
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .offset(x = 280.dp, y = (-10).dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                CircleShape
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Notificaciones",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (unreadCount > 0) {
                                Text(
                                    text = "$unreadCount sin leer",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        Row {
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Actualizar desde servidor",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onCloseNotifications,
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    when (notificationsState) {
                        is NotificationViewModel.NotificationsState.Loading -> {
                            LoadingNotifications()
                        }
                        is NotificationViewModel.NotificationsState.Empty -> {
                            EmptyNotifications()
                        }
                        is NotificationViewModel.NotificationsState.Success -> {
                            Column {
                                // Info sobre acciones locales
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF0F8FF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF667eea),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Las acciones se aplican solo durante esta sesión. Usa 'Actualizar' para obtener nuevas notificaciones.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF4A5568)
                                        )
                                    }
                                }
                                NotificationsList(
                                    // Ordena las notificaciones por timestamp en orden descendente
                                    notifications = notificationsState.notifications.sortedByDescending { it.timestamp },
                                    onDismiss = onDismissNotification,
                                    onMarkAsRead = onMarkAsRead
                                )
                            }
                        }
                        is NotificationViewModel.NotificationsState.Error -> {
                            ErrorNotifications(
                                message = notificationsState.message,
                                onRetry = onRefresh
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF667eea),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando notificaciones...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Color(0xFF667eea).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF667eea)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "¡Todo al día!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            Text(
                text = "No tienes notificaciones pendientes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ErrorNotifications(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Color.Red.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Error al cargar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun NotificationsList(
    notifications: List<UiNotification>,
    onDismiss: (String) -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(notifications) { notification ->
            ModernNotificationItem(
                notification = notification,
                onDismiss = { onDismiss(notification.id) },
                onMarkAsRead = { onMarkAsRead(notification.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernNotificationItem(
    notification: UiNotification,
    onDismiss: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    val priorityColor = when (notification.priority) {
        NotificationPriority.CRITICAL -> Color(0xFFE53E3E)
        NotificationPriority.HIGH -> Color(0xFFED8936)
        NotificationPriority.MEDIUM -> Color(0xFF3182CE)
        NotificationPriority.LOW -> Color(0xFF38A169)
    }
    val typeIcon = when (notification.type) {
        NotificationType.STOCK_CRITICAL -> Icons.Default.Warning
        NotificationType.STOCK_LOW -> Icons.Default.List
        NotificationType.INVENTORY_ALERT -> Icons.Default.List
        NotificationType.SYSTEM_NOTIFICATION -> Icons.Default.Info
        NotificationType.WARNING -> Icons.Default.Warning
        NotificationType.INFO -> Icons.Default.Info
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (notification.isRead) 2.dp else 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                Color.Gray.copy(alpha = 0.05f)
            else
                Color.White
        ),
        border = if (!notification.isRead)
            BorderStroke(2.dp, priorityColor.copy(alpha = 0.3f))
        else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Priority indicator and icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            priorityColor.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        typeIcon,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            modifier = Modifier.weight(1f)
                        )
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(priorityColor, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.area,
                        style = MaterialTheme.typography.labelMedium,
                        color = priorityColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A5568),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Metadata
                    notification.metadata?.let { metadata ->
                        if (metadata.codigo != null || metadata.existencia != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF7FAFC)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    metadata.codigo?.let { codigo ->
                                        Row {
                                            Text(
                                                text = "Código: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = codigo,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF2D3748),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (metadata.existencia != null && metadata.minimo != null) {
                                        Row {
                                            Text(
                                                text = "Stock: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${metadata.existencia.toInt()} / ${metadata.minimo} mín",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = priorityColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Actions - CORREGIDO: Layout mejorado para evitar cortes
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Fecha
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Botones de acción en columna para evitar cortes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!notification.isRead) {
                                Button(
                                    onClick = onMarkAsRead,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF667eea).copy(alpha = 0.1f),
                                        contentColor = Color(0xFF667eea)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Leída",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53E3E).copy(alpha = 0.1f),
                                    contentColor = Color(0xFFE53E3E)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Ocultar",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
