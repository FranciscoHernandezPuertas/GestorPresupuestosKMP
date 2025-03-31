// site/src/jsMain/kotlin/org/dam/tfg/pages/Index.kt
package org.dam.tfg.pages.budget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.models.Theme
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun HomePage() {
    isUserLoggedInCheck {
        LaunchedEffect(Unit) {
            BudgetManager.resetBudgetData()
        }
        HomePageContent()
    }
}

@Composable
fun HomePageContent() {
    val context = rememberPageContext()
    val breakpoint = rememberBreakpoint()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.px),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Usa el componente reutilizable
            AppHeader()

            // Contenido específico para usuarios
            HomeContent()
        }
    }
}


@Composable
private fun HomeContent() {
    val context = rememberPageContext()

    // Título de la sección
    SpanText(
        modifier = Modifier
            .margin(top = 80.px, bottom = 40.px)
            .fontFamily(FONT_FAMILY)
            .fontSize(24.px)
            .fontWeight(FontWeight.Medium)
            .textAlign(TextAlign.Center)
            .color(Theme.Secondary.rgb),
        text = "¿Qué presupuesto desea generar?"
    )

    // Opciones según tipo de usuario
    Column(
        modifier = Modifier
            .margin(bottom = 30.px)
            .padding(10.px)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Colors.LightGray)
            .width(250.px)
            .cursor(Cursor.Pointer)
            .onClick {
                // Aquí iría la navegación al wizard de mesas
                context.router.navigateTo(Screen.TableSelector.route)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .width(200.px)
                .height(150.px)
                .margin(bottom = 15.px),
            src = Res.Image.formTableIcon,
            alt = "Mesa"
        )

        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(20.px)
                .fontWeight(FontWeight.Bold)
                .color(Theme.Secondary.rgb),
            text = "Mesas"
        )
    }
}