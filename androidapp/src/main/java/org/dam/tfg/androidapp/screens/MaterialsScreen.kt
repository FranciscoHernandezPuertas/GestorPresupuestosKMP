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
import org.dam.tfg.androidapp.repository.ApiRepository
import org.dam.tfg.androidapp.models.Material
import org.dam.tfg.androidapp.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToEditMaterial: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val apiRepository = remember { ApiRepository() }

    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Material?>(null) }

    // Load materials on first composition
    LaunchedEffect(Unit) {
        loadMaterials(apiRepository) { newMaterials, error ->
            materials = newMaterials
            errorMessage = error
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materiales") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditMaterial("new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Material")
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
                                loadMaterials(apiRepository) { newMaterials, error ->
                                    materials = newMaterials
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
            } else if (materials.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay materiales disponibles",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Button(
                        onClick = { onNavigateToEditMaterial("new") },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Añadir Material")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(materials) { material ->
                        MaterialItem(
                            material = material,
                            onEdit = { onNavigateToEditMaterial(material._id) },
                            onDelete = { showDeleteDialog = material }
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar Material") },
                text = { Text("¿Está seguro que desea eliminar el material '${showDeleteDialog?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val materialToDelete = showDeleteDialog
                            showDeleteDialog = null

                            if (materialToDelete != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val success = apiRepository.deleteMaterial(materialToDelete._id)
                                        if (success) {
                                            loadMaterials(apiRepository) { newMaterials, error ->
                                                materials = newMaterials
                                                errorMessage = error
                                                isLoading = false
                                            }
                                        } else {
                                            errorMessage = "No se pudo eliminar el material"
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
fun MaterialItem(
    material: Material,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Mantener el diseño del elemento de la lista sin cambios
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
                    text = material.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Precio: ${material.price}€",
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

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private suspend fun loadMaterials(
    apiRepository: ApiRepository,
    onResult: (List<Material>, String?) -> Unit
) {
    try {
        val materials = apiRepository.getAllMaterials()
        onResult(materials, if (materials.isEmpty()) "No se encontraron materiales" else null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}
