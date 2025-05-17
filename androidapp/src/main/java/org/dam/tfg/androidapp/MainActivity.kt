package org.dam.tfg.androidapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_URI
import org.dam.tfg.androidapp.data.MongoDBService
import org.dam.tfg.androidapp.navigation.AppNavigation
import org.dam.tfg.androidapp.ui.theme.AdminPanelTheme
import org.dam.tfg.androidapp.util.NetworkUtils

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar conectividad de red
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_LONG).show()
        }

        // Probar la conexión a MongoDB con una instancia directa
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configurar la dirección del servidor MongoDB
                val serverAddress = if (isEmulator()) {
                    "mongodb://10.0.2.2:27017"
                } else {
                    "mongodb://192.168.1.50:27017"
                }

                Log.d(TAG, "Intentando conectar a: $serverAddress")

                // Intentar una operación simple para probar la conexión
                val isConnected = NetworkUtils.canConnectToMongoDB(serverAddress)

                // Si llegamos aquí, la conexión fue exitosa
                if (isConnected) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Conexión a MongoDB exitosa", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo conectar a MongoDB. Verifica la configuración.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // Si hay un error, mostrar un mensaje detallado
                Log.e(TAG, "Error detallado de conexión a MongoDB: ${e.message}", e)
                e.printStackTrace()

                // Obtener información de red
                val networkInfo = NetworkUtils.getNetworkInfo(this@MainActivity)
                Log.d(TAG, "Información de red: $networkInfo")

                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error de conexión a MongoDB: ${e.message?.take(100)}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        setContent {
            AdminPanelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == android.os.Build.PRODUCT
    }
}
