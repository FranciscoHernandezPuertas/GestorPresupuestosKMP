package org.dam.tfg.androidapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.repository.ApiRepository
import org.dam.tfg.androidapp.models.Formula
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.util.IdUtils

private const val TAG = "EditFormulaScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFormulaScreen(
    formulaId: String,
    user: User,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val apiRepository = remember { ApiRepository() }

    var formula by remember { mutableStateOf<Formula?>(null) }
    var name by remember { mutableStateOf("") }
    var formulaText by remember { mutableStateOf("") }
    var variables by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var variableKey by remember { mutableStateOf("") }
    var variableValue by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(formulaId != "new") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Load formula if editing an existing one
    LaunchedEffect(formulaId) {
        if (formulaId != "new") {
            try {
                Log.d(TAG, "Cargando fórmula con ID: $formulaId")
                isLoading = true
                errorMessage = null

                val loadedFormula = apiRepository.getFormulaById(formulaId)

                if (loadedFormula != null) {
                    Log.d(TAG, "Fórmula cargada: ${loadedFormula.name}")
                    formula = loadedFormula
                    name = loadedFormula.name

                    // La fórmula ya viene desencriptada de la API si el usuario es admin
                    formulaText = loadedFormula.formula
                    variables = loadedFormula.variables
                } else {
                    Log.e(TAG, "Fórmula no encontrada con ID: $formulaId")
                    errorMessage = "Fórmula no encontrada. Verifique el ID."
                }
                isLoading = false
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar la fórmula: ${e.message}", e)
                errorMessage = "Error al cargar la fórmula: ${e.message?.take(100)}"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    // Función para guardar fórmula
    fun saveFormula() {
        // Validate inputs
        if (name.isBlank()) {
            errorMessage = "El nombre no puede estar vacío"
            return
        }

        if (formulaText.isBlank()) {
            errorMessage = "La fórmula no puede estar vacía"
            return
        }

        // Save formula
        coroutineScope.launch {
            isSaving = true
            errorMessage = null
            successMessage = null

            try {
                Log.d(TAG, "Guardando fórmula: $name")

                // La API se encargará de encriptar la fórmula en el servidor
                val formulaToSave = Formula(
                    _id = formula?._id ?: "",
                    name = name,
                    formula = formulaText,
                    formulaEncrypted = false, // La API la encriptará
                    variables = variables
                )

                val success = if (formulaId == "new") {
                    apiRepository.createFormula(formulaToSave)
                } else {
                    apiRepository.updateFormula(formulaToSave)
                }

                if (success) {
                    Log.d(TAG, "Fórmula guardada correctamente")
                    successMessage = if (formulaId == "new") {
                        "Fórmula creada correctamente"
                    } else {
                        "Fórmula actualizada correctamente"
                    }

                    // Clear form if creating a new formula
                    if (formulaId == "new") {
                        name = ""
                        formulaText = ""
                        variables = emptyMap()
                        variableKey = ""
                        variableValue = ""
                    }
                } else {
                    Log.e(TAG, "No se pudo guardar la fórmula")
                    errorMessage = "No se pudo guardar la fórmula. Verifique la conexión e intente nuevamente."
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar la fórmula: ${e.message}", e)
                errorMessage = "Error: ${e.message?.take(100)}"
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (formulaId == "new") "Nueva Fórmula" else "Editar Fórmula")
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
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando fórmula...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
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
                        value = formulaText,
                        onValueChange = { formulaText = it },
                        label = { Text("Fórmula") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(120.dp),
                        isError = formulaText.isBlank() && errorMessage != null
                    )

                    // Variables section
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
                            Text(
                                text = "Variables",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Display existing variables
                            if (variables.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    variables.forEach { (key, value) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$key: $value",
                                                modifier = Modifier.weight(1f)
                                            )

                                            IconButton(
                                                onClick = {
                                                    variables = variables.toMutableMap().apply {
                                                        remove(key)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar Variable",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Add new variable
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = variableKey,
                                    onValueChange = { variableKey = it },
                                    label = { Text("Clave") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = variableValue,
                                    onValueChange = { variableValue = it },
                                    label = { Text("Valor") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    if (variableKey.isNotBlank() && variableValue.isNotBlank()) {
                                        variables = variables.toMutableMap().apply {
                                            put(variableKey, variableValue)
                                        }
                                        variableKey = ""
                                        variableValue = ""
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp),
                                enabled = variableKey.isNotBlank() && variableValue.isNotBlank()
                            ) {
                                Text("Añadir Variable")
                            }
                        }
                    }

                    Button(
                        onClick = { saveFormula() },
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
                            Text(if (formulaId == "new") "Crear Fórmula" else "Actualizar Fórmula")
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
                }
            }
        }
    }
}
