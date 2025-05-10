package org.dam.tfg.androidapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT
import org.dam.tfg.androidapp.util.Constants.ROUTE_HISTORY
import org.dam.tfg.androidapp.util.Constants.ROUTE_HOME
import org.dam.tfg.androidapp.util.Constants.ROUTE_LIST
import org.dam.tfg.androidapp.util.Constants.ROUTE_LOGOUT
import org.dam.tfg.androidapp.util.SessionManager

@Composable
fun AppDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    sessionManager: SessionManager
) {
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }

    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Panel de Administración",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Usuario: ${sessionManager.getUsername() ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Inicio
            NavigationDrawerItem(
                label = { Text("Inicio") },
                icon = { Icon(Icons.Default.Home, "Inicio") },
                selected = selectedItem == 0,
                onClick = {
                    selectedItem = 0
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(ROUTE_HOME)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Edición
            NavigationDrawerItem(
                label = { Text("Edición") },
                icon = { Icon(Icons.Default.Edit, "Edición") },
                selected = selectedItem == 1,
                onClick = {
                    selectedItem = 1
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(ROUTE_EDIT)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Listado
            NavigationDrawerItem(
                label = { Text("Listado") },
                icon = { Icon(Icons.Default.List, "Listado") },
                selected = selectedItem == 2,
                onClick = {
                    selectedItem = 2
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(ROUTE_LIST)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Historial
            NavigationDrawerItem(
                label = { Text("Historial") },
                icon = { Icon(Icons.Default.Refresh, "Historial") },
                selected = selectedItem == 3,
                onClick = {
                    selectedItem = 3
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(ROUTE_HISTORY)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Cerrar Sesión
            NavigationDrawerItem(
                label = { Text("Cerrar Sesión") },
                icon = { Icon(Icons.Default.ExitToApp, "Cerrar Sesión") },
                selected = selectedItem == 4,
                onClick = {
                    selectedItem = 4
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(ROUTE_LOGOUT)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
