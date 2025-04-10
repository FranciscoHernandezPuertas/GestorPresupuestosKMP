package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.bottom
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.flexShrink
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.models.Theme
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.silk.components.icons.fa.FaArrowLeft
import com.varabyte.kobweb.silk.components.icons.fa.FaArrowRight
import com.varabyte.kobweb.silk.components.icons.fa.FaXmark
import org.dam.tfg.util.BudgetManager

@Composable
fun BudgetFooter(
    previousScreen: Screen,
    nextScreen: Screen,
    validateData: () -> Boolean,
    saveData: () -> Unit
) {
    val context = rememberPageContext()
    var showConfirmDialog by remember { mutableStateOf(false) }
    val breakpoint = rememberBreakpoint()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.px)
            .position(Position.Fixed)
            .bottom(0.px)  // Fijar al fondo explícitamente
            .left(0.px)    // Fijar a la izquierda explícitamente
            .backgroundColor(Theme.Primary.rgb)
            .padding(leftRight = 20.px)
            .zIndex(100),  // Asegurar que esté por encima de otros elementos
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Cancelar
            FooterButton(
                text = "Cancelar",
                backgroundColor = Colors.Red,
                onClick = { showConfirmDialog = true },
                icon = {
                    FaXmark(
                        modifier = Modifier.color(Colors.White)
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Botón Atrás
                FooterButton(
                    text = "Atrás",
                    backgroundColor = Theme.Secondary.rgb,
                    onClick = {
                        if (previousScreen == Screen.Home) {
                            showConfirmDialog = true
                            return@FooterButton
                        } else {
                            saveData()
                            context.router.navigateTo(previousScreen.route)
                        }
                    },
                    icon = {
                        FaArrowLeft(
                            modifier = Modifier.color(Colors.White)
                        )
                    }
                )

                // Envolvemos el botón "Continuar" en un Box y aplicamos un margen a la izquierda
                Box(modifier = Modifier.margin(left = 8.px)) {
                    FooterButton(
                        text = "Continuar",
                        backgroundColor = Colors.Green,
                        onClick = {
                            if (validateData()) {
                                saveData()
                                context.router.navigateTo(nextScreen.route)
                            }
                        },
                        icon = {
                            FaArrowRight(
                                modifier = Modifier.color(Colors.White)
                            )
                        }
                    )
                }
            }
        }
    }

            // Diálogo de confirmación para cancelar
    if (showConfirmDialog) {
        ConfirmDialog(
            message = "¿Está seguro que desea cancelar? Se perderán todos los datos introducidos.",
            onConfirm = {
                showConfirmDialog = false
                BudgetManager.clearAllData() // Resetear los datos
                context.router.navigateTo(Screen.Home.route)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
private fun FooterButton(
    text: String,
    backgroundColor: CSSColorValue,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .padding(10.px)
            .backgroundColor(backgroundColor)
            .borderRadius(6.px)
            .padding(topBottom = 10.px, leftRight = 20.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Usamos una Row para alinear el texto y el icono en horizontal
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Colors.White),
                text = text
            )
            if (icon != null) {
                Box(modifier = Modifier.margin(left = 8.px)) {
                    icon()
                }
            }
        }
    }
}


@Composable
fun ConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Overlay semi-transparente que cubre toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
            .top(0.px)
            .left(0.px)
            .zIndex(999)
            .backgroundColor(Theme.HalfBlack.rgb)
            .onClick { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Contenedor del diálogo
        Box(
            modifier = Modifier
                .width(400.px)
                .padding(20.px)
                .backgroundColor(Theme.White.rgb)
                .borderRadius(8.px)
                .boxShadow(offsetX = 0.px, offsetY = 4.px, blurRadius = 8.px, color = Theme.Black.rgb)
                .onClick { it.stopPropagation() } // Evita que el clic se propague al overlay
        ) {
            Column(
                modifier = Modifier
                    .padding(20.px)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mensaje
                SpanText(
                    modifier = Modifier
                        .margin(bottom = 30.px)
                        .fontFamily(FONT_FAMILY)
                        .fontSize(18.px)
                        .color(Theme.Secondary.rgb),
                    text = message
                )

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Colors.Red)
                            .borderRadius(6.px)
                            .padding(topBottom = 10.px, leftRight = 20.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .color(Colors.White),
                            text = "Confirmar"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Theme.Secondary.rgb)
                            .borderRadius(6.px)
                            .flexShrink(0)
                            .padding(topBottom = 10.px, leftRight = 20.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .color(Colors.White),
                            text = "Cancelar"
                        )
                    }
                }
            }
        }
    }
}