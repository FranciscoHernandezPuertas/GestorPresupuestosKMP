package org.dam.tfg.androidapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

/**
 * Utilidad para verificar la conectividad de red
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"

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
        val address = if (isEmulator) {
            // 10.0.2.2 es la dirección IP que el emulador de Android usa para acceder a la máquina host
            "mongodb://10.0.2.2:27017/?connectTimeoutMS=30000&socketTimeoutMS=30000&serverSelectionTimeoutMS=30000"
        } else {
            // Usar la IP real de tu servidor MongoDB
            "mongodb://192.168.1.50:27017/?connectTimeoutMS=30000&socketTimeoutMS=30000&serverSelectionTimeoutMS=30000"
        }

        Log.d(TAG, "Dirección del servidor MongoDB: $address")
        return address
    }

    /**
     * Comprueba si se puede conectar al servidor MongoDB
     */
    suspend fun canConnectToMongoDB(mongodbUri: String): Boolean {
        return try {
            // Configuración del cliente con opciones adicionales
            val settings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(mongodbUri))
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(30000, TimeUnit.MILLISECONDS)
                    builder.readTimeout(30000, TimeUnit.MILLISECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(30000, TimeUnit.MILLISECONDS)
                }
                .build()

            // Intentar una conexión de prueba con timeout
            withTimeout(30000) {
                val testClient = MongoClient.create(settings)
                try {
                    val testDb = testClient.listDatabaseNames().toList()
                    Log.d(TAG, "Conexión a MongoDB exitosa: $testDb")
                    true
                } finally {
                    testClient.close()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar con MongoDB: ${e.message}")
            false
        }
    }

    /**
     * Obtiene información detallada sobre la red
     */
    fun getNetworkInfo(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val sb = StringBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                sb.append("Red activa: ${network}\n")
                sb.append("Capacidades: ${capabilities}\n")

                if (capabilities != null) {
                    sb.append("Transporte: ")
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) sb.append("WIFI ")
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) sb.append("CELLULAR ")
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) sb.append("ETHERNET ")
                    sb.append("\n")

                    sb.append("Capacidades: ")
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) sb.append("INTERNET ")
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) sb.append("VALIDATED ")
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) sb.append("NOT_METERED ")
                    sb.append("\n")
                }
            } else {
                sb.append("No hay red activa\n")
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null) {
                @Suppress("DEPRECATION")
                sb.append("Red activa: ${networkInfo.typeName}\n")
                @Suppress("DEPRECATION")
                sb.append("Estado: ${if (networkInfo.isConnected) "Conectado" else "Desconectado"}\n")
            } else {
                sb.append("No hay red activa\n")
            }
        }

        return sb.toString()
    }
}
