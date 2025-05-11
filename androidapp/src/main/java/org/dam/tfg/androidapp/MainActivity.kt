package org.dam.tfg.androidapp

import android.os.Bundle
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
import org.dam.tfg.androidapp.data.MongoDBServiceFactory
import org.dam.tfg.androidapp.navigation.AppNavigation
import org.dam.tfg.androidapp.ui.theme.AdminPanelTheme
import org.dam.tfg.androidapp.util.NetworkUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar conectividad de red
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_LONG).show()
        }

        // Inicializar el servicio MongoDB
        val mongoDBService = MongoDBServiceFactory.createService(this)

        // Probar la conexión a MongoDB
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intentar una operación simple para probar la conexión
                val users = mongoDBService.getAllUsers()

                // Si llegamos aquí, la conexión fue exitosa
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Conexión a MongoDB exitosa", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Si hay un error, mostrar un mensaje
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error de conexión a MongoDB: ${e.message}",
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
}
