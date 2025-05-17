package org.dam.tfg.androidapp.data

import android.content.Context
import android.os.Build
import android.util.Log
import org.dam.tfg.androidapp.util.NetworkUtils

/**
 * Singleton para gestionar una única instancia de MongoDBService en toda la aplicación
 */
object MongoDBServiceSingleton {
    private const val TAG = "MongoDBServiceSingleton"
    private var instance: MongoDBService? = null

    /**
     * Obtiene la instancia única de MongoDBService
     */
    fun getInstance(context: Context): MongoDBService {
        return instance ?: synchronized(this) {
            instance ?: createService(context).also {
                instance = it
                Log.d(TAG, "Creada nueva instancia de MongoDBService")
            }
        }
    }

    /**
     * Crea una instancia de MongoDBService con la configuración adecuada
     */
    private fun createService(context: Context): MongoDBService {
        // Verificar si estamos en un emulador
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT

        // Obtener la dirección del servidor MongoDB
        val serverAddress = NetworkUtils.getMongoDBServerAddress(isEmulator)
        Log.d(TAG, "Conectando a MongoDB en: $serverAddress")

        // Crear y devolver el servicio
        return MongoDBService(serverAddress)
    }

    /**
     * Cierra la conexión y libera recursos
     */
    fun closeConnection() {
        instance?.let {
            try {
                // Aquí deberías añadir código para cerrar la conexión si es necesario
                Log.d(TAG, "Conexión a MongoDB cerrada")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cerrar la conexión: ${e.message}")
            }
            instance = null
        }
    }
}
