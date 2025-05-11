package org.dam.tfg.androidapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Materials : Screen("materials")
    object Formulas : Screen("formulas")
    object Users : Screen("users")
    object Budgets : Screen("budgets")
    object History : Screen("history")
    
    // Edit screens
    object EditMaterial : Screen("edit_material/{materialId}") {
        fun createRoute(materialId: String) = "edit_material/$materialId"
    }
    
    object EditFormula : Screen("edit_formula/{formulaId}") {
        fun createRoute(formulaId: String) = "edit_formula/$formulaId"
    }
    
    object EditUser : Screen("edit_user/{userId}") {
        fun createRoute(userId: String) = "edit_user/$userId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val actions = remember(navController) { NavigationActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    actions.navigateToHome(user)
                }
            )
        }
        
        composable(Screen.Home.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                HomeScreen(
                    user = it,
                    onNavigateToMaterials = actions.navigateToMaterials,
                    onNavigateToFormulas = actions.navigateToFormulas,
                    onNavigateToUsers = actions.navigateToUsers,
                    onNavigateToBudgets = actions.navigateToBudgets,
                    onNavigateToHistory = actions.navigateToHistory,
                    onLogout = actions.navigateToLogin
                )
            }
        }
        
        composable(Screen.Materials.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                MaterialsScreen(
                    user = it,
                    onNavigateBack = actions.navigateUp,
                    onNavigateToEditMaterial = actions.navigateToEditMaterial
                )
            }
        }
        
        composable(
            route = Screen.EditMaterial.route,
            arguments = listOf(navArgument("materialId") { type = NavType.StringType })
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                EditMaterialScreen(
                    materialId = materialId,
                    user = it,
                    onNavigateBack = actions.navigateUp
                )
            }
        }
        
        composable(Screen.Formulas.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                FormulasScreen(
                    user = it,
                    onNavigateBack = actions.navigateUp,
                    onNavigateToEditFormula = actions.navigateToEditFormula
                )
            }
        }
        
        composable(
            route = Screen.EditFormula.route,
            arguments = listOf(navArgument("formulaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val formulaId = backStackEntry.arguments?.getString("formulaId") ?: ""
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                EditFormulaScreen(
                    formulaId = formulaId,
                    user = it,
                    onNavigateBack = actions.navigateUp
                )
            }
        }
        
        composable(Screen.Users.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                UsersScreen(
                    user = it,
                    onNavigateBack = actions.navigateUp,
                    onNavigateToEditUser = actions.navigateToEditUser
                )
            }
        }
        
        composable(
            route = Screen.EditUser.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                EditUserScreen(
                    userId = userId,
                    user = it,
                    onNavigateBack = actions.navigateUp
                )
            }
        }
        
        composable(Screen.Budgets.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                BudgetsScreen(
                    user = it,
                    onNavigateBack = actions.navigateUp
                )
            }
        }
        
        composable(Screen.History.route) {
            val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
            user?.let {
                HistoryScreen(
                    user = it,
                    onNavigateBack = actions.navigateUp
                )
            }
        }
    }
}

class NavigationActions(private val navController: NavHostController) {
    
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }
    
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    val navigateToHome: (User) -> Unit = { user ->
        navController.currentBackStackEntry?.savedStateHandle?.set("user", user)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }
    
    val navigateToMaterials: () -> Unit = {
        navController.navigate(Screen.Materials.route)
    }
    
    val navigateToFormulas: () -> Unit = {
        navController.navigate(Screen.Formulas.route)
    }
    
    val navigateToUsers: () -> Unit = {
        navController.navigate(Screen.Users.route)
    }
    
    val navigateToBudgets: () -> Unit = {
        navController.navigate(Screen.Budgets.route)
    }
    
    val navigateToHistory: () -> Unit = {
        navController.navigate(Screen.History.route)
    }
    
    val navigateToEditMaterial: (String) -> Unit = { materialId ->
        navController.navigate(Screen.EditMaterial.createRoute(materialId))
    }
    
    val navigateToEditFormula: (String) -> Unit = { formulaId ->
        navController.navigate(Screen.EditFormula.createRoute(formulaId))
    }
    
    val navigateToEditUser: (String) -> Unit = { userId ->
        navController.navigate(Screen.EditUser.createRoute(userId))
    }
}
