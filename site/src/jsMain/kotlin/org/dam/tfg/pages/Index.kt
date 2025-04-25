package org.dam.tfg.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import org.dam.tfg.navigation.Screen

@Page  // al ser Index.kt se asigna ruta "/"
@Composable
fun RootRedirectPage() {
    val ctx = rememberPageContext()
    LaunchedEffect(Unit) {
        ctx.router.navigateTo(Screen.Login.route)
    }
}
