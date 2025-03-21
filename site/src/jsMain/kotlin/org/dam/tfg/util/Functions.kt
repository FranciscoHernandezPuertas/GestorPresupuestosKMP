package org.dam.tfg.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import org.dam.tfg.navigation.Screen
import org.w3c.dom.get
import org.w3c.dom.set

@Composable
fun isUserLoggedInCheck(content: @Composable () -> Unit) {
    val context = rememberPageContext()
    var isLoading by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Leer los valores actuales
        val remembered = localStorage["remember"]?.toBoolean() ?: false
        val userId = localStorage["userId"]
        console.log("Datos de sesión: remembered = $remembered, userId = $userId")

        if (remembered && !userId.isNullOrEmpty()) {
            val userIdExists = checkUserId(id = userId)
            console.log("Resultado de checkUserId: $userIdExists")
            if (userIdExists) {
                isAuthenticated = true
            } else {
                console.log("Verificación fallida, redirigiendo a /admin/login")
                context.router.navigateTo(Screen.AdminLogin.route)
            }
        } else {
            console.log("No hay datos de sesión, redirigiendo a /admin/login")
            context.router.navigateTo(Screen.AdminLogin.route)
        }
        isLoading = false
    }

    if (isLoading) {
        console.log("Cargando...")
    } else if (isAuthenticated) {
        content()
    }
}

fun logout() {
    localStorage["remember"] = "false"
    localStorage["userId"] = ""
    localStorage["username"] = ""
}