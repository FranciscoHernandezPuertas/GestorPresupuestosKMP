package org.dam.tfg.androidapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.components.AppBar
import org.dam.tfg.androidapp.components.AppDrawerContent
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_FORMULAS
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_MATERIALS
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_USERS
import org.dam.tfg.androidapp.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavController,
    sessionManager: SessionManager
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                drawerState = drawerState,
                sessionManager = sessionManager
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    title = "Edición",
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Selecciona qué quieres editar",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                EditOptionCard(
                    title = "Materiales",
                    description = "Añadir, editar o eliminar materiales",
                    onClick = { navController.navigate(ROUTE_EDIT_MATERIALS) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditOptionCard(
                    title = "Fórmulas",
                    description = "Añadir, editar o eliminar fórmulas",
                    onClick = { navController.navigate(ROUTE_EDIT_FORMULAS) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditOptionCard(
                    title = "Usuarios",
                    description = "Añadir, editar o eliminar usuarios",
                    onClick = { navController.navigate(ROUTE_EDIT_USERS) }
                )
            }
        }
    }
}

@Composable
fun EditOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar")
            }
        }
    }
}
