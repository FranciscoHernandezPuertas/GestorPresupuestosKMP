package org.dam.tfg.androidapp.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.AppBar
import org.dam.tfg.androidapp.components.SearchBar
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.models.Mesa
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavController,
    dataStore: DataStore
) {
    val scope = rememberCoroutineScope()

    val mesas by dataStore.mesas.collectAsState()
    var filteredMesas by remember { mutableStateOf(mesas) }
    var searchQuery by remember { mutableStateOf("") }

    // Load mesas
    LaunchedEffect(Unit) {
        dataStore.loadMesas()
    }

    // Update filtered mesas when mesas change
    LaunchedEffect(mesas, searchQuery) {
        filteredMesas = if (searchQuery.isBlank()) {
            mesas
        } else {
            dataStore.searchMesas(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = "Listado de Presupuestos",
                onMenuClick = {
                    scope.launch {
                        // No drawer in this screen
                    }
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                hint = "Buscar por usuario o fecha",
                onSearch = { query ->
                    searchQuery = query
                }
            )

            if (filteredMesas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay presupuestos",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredMesas) { mesa ->
                        MesaItem(mesa = mesa)
                    }
                }
            }
        }
    }
}

@Composable
fun MesaItem(mesa: Mesa) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    // Format date
    val formattedDate = mesa.fechaCreacion?.let {
        try {
            val instant = Instant.parse(it)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            it
        }
    } ?: "Fecha desconocida"

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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mesa tipo: ${mesa.tipo}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = numberFormat.format(mesa.precioTotal / 100), // Assuming price is in cents
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usuario: ${mesa.username}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Fecha: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tramos: ${mesa.tramos.size}",
                style = MaterialTheme.typography.bodySmall
            )

            if (mesa.cubetas.isNotEmpty()) {
                Text(
                    text = "Cubetas: ${mesa.cubetas.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (mesa.modulos.isNotEmpty()) {
                Text(
                    text = "Módulos: ${mesa.modulos.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (mesa.elementosGenerales.isNotEmpty()) {
                Text(
                    text = "Elementos generales: ${mesa.elementosGenerales.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
