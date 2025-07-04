package com.example.barsa.data.retrofit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.data.retrofit.models.UiNotification
import com.example.barsa.data.retrofit.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val notificationsState: StateFlow<NotificationsState> = _notificationsState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    sealed class NotificationsState {
        object Loading : NotificationsState()
        object Empty : NotificationsState()
        data class Success(val notifications: List<UiNotification>) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = NotificationsState.Loading

            repository.getNotifications().fold(
                onSuccess = { notifications ->
                    if (notifications.isEmpty()) {
                        _notificationsState.value = NotificationsState.Empty
                    } else {
                        _notificationsState.value = NotificationsState.Success(notifications)
                        _unreadCount.value = notifications.count { !it.isRead }
                    }
                },
                onFailure = { exception ->
                    _notificationsState.value = NotificationsState.Error(
                        exception.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    // Eliminación local - actualiza el estado inmediatamente
    fun deleteNotification(notificationId: String) {
        repository.deleteNotificationLocally(notificationId).fold(
            onSuccess = {
                // Actualizar estado inmediatamente con cache local
                updateStateFromCache()
            },
            onFailure = { exception ->
                _notificationsState.value = NotificationsState.Error(
                    "Error al eliminar: ${exception.message}"
                )
            }
        )
    }

    // Marcar como leída localmente - actualiza el estado inmediatamente
    fun markAsRead(notificationId: String) {
        repository.markAsReadLocally(notificationId).fold(
            onSuccess = {
                // Actualizar estado inmediatamente con cache local
                updateStateFromCache()
            },
            onFailure = { exception ->
                _notificationsState.value = NotificationsState.Error(
                    "Error al marcar como leída: ${exception.message}"
                )
            }
        )
    }

    // Actualizar estado desde cache local (para cambios inmediatos)
    private fun updateStateFromCache() {
        val cachedNotifications = repository.getCachedNotifications()
        if (cachedNotifications.isEmpty()) {
            _notificationsState.value = NotificationsState.Empty
        } else {
            _notificationsState.value = NotificationsState.Success(cachedNotifications)
            _unreadCount.value = cachedNotifications.count { !it.isRead }
        }
    }

    // Refresh desde servidor
    fun refreshNotifications() {
        loadNotifications()
    }

    // Limpiar cache (útil para logout)
    fun clearCache() {
        repository.clearCache()
        _notificationsState.value = NotificationsState.Loading
        _unreadCount.value = 0
    }
}
