package org.dam.tfg.androidapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.data.MongoDBService
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.util.CryptoUtil
import org.dam.tfg.androidapp.data.MongoDBServiceFactory

private const val TAG = "EditUserScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    userId: String,
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val mongoDBService = remember { MongoDBServiceFactory.createService(context) }

    var userToEdit by remember { mutableStateOf<User?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("admin") }

    var isLoading by remember { mutableStateOf(userId != "new") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Load user if editing an existing one
    LaunchedEffect(userId) {
        if (userId != "new") {
            try {
                Log.d(TAG, "Cargando usuario con ID: $userId")
                val loadedUser = mongoDBService.getUserById(userId)

                if (loadedUser != null) {
                    Log.d(TAG, "Usuario cargado: ${loadedUser.username}")
                    userToEdit = loadedUser
                    username = loadedUser.username
                    userType = loadedUser.type
                } else {
                    Log.e(TAG, "Usuario no encontrado con ID: $userId")
                    errorMessage = "Usuario no encontrado"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar el usuario: ${e.message}", e)
                errorMessage = "Error al cargar el usuario: ${e.message}"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (userId == "new") "Nuevo Usuario" else "Editar Usuario")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nombre de usuario") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        isError = username.isBlank() && errorMessage != null
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña" + if (userId != "new") " (dejar en blanco para no cambiar)" else "") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        isError = userId == "new" && password.isBlank() && errorMessage != null
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        isError = password != confirmPassword && errorMessage != null
                    )

                    // User type selection
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Tipo de usuario",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userType == "admin",
                                onClick = { userType = "admin" }
                            )
                            Text(
                                text = "Administrador",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userType == "user",
                                onClick = { userType = "user" }
                            )
                            Text(
                                text = "Usuario",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (successMessage != null) {
                        Text(
                            text = successMessage!!,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            // Validate inputs
                            if (username.isBlank()) {
                                errorMessage = "El nombre de usuario no puede estar vacío"
                                return@Button
                            }

                            if (userId == "new" && password.isBlank()) {
                                errorMessage = "La contraseña no puede estar vacía"
                                return@Button
                            }

                            if (password.isNotBlank() && password != confirmPassword) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@Button
                            }

                            // Save user
                            coroutineScope.launch {
                                isSaving = true
                                errorMessage = null
                                successMessage = null

                                try {
                                    Log.d(TAG, "Guardando usuario: $username")
                                    // Hash password if provided
                                    val hashedPassword = if (password.isNotBlank()) {
                                        CryptoUtil.hashSHA256(password)
                                    } else {
                                        userToEdit?.password ?: ""
                                    }

                                    val userToSave = User(
                                        _id = userToEdit?._id ?: "",
                                        username = username,
                                        password = hashedPassword,
                                        type = userType
                                    )

                                    val success = if (userId == "new") {
                                        mongoDBService.createUser(userToSave)
                                    } else {
                                        mongoDBService.updateUser(userToSave)
                                    }

                                    if (success) {
                                        Log.d(TAG, "Usuario guardado correctamente")
                                        successMessage = if (userId == "new") {
                                            "Usuario creado correctamente"
                                        } else {
                                            "Usuario actualizado correctamente"
                                        }

                                        // Clear form if creating a new user
                                        if (userId == "new") {
                                            username = ""
                                            password = ""
                                            confirmPassword = ""
                                            userType = "admin"
                                        }
                                    } else {
                                        Log.e(TAG, "No se pudo guardar el usuario")
                                        errorMessage = "No se pudo guardar el usuario"
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al guardar el usuario: ${e.message}", e)
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (userId == "new") "Crear Usuario" else "Actualizar Usuario")
                        }
                    }
                }
            }
        }
    }
}
