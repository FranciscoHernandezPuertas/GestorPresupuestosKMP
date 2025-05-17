package org.dam.tfg.androidapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.data.MongoDBConstants.DATABASE_URI
import org.dam.tfg.androidapp.data.MongoDBService
import org.dam.tfg.androidapp.models.Material
import org.dam.tfg.androidapp.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialScreen(
    materialId: String,
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Crear una instancia directa de MongoDBService en lugar de usar el singleton
    val mongoDBService = remember { MongoDBService(DATABASE_URI) }

    var material by remember { mutableStateOf<Material?>(null) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(materialId != "new") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Load material if editing an existing one
    LaunchedEffect(materialId) {
        if (materialId != "new") {
            try {
                val loadedMaterial = mongoDBService.getMaterialById(materialId)
                if (loadedMaterial != null) {
                    material = loadedMaterial
                    name = loadedMaterial.name
                    price = loadedMaterial.price.toString()
                } else {
                    errorMessage = "Material no encontrado"
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar el material: ${e.message}"
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
                    Text(if (materialId == "new") "Nuevo Material" else "Editar Material")
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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        isError = name.isBlank() && errorMessage != null
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        isError = (price.isBlank() || price.toDoubleOrNull() == null) && errorMessage != null
                    )

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
                            if (name.isBlank()) {
                                errorMessage = "El nombre no puede estar vacío"
                                return@Button
                            }

                            val priceValue = price.toDoubleOrNull()
                            if (priceValue == null) {
                                errorMessage = "El precio debe ser un número válido"
                                return@Button
                            }

                            // Save material
                            coroutineScope.launch {
                                isSaving = true
                                errorMessage = null
                                successMessage = null

                                try {
                                    val materialToSave = Material(
                                        _id = material?._id ?: "",
                                        name = name,
                                        price = priceValue
                                    )

                                    val success = if (materialId == "new") {
                                        mongoDBService.createMaterial(materialToSave, user.username)
                                    } else {
                                        mongoDBService.updateMaterial(materialToSave, user.username)
                                    }

                                    if (success) {
                                        successMessage = if (materialId == "new") {
                                            "Material creado correctamente"
                                        } else {
                                            "Material actualizado correctamente"
                                        }

                                        // Clear form if creating a new material
                                        if (materialId == "new") {
                                            name = ""
                                            price = ""
                                        }
                                    } else {
                                        errorMessage = "No se pudo guardar el material"
                                    }
                                } catch (e: Exception) {
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
                            Text(if (materialId == "new") "Crear Material" else "Actualizar Material")
                        }
                    }
                }
            }
        }
    }
}
