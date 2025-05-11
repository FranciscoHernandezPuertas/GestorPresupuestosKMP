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
import org.dam.tfg.androidapp.models.Budget
import org.dam.tfg.androidapp.models.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val mongoDBService = remember { MongoDBService(DATABASE_URI) }

    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Search filters
    var searchUsername by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // Load budgets on first composition
    LaunchedEffect(Unit) {
        loadAllBudgets(mongoDBService) { newBudgets, error ->
            budgets = newBudgets
            errorMessage = error
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search filters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Filtros de búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = searchUsername,
                        onValueChange = { searchUsername = it },
                        label = { Text("Usuario") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Fecha inicio (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("Fecha fin (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                when {
                                    searchUsername.isNotBlank() -> {
                                        loadBudgetsByUsername(mongoDBService, searchUsername) { newBudgets, error ->
                                            budgets = newBudgets
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    }
                                    startDate.isNotBlank() && endDate.isNotBlank() -> {
                                        loadBudgetsByDateRange(mongoDBService, startDate, endDate) { newBudgets, error ->
                                            budgets = newBudgets
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    }
                                    else -> {
                                        loadAllBudgets(mongoDBService) { newBudgets, error ->
                                            budgets = newBudgets
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Buscar")
                    }
                }
            }

            // Budgets list
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
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
                                    loadAllBudgets(mongoDBService) { newBudgets, error ->
                                        budgets = newBudgets
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
                } else if (budgets.isEmpty()) {
                    Text(
                        text = "No hay presupuestos disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(budgets) { budget ->
                            BudgetItem(budget = budget)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItem(budget: Budget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mesa tipo: ${budget.tipo}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Usuario: ${budget.username}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Precio total: ${formatPrice(budget.precioTotal)}€",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Fecha: ${formatDate(budget.fechaCreacion)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary of components
            Text(
                text = "Resumen:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "- ${budget.tramos.size} tramos",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "- ${budget.elementosGenerales.size} elementos generales",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "- ${budget.cubetas.size} cubetas",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "- ${budget.modulos.size} módulos",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatPrice(price: Long): String {
    return String.format("%,d", price)
}

private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        return dateString
    }
}

private suspend fun loadAllBudgets(
    mongoDBService: MongoDBService,
    onResult: (List<Budget>, String?) -> Unit
) {
    try {
        val budgets = mongoDBService.getAllBudgets()
        onResult(budgets, null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}

private suspend fun loadBudgetsByUsername(
    mongoDBService: MongoDBService,
    username: String,
    onResult: (List<Budget>, String?) -> Unit
) {
    try {
        val budgets = mongoDBService.getBudgetsByUsername(username)
        onResult(budgets, null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}

private suspend fun loadBudgetsByDateRange(
    mongoDBService: MongoDBService,
    startDate: String,
    endDate: String,
    onResult: (List<Budget>, String?) -> Unit
) {
    try {
        val budgets = mongoDBService.getBudgetsByDateRange(startDate, endDate)
        onResult(budgets, null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}
