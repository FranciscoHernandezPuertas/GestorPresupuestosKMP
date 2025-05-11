package org.dam.tfg.androidapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_URI
import org.dam.tfg.androidapp.data.MongoDBService
import org.dam.tfg.androidapp.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToEditUser: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val mongoDBService = remember { MongoDBService(DATABASE_URI) }
    
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }
    
    // Load users on first composition
    LaunchedEffect(Unit) {
        loadUsers(mongoDBService) { newUsers, error ->
            users = newUsers
            errorMessage = error
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditUser("new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Usuario")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                loadUsers(mongoDBService) { newUsers, error ->
                                    users = newUsers
                                    errorMessage = error
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            } else if (users.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay usuarios disponibles",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Button(
                        onClick = { onNavigateToEditUser("new") },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Añadir Usuario")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(users) { userItem ->
                        UserItem(
                            user = userItem,
                            onEdit = { onNavigateToEditUser(userItem._id) },
                            onDelete = { 
                                // Don't allow deleting the current user
                                if (userItem._id != user._id) {
                                    showDeleteDialog = userItem
                                }
                            },
                            isCurrentUser = userItem._id == user._id
                        )
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar Usuario") },
                text = { Text("¿Está seguro que desea eliminar el usuario '${showDeleteDialog?.username}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val userToDelete = showDeleteDialog
                            showDeleteDialog = null
                            
                            if (userToDelete != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val success = mongoDBService.deleteUser(userToDelete._id, user.username)
                                        if (success) {
                                            loadUsers(mongoDBService) { newUsers, error ->
                                                users = newUsers
                                                errorMessage = error
                                                isLoading = false
                                            }
                                        } else {
                                            errorMessage = "No se pudo eliminar el usuario"
                                            isLoading = false
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username + if (isCurrentUser) " (Tú)" else "",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Tipo: ${user.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    enabled = !isCurrentUser
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = if (isCurrentUser) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private suspend fun loadUsers(
    mongoDBService: MongoDBService,
    onResult: (List<User>, String?) -> Unit
) {
    try {
        val users = mongoDBService.getAllUsers()
        onResult(users, null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}
