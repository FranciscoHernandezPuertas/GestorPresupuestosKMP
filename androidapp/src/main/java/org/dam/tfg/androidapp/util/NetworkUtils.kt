package org.dam.tfg.androidapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utilidad para verificar la conectividad de red
 */
object NetworkUtils {

    /**
     * Verifica si el dispositivo tiene conexión a Internet
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Obtiene la dirección IP del servidor MongoDB según el entorno
     */
    fun getMongoDBServerAddress(isEmulator: Boolean): String {
        return if (isEmulator) {
            // 10.0.2.2 es la dirección IP que el emulador de Android usa para acceder a la máquina host
            "mongodb://10.0.2.2:27017"
        } else {
            // Aquí deberías poner la IP real de tu servidor MongoDB
            // Por ejemplo, si MongoDB está en la misma red local: "mongodb://192.168.1.100:27017"
            "mongodb://192.168.1.100:27017"
        }
    }
}
