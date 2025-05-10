package org.dam.tfg.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.dam.tfg.androidapp.navigation.AppNavigation
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        val sessionManager = SessionManager(this)

        // Initialize data store
        val dataStore = DataStore()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(sessionManager, dataStore)
                }
            }
        }
    }
}
