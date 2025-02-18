package com.example.barsa.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkMonitor(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus

    private val networkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkNetworkStatus(network)
        }

        override fun onLost(network: Network) {
            Log.d("NetworkMonitor", "Sin conexión a ninguna red")
            _networkStatus.postValue(NetworkStatus.NoConnection)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkNetworkStatus(network)
        }
    }

    private fun checkNetworkStatus(network: Network) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        val isValidated = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
        val ipAddress = getLocalIpAddress()

        val status = when {
            isValidated -> {
                Log.d("NetworkMonitor", "Conectado con acceso a Internet. IP local: $ipAddress")
                NetworkStatus.ConnectedInternet(ipAddress)
            }
            hasInternet -> {
                Log.d("NetworkMonitor", "Conectado a una red, pero sin Internet. IP local: $ipAddress")
                NetworkStatus.ConnectedNoInternet(ipAddress)
            }
            else -> {
                Log.d("NetworkMonitor", "Sin conexión a ninguna red")
                NetworkStatus.NoConnection
            }
        }

        _networkStatus.postValue(status)
    }

    private fun getLocalIpAddress(): String? {
        val ipInt = wifiManager.connectionInfo.ipAddress
        return if (ipInt == 0) null else Formatter.formatIpAddress(ipInt)
    }

    fun register() {
        val networkRequest = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
