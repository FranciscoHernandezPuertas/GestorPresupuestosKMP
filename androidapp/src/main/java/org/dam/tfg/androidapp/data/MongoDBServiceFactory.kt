package org.dam.tfg.androidapp.data

import android.content.Context
import android.os.Build
import android.util.Log
import org.dam.tfg.androidapp.util.NetworkUtils

/**
 * Factory para crear instancias de MongoDBService con la configuración adecuada
 */
object MongoDBServiceFactory {
    private const val TAG = "MongoDBServiceFactory"

    /**
     * Crea una instancia de MongoDBService con la configuración adecuada para el entorno
     */
    fun createService(context: Context): MongoDBService {
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
        Log.d(TAG, "Creando servicio MongoDB con dirección: $serverAddress")

        // Crear y devolver el servicio
        return MongoDBService(serverAddress)
    }
}
