package org.dam.tfg.androidapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.dam.tfg.androidapp.data.DataStore
import org.dam.tfg.androidapp.screens.EditScreen
import org.dam.tfg.androidapp.screens.HistoryScreen
import org.dam.tfg.androidapp.screens.HomeScreen
import org.dam.tfg.androidapp.screens.ListScreen
import org.dam.tfg.androidapp.screens.LoginScreen
import org.dam.tfg.androidapp.screens.edit.EditFormulasScreen
import org.dam.tfg.androidapp.screens.edit.EditMaterialsScreen
import org.dam.tfg.androidapp.screens.edit.EditUsersScreen
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_FORMULAS
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_MATERIALS
import org.dam.tfg.androidapp.util.Constants.ROUTE_EDIT_USERS
import org.dam.tfg.androidapp.util.Constants.ROUTE_HISTORY
import org.dam.tfg.androidapp.util.Constants.ROUTE_HOME
import org.dam.tfg.androidapp.util.Constants.ROUTE_LIST
import org.dam.tfg.androidapp.util.Constants.ROUTE_LOGIN
import org.dam.tfg.androidapp.util.Constants.ROUTE_LOGOUT
import org.dam.tfg.androidapp.util.SessionManager

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    dataStore: DataStore
) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

    // Check if user is logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(ROUTE_HOME) {
                popUpTo(ROUTE_LOGIN) { inclusive = true }
            }
        } else {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) ROUTE_HOME else ROUTE_LOGIN
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    isLoggedIn = true
                },
                sessionManager = sessionManager,
                dataStore = dataStore
            )
        }

        composable(ROUTE_HOME) {
            HomeScreen(
                navController = navController,
                sessionManager = sessionManager,
                dataStore = dataStore
            )
        }

        composable(ROUTE_EDIT) {
            EditScreen(
                navController = navController,
                sessionManager = sessionManager
            )
        }

        composable(ROUTE_EDIT_MATERIALS) {
            EditMaterialsScreen(
                navController = navController,
                dataStore = dataStore
            )
        }

        composable(ROUTE_EDIT_FORMULAS) {
            EditFormulasScreen(
                navController = navController,
                dataStore = dataStore,
                sessionManager = sessionManager
            )
        }

        composable(ROUTE_EDIT_USERS) {
            EditUsersScreen(
                navController = navController,
                dataStore = dataStore,
                sessionManager = sessionManager
            )
        }

        composable(ROUTE_LIST) {
            ListScreen(
                navController = navController,
                dataStore = dataStore
            )
        }

        composable(ROUTE_HISTORY) {
            HistoryScreen(
                navController = navController,
                dataStore = dataStore
            )
        }

        composable(ROUTE_LOGOUT) {
            LaunchedEffect(Unit) {
                sessionManager.clearSession()
                isLoggedIn = false
            }
        }
    }
}
