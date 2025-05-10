package org.dam.tfg.androidapp.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.AppBar
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsersScreen(
    navController: NavController,
    dataStore: DataStore,
    sessionManager: SessionManager
) {
    val users by dataStore.users.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    
    // Load users
    LaunchedEffect(Unit) {
        dataStore.loadUsers()
    }
    
    Scaffold(
        topBar = {
            AppBar(
                title = "Gestionar Usuarios",
                onMenuClick = {
                    navController.popBackStack()
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Usuario"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (users.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay usuarios",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = { showAddDialog = true }) {
                        Text("Añadir Usuario")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(users) { user ->
                        UserItem(
                            user = user,
                            onEdit = {
                                selectedUser = user
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedUser = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add User Dialog
    if (showAddDialog) {
        UserDialog(
            title = "Añadir Usuario",
            user = User(),
            sessionManager = sessionManager,
            onDismiss = { showAddDialog = false },
            onConfirm = { user ->
                scope.launch {
                    val success = dataStore.addUser(user)
                    if (success) {
                        snackbarHostState.showSnackbar("Usuario añadido correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al añadir usuario")
                    }
                    showAddDialog = false
                }
            }
        )
    }
    
    // Edit User Dialog
    if (showEditDialog && selectedUser != null) {
        UserDialog(
            title = "Editar Usuario",
            user = selectedUser!!,
            sessionManager = sessionManager,
            onDismiss = { showEditDialog = false },
            onConfirm = { user ->
                scope.launch {
                    val success = dataStore.updateUser(user)
                    if (success) {
                        snackbarHostState.showSnackbar("Usuario actualizado correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al actualizar usuario")
                    }
                    showEditDialog = false
                }
            }
        )
    }
    
    // Delete User Dialog
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de que quieres eliminar este usuario?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = dataStore.deleteUser(selectedUser!!.id)
                            if (success) {
                                snackbarHostState.showSnackbar("Usuario eliminado correctamente")
                            } else {
                                snackbarHostState.showSnackbar("Error al eliminar usuario")
                            }
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
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
                        contentDescription = "Editar"
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDialog(
    title: String,
    user: User,
    sessionManager: SessionManager,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf(user.type) }
    var expanded by remember { mutableStateOf(false) }
    
    val userTypes = listOf("admin", "user")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = userType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de usuario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    userType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isBlank()) {
                        return@Button
                    }
                    
                    // Hash password if provided, otherwise keep the existing one
                    val finalPassword = if (password.isNotBlank()) {
                        sessionManager.hashPassword(password)
                    } else {
                        user.password
                    }
                    
                    onConfirm(
                        User(
                            id = user.id,
                            username = username,
                            password = finalPassword,
                            type = userType
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
