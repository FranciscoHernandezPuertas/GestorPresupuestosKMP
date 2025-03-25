package org.dam.tfg.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dam.tfg.navigation.Screen
import org.w3c.dom.get

/**
 * Comprueba si el usuario está autenticado y ejecuta el contenido si lo está.
 * Si no está autenticado, redirige a la página de login.
 */
@Composable
fun isUserLoggedInCheck(content: @Composable () -> Unit) {
    val context = rememberPageContext()
    var isAuthorized by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val token = localStorage["jwt_token"]

        if (token == null) {
            context.router.navigateTo(Screen.Login.route)
        } else {
            // Validar el token en el servidor
            val user = validateToken()
            if (user == null) {
                logout()
                context.router.navigateTo(Screen.Login.route)
            } else {
                isAuthorized = true
            }
        }
        isChecking = false
    }

    if (!isChecking && isAuthorized) {
        content()
    }
}

/**
 * Verifica si el usuario actual es administrador.
 * Si no lo es, redirige a la página principal.
 */
@Composable
fun isAdminCheck(content: @Composable () -> Unit) {
    val context = rememberPageContext()
    var isAdmin by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Validar el token en el servidor y obtener el tipo de usuario
        val user = validateToken()

        if (user?.type != "admin") {
            // Si el token no es válido o el usuario no es admin, redirigir
            context.router.navigateTo(Screen.Home.route)
        } else {
            isAdmin = true
        }
        isChecking = false
    }

    if (!isChecking && isAdmin) {
        content()
    }
}