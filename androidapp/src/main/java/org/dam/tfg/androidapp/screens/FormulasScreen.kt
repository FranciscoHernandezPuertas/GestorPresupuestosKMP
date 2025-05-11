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
import org.dam.tfg.androidapp.models.Formula
import org.dam.tfg.androidapp.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulasScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToEditFormula: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val mongoDBService = remember { MongoDBService(DATABASE_URI) }
    
    var formulas by remember { mutableStateOf<List<Formula>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Formula?>(null) }
    
    // Load formulas on first composition
    LaunchedEffect(Unit) {
        loadFormulas(mongoDBService) { newFormulas, error ->
            formulas = newFormulas
            errorMessage = error
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fórmulas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditFormula("new") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Fórmula")
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
                                loadFormulas(mongoDBService) { newFormulas, error ->
                                    formulas = newFormulas
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
            } else if (formulas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay fórmulas disponibles",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Button(
                        onClick = { onNavigateToEditFormula("new") },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
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
                            onEdit = { onNavigateToEditFormula(formula._id) },
                            onDelete = { showDeleteDialog = formula }
                        )
                    }
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Eliminar Fórmula") },
                text = { Text("¿Está seguro que desea eliminar la fórmula '${showDeleteDialog?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val formulaToDelete = showDeleteDialog
                            showDeleteDialog = null
                            
                            if (formulaToDelete != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val success = mongoDBService.deleteFormula(formulaToDelete._id, user.username)
                                        if (success) {
                                            loadFormulas(mongoDBService) { newFormulas, error ->
                                                formulas = newFormulas
                                                errorMessage = error
                                                isLoading = false
                                            }
                                        } else {
                                            errorMessage = "No se pudo eliminar la fórmula"
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
fun FormulaItem(
    formula: Formula,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    text = formula.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Encriptada: ${if (formula.formulaEncrypted) "Sí" else "No"}",
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

private suspend fun loadFormulas(
    mongoDBService: MongoDBService,
    onResult: (List<Formula>, String?) -> Unit
) {
    try {
        val formulas = mongoDBService.getAllFormulas()
        onResult(formulas, null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}
