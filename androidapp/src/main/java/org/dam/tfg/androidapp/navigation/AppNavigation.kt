package org.dam.tfg.androidapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.androidapp.models.User
import org.dam.tfg.androidapp.screens.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "home/$encodedUserJson"
        }
    }
    object Materials : Screen("materials/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "materials/$encodedUserJson"
        }
    }
    object Formulas : Screen("formulas/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "formulas/$encodedUserJson"
        }
    }
    object Users : Screen("users/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "users/$encodedUserJson"
        }
    }
    object Budgets : Screen("budgets/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "budgets/$encodedUserJson"
        }
    }
    object History : Screen("history/{userJson}") {
        fun createRoute(user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "history/$encodedUserJson"
        }
    }

    // Edit screens
    object EditMaterial : Screen("edit_material/{materialId}/{userJson}") {
        fun createRoute(materialId: String, user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "edit_material/$materialId/$encodedUserJson"
        }
    }

    object EditFormula : Screen("edit_formula/{formulaId}/{userJson}") {
        fun createRoute(formulaId: String, user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "edit_formula/$formulaId/$encodedUserJson"
        }
    }

    object EditUser : Screen("edit_user/{userId}/{userJson}") {
        fun createRoute(userId: String, user: User): String {
            val userJson = Json.encodeToString(user)
            val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
            return "edit_user/$userId/$encodedUserJson"
        }
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

        composable(
            route = Screen.Home.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            HomeScreen(
                user = user,
                onNavigateToMaterials = { actions.navigateToMaterials(user) },
                onNavigateToFormulas = { actions.navigateToFormulas(user) },
                onNavigateToUsers = { actions.navigateToUsers(user) },
                onNavigateToBudgets = { actions.navigateToBudgets(user) },
                onNavigateToHistory = { actions.navigateToHistory(user) },
                onLogout = actions.navigateToLogin
            )
        }

        composable(
            route = Screen.Materials.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            MaterialsScreen(
                user = user,
                onNavigateBack = actions.navigateUp,
                onNavigateToEditMaterial = { materialId -> actions.navigateToEditMaterial(materialId, user) }
            )
        }

        composable(
            route = Screen.EditMaterial.route,
            arguments = listOf(
                navArgument("materialId") { type = NavType.StringType },
                navArgument("userJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            EditMaterialScreen(
                materialId = materialId,
                user = user,
                onNavigateBack = actions.navigateUp
            )
        }

        composable(
            route = Screen.Formulas.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            FormulasScreen(
                user = user,
                onNavigateBack = actions.navigateUp,
                onNavigateToEditFormula = { formulaId -> actions.navigateToEditFormula(formulaId, user) }
            )
        }

        composable(
            route = Screen.EditFormula.route,
            arguments = listOf(
                navArgument("formulaId") { type = NavType.StringType },
                navArgument("userJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val formulaId = backStackEntry.arguments?.getString("formulaId") ?: ""
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            EditFormulaScreen(
                formulaId = formulaId,
                user = user,
                onNavigateBack = actions.navigateUp
            )
        }

        composable(
            route = Screen.Users.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            UsersScreen(
                user = user,
                onNavigateBack = actions.navigateUp,
                onNavigateToEditUser = { userId -> actions.navigateToEditUser(userId, user) }
            )
        }

        composable(
            route = Screen.EditUser.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            EditUserScreen(
                userId = userId,
                user = user,
                onNavigateBack = actions.navigateUp
            )
        }

        composable(
            route = Screen.Budgets.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            BudgetsScreen(
                user = user,
                onNavigateBack = actions.navigateUp
            )
        }

        composable(
            route = Screen.History.route,
            arguments = listOf(navArgument("userJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUserJson = backStackEntry.arguments?.getString("userJson") ?: ""
            val userJson = URLDecoder.decode(encodedUserJson, StandardCharsets.UTF_8.toString())
            val user = Json.decodeFromString<User>(userJson)

            HistoryScreen(
                user = user,
                onNavigateBack = actions.navigateUp
            )
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
        navController.navigate(Screen.Home.createRoute(user)) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToMaterials: (User) -> Unit = { user ->
        navController.navigate(Screen.Materials.createRoute(user))
    }

    val navigateToFormulas: (User) -> Unit = { user ->
        navController.navigate(Screen.Formulas.createRoute(user))
    }

    val navigateToUsers: (User) -> Unit = { user ->
        navController.navigate(Screen.Users.createRoute(user))
    }

    val navigateToBudgets: (User) -> Unit = { user ->
        navController.navigate(Screen.Budgets.createRoute(user))
    }

    val navigateToHistory: (User) -> Unit = { user ->
        navController.navigate(Screen.History.createRoute(user))
    }

    val navigateToEditMaterial: (String, User) -> Unit = { materialId, user ->
        navController.navigate(Screen.EditMaterial.createRoute(materialId, user))
    }

    val navigateToEditFormula: (String, User) -> Unit = { formulaId, user ->
        navController.navigate(Screen.EditFormula.createRoute(formulaId, user))
    }

    val navigateToEditUser: (String, User) -> Unit = { userId, user ->
        navController.navigate(Screen.EditUser.createRoute(userId, user))
    }
}