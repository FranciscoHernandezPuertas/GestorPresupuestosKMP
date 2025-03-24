// site/src/jsMain/kotlin/org/dam/tfg/pages/Index.kt
package org.dam.tfg.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaGear
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.localStorage
import org.dam.tfg.models.Theme
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.Constants
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.jetbrains.compose.web.css.px
import org.w3c.dom.get

@Page
@Composable
fun HomePage() {
    val context = rememberPageContext()
    var userType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userType = localStorage["userType"] ?: ""
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.px),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con título y posible botón de admin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(leftRight = 20.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer()

                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(28.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb),
                    text = "Generador de Presupuestos"
                )

                Spacer()

                // Mostrar el icono de engranaje solo para administradores
                if (userType == "admin") {
                    FaGear(
                        modifier = Modifier
                            .margin(left = 15.px)
                            .color(Theme.Primary.rgb)
                            .cursor(Cursor.Pointer)
                            .onClick { context.router.navigateTo(Screen.AdminHome.route) },
                        size = IconSize.XL
                    )
                }
            }

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

            // Opción de mesa
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
                        // context.router.navigateTo("/budget/table") 
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .width(200.px)
                        .height(150.px)
                        .margin(bottom = 15.px),
                    src = Res.Image.formTableIcon, // Necesitarás crear este recurso
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
    }
}