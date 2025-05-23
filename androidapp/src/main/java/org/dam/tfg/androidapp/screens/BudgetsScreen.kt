package org.dam.tfg.androidapp.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.repository.ApiRepository
import org.dam.tfg.androidapp.models.Budget
import org.dam.tfg.androidapp.models.User
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "BudgetsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val apiRepository = remember { ApiRepository() }

    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Search filters
    var searchUsername by remember { mutableStateOf("") }

    // Para el diálogo de detalle
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // Función para parsear diferentes formatos de fecha
    fun parseDate(dateString: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd",
            "dd/MM/yyyy"
        )

        for (format in formats) {
            try {
                val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
                if (format.contains("'Z'")) {
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
                }
                return simpleDateFormat.parse(dateString)
            } catch (e: Exception) {
                // Intentar con el siguiente formato
            }
        }
        return null
    }

    // Función para cargar presupuestos
    fun loadBudgets(onComplete: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                Log.d(TAG, "Iniciando carga de presupuestos...")

                val loadedBudgets = apiRepository.getAllBudgets()

                // Ordenar por fecha, de más reciente a más antigua
                val sortedBudgets = loadedBudgets.sortedByDescending { budget ->
                    parseDate(budget.fechaCreacion)
                }

                // Filtrar por username si se especificó
                val filteredBudgets = if (searchUsername.isNotBlank()) {
                    sortedBudgets.filter { it.username.contains(searchUsername, ignoreCase = true) }
                } else {
                    sortedBudgets
                }

                budgets = filteredBudgets

                Log.d(TAG, "Presupuestos cargados exitosamente: ${budgets.size}")
                onComplete(true, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar presupuestos: ${e.message}", e)
                errorMessage = "Error al cargar presupuestos: ${e.message?.take(100)}"
                onComplete(false, errorMessage)
            } finally {
                isLoading = false
            }
        }
    }

    // Load budgets on first composition
    LaunchedEffect(Unit) {
        Log.d(TAG, "Cargando presupuestos iniciales...")
        loadBudgets { success, error ->
            if (!success) {
                Log.e(TAG, "Error en carga inicial: $error")
            }
        }
    }

    // Función para eliminar un presupuesto
    fun deleteBudget(budget: Budget) {
        coroutineScope.launch {
            try {
                isLoading = true
                val budgetId = budget.getActualId()
                Log.d(TAG, "Eliminando presupuesto con ID: $budgetId")

                val success = apiRepository.deleteBudget(budgetId)

                if (success) {
                    // Eliminar el presupuesto de la lista local
                    budgets = budgets.filter { it._id != budget._id && it.id != budget.id }

                    // Mostrar mensaje de éxito
                    errorMessage = null
                } else {
                    errorMessage = "Error al eliminar el presupuesto"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar presupuesto: ${e.message}", e)
                errorMessage = "Error al eliminar presupuesto: ${e.message}"
            } finally {
                isLoading = false
            }
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

                    Button(
                        onClick = {
                            loadBudgets { _, _ -> }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando presupuestos...")
                    }
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                loadBudgets { _, _ -> }
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                } else if (budgets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sin datos",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No hay presupuestos disponibles",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(budgets) { budget ->
                            BudgetItem(
                                budget = budget,
                                onClick = {
                                    selectedBudget = budget
                                    showDetailDialog = true
                                },
                                onDelete = { deleteBudget(it) }
                            )
                        }
                    }
                }
            }

            // Diálogo de detalle
            if (showDetailDialog && selectedBudget != null) {
                BudgetDetailDialog(
                    budget = selectedBudget!!,
                    onDismiss = {
                        showDetailDialog = false
                        selectedBudget = null
                    }
                )
            }
        }
    }
}

@Composable
fun BudgetItem(
    budget: Budget,
    onClick: () -> Unit,
    onDelete: (Budget) -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
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

            // Fila de acciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para ver detalles
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Ver detalle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ver detalles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Botón de eliminar
                IconButton(
                    onClick = { showDeleteConfirmation = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Está seguro que desea eliminar este presupuesto? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete(budget)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BudgetDetailDialog(
    budget: Budget,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del Presupuesto",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                // Budget content in a scrollable column
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Sección principal del presupuesto
                    item {
                        SectionTitle("Información General")
                        DetailRow("ID", budget._id)
                        DetailRow("Tipo", budget.tipo)
                        DetailRow("Usuario", budget.username)
                        DetailRow("Fecha", formatDate(budget.fechaCreacion))
                        DetailRow("Precio Total", "${formatPrice(budget.precioTotal)}€")
                        if (budget.error.isNotEmpty()) {
                            DetailRow("Error", budget.error)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Sección de tramos
                    item {
                        SectionTitle("Tramos (${budget.tramos.size})")
                    }

                    // Lista de tramos
                    items(budget.tramos) { tramo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                DetailRow("Número", tramo.numero.toString())
                                DetailRow("Tipo", tramo.tipo)
                                DetailRow("Largo", "${tramo.largo} mm")
                                DetailRow("Ancho", "${tramo.ancho} mm")
                                DetailRow("Precio", "${formatPrice(tramo.precio)}€")
                                if (tramo.error.isNotEmpty()) {
                                    DetailRow("Error", tramo.error)
                                }
                            }
                        }
                    }

                    // Separador
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Sección de elementos generales
                    item {
                        SectionTitle("Elementos Generales (${budget.elementosGenerales.size})")
                    }

                    // Lista de elementos generales
                    items(budget.elementosGenerales) { elemento ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                DetailRow("Nombre", elemento.nombre)
                                DetailRow("Cantidad", elemento.cantidad.toString())
                                DetailRow("Precio", "${formatPrice(elemento.precio)}€")
                            }
                        }
                    }

                    // Separador
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Sección de cubetas
                    item {
                        SectionTitle("Cubetas (${budget.cubetas.size})")
                    }

                    // Lista de cubetas
                    items(budget.cubetas) { cubeta ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                DetailRow("Tipo", cubeta.tipo)
                                DetailRow("Número", cubeta.numero.toString())
                                DetailRow("Largo", "${cubeta.largo} mm")
                                DetailRow("Fondo", "${cubeta.fondo} mm")
                                DetailRow("Alto", "${cubeta.alto} mm")
                                DetailRow("Precio", "${formatPrice(cubeta.precio)}€")
                                if (cubeta.error.isNotEmpty()) {
                                    DetailRow("Error", cubeta.error)
                                }
                            }
                        }
                    }

                    // Separador
                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Sección de módulos
                    item {
                        SectionTitle("Módulos (${budget.modulos.size})")
                    }

                    // Lista de módulos
                    items(budget.modulos) { modulo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                DetailRow("Nombre", modulo.nombre)
                                DetailRow("Largo", "${modulo.largo} mm")
                                DetailRow("Fondo", "${modulo.fondo} mm")
                                DetailRow("Alto", "${modulo.alto} mm")
                                DetailRow("Cantidad", modulo.cantidad.toString())
                                DetailRow("Precio", "${formatPrice(modulo.precio)}€")
                            }
                        }
                    }

                    // Espacio final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.2f", price)
}

private fun formatDate(dateString: String): String {
    try {
        // Intentar varios formatos de fecha
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )

        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // Intentar con el siguiente formato
            }
        }

        // Si ningún formato funciona, devolver la cadena original
        return dateString
    } catch (e: Exception) {
        Log.e(TAG, "Error al formatear fecha: $dateString", e)
        return dateString
    }
}

