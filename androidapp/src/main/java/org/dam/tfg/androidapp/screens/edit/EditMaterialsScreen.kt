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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.AppBar
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.models.Material

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialsScreen(
    navController: NavController,
    dataStore: DataStore
) {
    val materials by dataStore.materials.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<Material?>(null) }

    // Load materials
    LaunchedEffect(Unit) {
        dataStore.loadMaterials()
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Gestionar Materiales",
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
                    contentDescription = "Añadir Material"
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
            if (materials.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay materiales",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showAddDialog = true }) {
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
                            onEdit = {
                                selectedMaterial = material
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedMaterial = material
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Material Dialog
    if (showAddDialog) {
        MaterialDialog(
            title = "Añadir Material",
            material = Material(),
            onDismiss = { showAddDialog = false },
            onConfirm = { material ->
                scope.launch {
                    val success = dataStore.addMaterial(material)
                    if (success) {
                        snackbarHostState.showSnackbar("Material añadido correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al añadir material")
                    }
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Material Dialog
    if (showEditDialog && selectedMaterial != null) {
        MaterialDialog(
            title = "Editar Material",
            material = selectedMaterial!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { material ->
                scope.launch {
                    val success = dataStore.updateMaterial(material)
                    if (success) {
                        snackbarHostState.showSnackbar("Material actualizado correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al actualizar material")
                    }
                    showEditDialog = false
                }
            }
        )
    }

    // Delete Material Dialog
    if (showDeleteDialog && selectedMaterial != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Material") },
            text = { Text("¿Estás seguro de que quieres eliminar este material?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = dataStore.deleteMaterial(selectedMaterial!!.id)
                            if (success) {
                                snackbarHostState.showSnackbar("Material eliminado correctamente")
                            } else {
                                snackbarHostState.showSnackbar("Error al eliminar material")
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
fun MaterialItem(
    material: Material,
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

@Composable
fun MaterialDialog(
    title: String,
    material: Material,
    onDismiss: () -> Unit,
    onConfirm: (Material) -> Unit
) {
    var name by remember { mutableStateOf(material.name) }
    var price by remember { mutableStateOf(material.price.toString()) }
    var priceError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = null
                    },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val priceValue = price.toDouble()
                        if (name.isBlank()) {
                            return@Button
                        }
                        onConfirm(
                            Material(
                                id = material.id,
                                name = name,
                                price = priceValue
                            )
                        )
                    } catch (e: NumberFormatException) {
                        priceError = "Introduce un precio válido"
                    }
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
