package com.example.barsa.data.retrofit.repository

import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.NotificationApiService
import com.example.barsa.data.retrofit.models.ApiNotification
import com.example.barsa.data.retrofit.models.UiNotification
import com.example.barsa.data.retrofit.models.toUiNotification


@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService,
    private val tokenManager: TokenManager
) {

    // Cache local para manejar el estado de las notificaciones
    private var cachedNotifications: List<UiNotification> = emptyList()
    private val deletedNotificationIds = mutableSetOf<String>()
    private val readNotificationIds = mutableSetOf<String>()

    suspend fun getNotifications(): Result<List<UiNotification>> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }

            Log.d("NotificationRepository", "Obteniendo notificaciones")

            val response = notificationApiService.getNotifications("Bearer $token")

            if (response.isSuccessful) {
                val apiNotifications: List<ApiNotification> = response.body() ?: emptyList()
                val uiNotifications: List<UiNotification> = apiNotifications
                    .map { apiNotification -> apiNotification.toUiNotification() }
                    .filter { notification -> !deletedNotificationIds.contains(notification.id) }
                    .map { notification ->
                        if (readNotificationIds.contains(notification.id)) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }

                cachedNotifications = uiNotifications
                Log.d("NotificationRepository", "Notificaciones obtenidas: ${uiNotifications.size}")
                Result.success(uiNotifications)
            } else {
                Log.e("NotificationRepository", "Error al obtener notificaciones: ${response.code()}")
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error inesperado al obtener notificaciones", e)
            Result.failure(e)
        }
    }

    // Eliminación local - solo marca como eliminada en memoria
    fun deleteNotificationLocally(notificationId: String): Result<Unit> {
        return try {
            deletedNotificationIds.add(notificationId)
            Log.d("NotificationRepository", "Notificación marcada como eliminada localmente: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error al eliminar notificación localmente", e)
            Result.failure(e)
        }
    }

    // Marcar como leída localmente - solo marca en memoria
    fun markAsReadLocally(notificationId: String): Result<Unit> {
        return try {
            readNotificationIds.add(notificationId)
            Log.d("NotificationRepository", "Notificación marcada como leída localmente: $notificationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error al marcar como leída localmente", e)
            Result.failure(e)
        }
    }

    // Obtener notificaciones desde cache (para actualizaciones rápidas)
    fun getCachedNotifications(): List<UiNotification> {
        return cachedNotifications
            .filter { notification -> !deletedNotificationIds.contains(notification.id) }
            .map { notification ->
                if (readNotificationIds.contains(notification.id)) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
    }

    // Limpiar cache (útil para logout o refresh completo)
    fun clearCache() {
        cachedNotifications = emptyList()
        deletedNotificationIds.clear()
        readNotificationIds.clear()
    }
}
