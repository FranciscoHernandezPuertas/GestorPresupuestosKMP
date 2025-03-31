package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.icons.fa.FaTriangleExclamation
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba


@Composable
fun AdvertenciaOverlay(
    mensaje: String,
    onDismiss: () -> Unit,
    autoHide: Boolean = true,
    duracionMs: Int = 5000
) {
    var opacidad by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        opacidad = 100

        if (autoHide) {
            kotlinx.browser.window.setTimeout({
                opacidad = 0
                kotlinx.browser.window.setTimeout({
                    onDismiss()
                }, 300) // Tiempo adicional para la animación de salida
            }, duracionMs)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
            .top(0.px)
            .left(0.px)
            .zIndex(1000)
            .backgroundColor(rgba(0, 0, 0, 0.5))
            .opacity(opacidad.percent)
            .styleModifier {
                property("transition", "opacity 300ms ease")
            }
            .onClick {
                if (!autoHide) {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (rememberBreakpoint() > Breakpoint.MD) 40.percent else 90.percent)
                .padding(24.px)
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .boxShadow(offsetX = 0.px, offsetY = 4.px, blurRadius = 8.px, color = rgba(0, 0, 0, 0.2))
                .onClick {
                    // Evita propagación de clics al fondo
                    it.stopPropagation()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.px),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ícono de advertencia
                FaTriangleExclamation(
                    modifier = Modifier
                        .margin(bottom = 16.px)
                        .color(Colors.Orange)
                        .fontSize(32.px)
                )

                // Mensaje
                SpanText(
                    text = mensaje,
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .color(Theme.Secondary.rgb)
                        .textAlign(TextAlign.Center)
                        .margin(bottom = 24.px)
                )

                // Botón OK
                Box(
                    modifier = Modifier
                        .backgroundColor(Theme.Primary.rgb)
                        .borderRadius(4.px)
                        .width(120.px)
                        .padding(topBottom = 8.px)
                        .cursor(Cursor.Pointer)
                        .onClick { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        text = "Aceptar",
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Colors.White)
                    )
                }
            }
        }
    }
}
