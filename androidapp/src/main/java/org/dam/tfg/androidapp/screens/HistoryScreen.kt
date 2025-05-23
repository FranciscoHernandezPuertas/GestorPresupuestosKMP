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
import org.dam.tfg.androidapp.models.History
import org.dam.tfg.androidapp.models.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val apiRepository = remember { ApiRepository() }

    var historyList by remember { mutableStateOf<List<History>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Search filters
    var searchUsername by remember { mutableStateOf("") }
    var searchAction by remember { mutableStateOf("") }

    // Load history on first composition
    LaunchedEffect(Unit) {
        loadHistory(apiRepository) { newHistory, error ->
            historyList = newHistory
            errorMessage = error
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = searchUsername,
                            onValueChange = { searchUsername = it },
                            label = { Text("Usuario") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = searchAction,
                            onValueChange = { searchAction = it },
                            label = { Text("Acción") },
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
                                loadHistory(apiRepository) { newHistory, error ->
                                    // Aplicar filtros localmente a los datos recuperados de la API
                                    val filteredHistory = newHistory.filter { historyItem ->
                                        val matchesUsername = searchUsername.isEmpty() ||
                                                historyItem.userId.contains(searchUsername, ignoreCase = true)
                                        val matchesAction = searchAction.isEmpty() ||
                                                historyItem.action.contains(searchAction, ignoreCase = true)
                                        matchesUsername && matchesAction
                                    }

                                    historyList = filteredHistory
                                    errorMessage = if (filteredHistory.isEmpty() && newHistory.isNotEmpty())
                                        "No hay resultados para los filtros aplicados"
                                    else
                                        error
                                    isLoading = false
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
            
            // History list
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
                                    loadHistory(apiRepository) { newHistory, error ->
                                        historyList = newHistory
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
                } else if (historyList.isEmpty()) {
                    Text(
                        text = "No hay registros de historial disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyList) { historyItem ->
                            HistoryItem(history = historyItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(history: History) {
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
                    text = history.action,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "Usuario: ${history.userId}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Fecha: ${formatDate(history.timestamp)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Detalles: ${history.details}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Función para parsear las diferentes fechas y convertirlas a un formato estándar
private fun parseDate(dateString: String): Date? {
    val formats = listOf(
        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    )

    for (format in formats) {
        try {
            return format.parse(dateString)
        } catch (e: Exception) {
            // Intentar con el siguiente formato
        }
    }
    return null
}

private fun formatDate(dateString: String): String {
    try {
        val date = parseDate(dateString)
        return date?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(it)
        } ?: dateString
    } catch (e: Exception) {
        return dateString
    }
}

private suspend fun loadHistory(
    apiRepository: ApiRepository,
    onResult: (List<History>, String?) -> Unit
) {
    try {
        val history = apiRepository.getAllHistory()
        // Ordenar por fecha, más reciente primero
        val sortedHistory = history.sortedByDescending { historyItem ->
            parseDate(historyItem.timestamp)
        }
        onResult(sortedHistory, if (sortedHistory.isEmpty()) "No se encontraron registros de historial" else null)
    } catch (e: Exception) {
        onResult(emptyList(), e.message)
    }
}
