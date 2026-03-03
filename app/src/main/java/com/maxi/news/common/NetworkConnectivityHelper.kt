package com.maxi.news.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

interface NetworkConnectivityHelper {

    fun hasNetworkConnection(): Boolean
}

class DefaultNetworkConnectivityHelper(
    private val context: Context
) : NetworkConnectivityHelper {

    override fun hasNetworkConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}