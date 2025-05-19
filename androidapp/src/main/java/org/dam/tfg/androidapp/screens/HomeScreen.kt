package org.dam.tfg.androidapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.MenuCard
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_URI
import org.dam.tfg.androidapp.data.MongoDBService
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.util.NetworkUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onNavigateToMaterials: () -> Unit,
    onNavigateToFormulas: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Crear una instancia directa de MongoDBService en lugar de usar el singleton
    val mongoDBService = remember { MongoDBService(DATABASE_URI) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var connectionError by remember { mutableStateOf<String?>(null) }

    // Verificar la conexión a MongoDB al cargar la pantalla
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Intentar una operación simple para verificar la conexión
                try {
                    val users = mongoDBService.getAllUsers()
                    connectionError = null
                } catch (e: Exception) {
                    connectionError = "Error de conexión a MongoDB: ${e.message?.take(100)}"
                }
            } catch (e: Exception) {
                connectionError = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Mostrar indicador de carga mientras se verifica la conexión
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verificando conexión a la base de datos...")
                }
            } else if (connectionError != null) {
                // Mostrar error de conexión
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = connectionError!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            connectionError = null
                            coroutineScope.launch {
                                try {
                                    // Intentar una operación simple para verificar la conexión
                                    try {
                                        val users = mongoDBService.getAllUsers()
                                        connectionError = null
                                    } catch (e: Exception) {
                                        connectionError = "Error de conexión a MongoDB: ${e.message?.take(100)}"
                                    }
                                } catch (e: Exception) {
                                    connectionError = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Reintentar")
                    }
                }
            } else {
                // Mostrar contenido principal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenid@, ${user.username}",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Seleccione una opción:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MenuCard(
                            title = "Materiales",
                            icon = Icons.Default.Build,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            onClick = onNavigateToMaterials
                        )

                        MenuCard(
                            title = "Fórmulas",
                            icon = Icons.Default.Check,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            onClick = onNavigateToFormulas
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MenuCard(
                            title = "Usuarios",
                            icon = Icons.Default.AccountCircle,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            onClick = onNavigateToUsers
                        )

                        MenuCard(
                            title = "Presupuestos",
                            icon = Icons.Default.Email,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            onClick = onNavigateToBudgets
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MenuCard(
                        title = "Historial",
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToHistory
                    )
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Está seguro que desea cerrar sesión?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
