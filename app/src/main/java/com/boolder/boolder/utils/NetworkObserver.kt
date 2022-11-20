package com.boolder.boolder.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

interface NetworkObserver {
    fun onConnectivityChange(connected: Boolean)
}

class NetworkObserverImpl {

    private var observer: NetworkObserver? = null

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        // Network is available for use (through WIFI or cellular)
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            observer?.onConnectivityChange(true)
        }

        // Network is unavailable for use (through WIFI or cellular)
        override fun onLost(network: Network) {
            super.onLost(network)
            observer?.onConnectivityChange(false)
        }
    }

    fun subscribeOn(context: Context, observer: NetworkObserver) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
        this.observer = observer
    }

}