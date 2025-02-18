package com.example.barsa.Network

sealed class NetworkStatus {
    object NoConnection : NetworkStatus()
    data class ConnectedNoInternet(val ipAddress: String?) : NetworkStatus()
    data class ConnectedInternet(val ipAddress: String?) : NetworkStatus()
}