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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.AppBar
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.models.Formula
import org.dam.tfg.androidapp.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFormulasScreen(
    navController: NavController,
    dataStore: DataStore,
    sessionManager: SessionManager
) {
    val formulas by dataStore.formulas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var selectedFormula by remember { mutableStateOf<Formula?>(null) }

    // Load formulas
    LaunchedEffect(Unit) {
        dataStore.loadFormulas()
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Gestionar Fórmulas",
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
                    contentDescription = "Añadir Fórmula"
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
            if (formulas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay fórmulas",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showAddDialog = true }) {
                        Text("Añadir Fórmula")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(formulas) { formula ->
                        FormulaItem(
                            formula = formula,
                            onEdit = {
                                selectedFormula = formula
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedFormula = formula
                                showDeleteDialog = true
                            },
                            onView = {
                                selectedFormula = formula
                                showViewDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Formula Dialog
    if (showAddDialog) {
        FormulaDialog(
            title = "Añadir Fórmula",
            formula = Formula(),
            onDismiss = { showAddDialog = false },
            onConfirm = { formula ->
                scope.launch {
                    // Encrypt formula if needed
                    val finalFormula = if (formula.formulaEncrypted) {
                        formula.copy(formula = dataStore.encryptFormula(formula.formula))
                    } else {
                        formula
                    }

                    val success = dataStore.addFormula(finalFormula)
                    if (success) {
                        snackbarHostState.showSnackbar("Fórmula añadida correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al añadir fórmula")
                    }
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Formula Dialog
    if (showEditDialog && selectedFormula != null) {
        // If formula is encrypted, decrypt it first for editing
        val editableFormula = if (selectedFormula!!.formulaEncrypted) {
            try {
                val decryptedFormula = dataStore.decryptFormula(
                    selectedFormula!!.formula,
                    sessionManager.getUserType() ?: "user"
                )
                selectedFormula!!.copy(formula = decryptedFormula)
            } catch (e: Exception) {
                selectedFormula!!
            }
        } else {
            selectedFormula!!
        }

        FormulaDialog(
            title = "Editar Fórmula",
            formula = editableFormula,
            onDismiss = { showEditDialog = false },
            onConfirm = { formula ->
                scope.launch {
                    // Encrypt formula if needed
                    val finalFormula = if (formula.formulaEncrypted) {
                        formula.copy(formula = dataStore.encryptFormula(formula.formula))
                    } else {
                        formula
                    }

                    val success = dataStore.updateFormula(finalFormula)
                    if (success) {
                        snackbarHostState.showSnackbar("Fórmula actualizada correctamente")
                    } else {
                        snackbarHostState.showSnackbar("Error al actualizar fórmula")
                    }
                    showEditDialog = false
                }
            }
        )
    }

    // Delete Formula Dialog
    if (showDeleteDialog && selectedFormula != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Fórmula") },
            text = { Text("¿Estás seguro de que quieres eliminar esta fórmula?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = dataStore.deleteFormula(selectedFormula!!.id)
                            if (success) {
                                snackbarHostState.showSnackbar("Fórmula eliminada correctamente")
                            } else {
                                snackbarHostState.showSnackbar("Error al eliminar fórmula")
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

    // View Formula Dialog
    if (showViewDialog && selectedFormula != null) {
        val displayFormula = if (selectedFormula!!.formulaEncrypted) {
            try {
                dataStore.decryptFormula(
                    selectedFormula!!.formula,
                    sessionManager.getUserType() ?: "user"
                )
            } catch (e: Exception) {
                "No se puede mostrar la fórmula: ${e.message}"
            }
        } else {
            selectedFormula!!.formula
        }

        AlertDialog(
            onDismissRequest = { showViewDialog = false },
            title = { Text("Ver Fórmula: ${selectedFormula!!.name}") },
            text = {
                Column {
                    Text("Fórmula:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayFormula,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (selectedFormula!!.variables.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Variables:")
                        Spacer(modifier = Modifier.height(8.dp))

                        selectedFormula!!.variables.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewDialog = false }) {
                    Text("Cerrar")
                }
            },
            dismissButton = null
        )
    }
}

@Composable
fun FormulaItem(
    formula: Formula,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formula.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(onClick = onView) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ver"
                        )
                    }

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

            Text(
                text = "Encriptada: ${if (formula.formulaEncrypted) "Sí" else "No"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Variables: ${formula.variables.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FormulaDialog(
    title: String,
    formula: Formula,
    onDismiss: () -> Unit,
    onConfirm: (Formula) -> Unit
) {
    var name by remember { mutableStateOf(formula.name) }
    var formulaText by remember { mutableStateOf(formula.formula) }
    var isEncrypted by remember { mutableStateOf(formula.formulaEncrypted) }
    var variables by remember { mutableStateOf(formula.variables.map { it.key to it.value }.toMutableList()) }

    var showAddVariableDialog by remember { mutableStateOf(false) }

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
                    value = formulaText,
                    onValueChange = { formulaText = it },
                    label = { Text("Fórmula") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEncrypted,
                        onCheckedChange = { isEncrypted = it }
                    )

                    Text("Encriptar fórmula")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Variables",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (variables.isEmpty()) {
                    Text("No hay variables")
                } else {
                    variables.forEachIndexed { index, (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$key: $value")

                            IconButton(
                                onClick = {
                                    variables.removeAt(index)
                                    variables = variables.toMutableList()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar Variable"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showAddVariableDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir Variable")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || formulaText.isBlank()) {
                        return@Button
                    }

                    onConfirm(
                        Formula(
                            id = formula.id,
                            name = name,
                            formula = formulaText,
                            formulaEncrypted = isEncrypted,
                            variables = variables.associate { it.first to it.second }
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

    if (showAddVariableDialog) {
        AddVariableDialog(
            onDismiss = { showAddVariableDialog = false },
            onConfirm = { key, value ->
                variables = (variables + (key to value)).toMutableList()
                showAddVariableDialog = false
            }
        )
    }
}

@Composable
fun AddVariableDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Variable") },
        text = {
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (key.isBlank()) {
                        return@Button
                    }

                    onConfirm(key, value)
                }
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
