package com.example.barsa.data.retrofit.models
import com.google.gson.annotations.SerializedName

data class ApiNotification(
    @SerializedName("_id") val id: String,
    @SerializedName("area") val area: String,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("__v") val version: Int,
    @SerializedName("color") val color: String?,  // Puede ser null
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("etapa") val etapa: String?,
    @SerializedName("existencia") val existencia: Double,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("minimo") val minimo: Int
)

// Modelo para la UI
data class UiNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val area: String,
    val isRead: Boolean = false,
    val priority: NotificationPriority,
    val metadata: NotificationMetadata? = null
)

enum class NotificationType {
    STOCK_LOW,
    STOCK_CRITICAL,
    INVENTORY_ALERT,
    SYSTEM_NOTIFICATION,
    WARNING,
    INFO
}

enum class NotificationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class NotificationMetadata(
    val codigo: String? = null,
    val existencia: Double? = null,
    val minimo: Int? = null,
    val descripcion: String? = null
)

// Función para convertir ApiNotification a UiNotification - CORREGIDA
fun ApiNotification.toUiNotification(): UiNotification {
    // Validación segura del color
    val safeColor = color?.uppercase() ?: "DESCONOCIDO"

    val type = when (safeColor) {
        "ROJO" -> NotificationType.STOCK_CRITICAL
        "NARANJA" -> NotificationType.STOCK_LOW
        "AMARILLO" -> NotificationType.WARNING
        "VERDE" -> NotificationType.INFO
        else -> NotificationType.INVENTORY_ALERT
    }

    val priority = when (safeColor) {
        "ROJO" -> NotificationPriority.CRITICAL
        "NARANJA" -> NotificationPriority.HIGH
        "AMARILLO" -> NotificationPriority.MEDIUM
        "VERDE" -> NotificationPriority.LOW
        else -> NotificationPriority.MEDIUM
    }

    // Validaciones adicionales para campos que pueden ser null o vacíos
    val safeArea = if (area.isBlank()) "Sistema" else area
    val safeMessage = if (mensaje.isBlank()) "Sin mensaje" else mensaje
    val safeFecha = if (fecha.isBlank()) "Sin fecha" else fecha
    val safeCodigo = if (codigo.isBlank()) null else codigo

    return UiNotification(
        id = id,
        title = "Alerta de $safeArea",
        message = safeMessage,
        type = type,
        timestamp = safeFecha,
        area = safeArea,
        priority = priority,
        metadata = NotificationMetadata(
            codigo = safeCodigo,
            existencia = existencia,
            minimo = minimo,
            descripcion = descripcion
        )
    )
}
