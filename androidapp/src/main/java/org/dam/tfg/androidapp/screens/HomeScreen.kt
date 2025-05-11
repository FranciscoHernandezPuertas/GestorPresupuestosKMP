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
import org.dam.tfg.androidapp.components.MenuCard
import org.dam.tfg.androidapp.models.User

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
    var showLogoutDialog by remember { mutableStateOf(false) }
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido, ${user.username}",
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
