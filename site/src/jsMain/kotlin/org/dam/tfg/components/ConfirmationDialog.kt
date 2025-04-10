package org.dam.tfg.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba

@Composable
fun ConfirmationDialog(
    mensaje: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    // Fondo oscuro con posicionamiento completo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
            .zIndex(1000)
            // Añadir estas propiedades para asegurar cobertura total
            .padding(0.px)
            .margin(0.px)
            // Coordenadas explícitas para todos los bordes
            .styleModifier {
                property("top", "0")
                property("left", "0")
                property("right", "0")
                property("bottom", "0")
            }
            .backgroundColor(rgba(0, 0, 0, 0.5))
            .onClick { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        // Contenedor del diálogo
        Box(
            modifier = Modifier
                .width(350.px)
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .border(1.px, color = Colors.LightGray)
                .padding(20.px)
                .onClick { /* Detener la propagación */
                    it.stopPropagation()
                },
            contentAlignment = Alignment.Center
        ) {
            // El resto del código se mantiene igual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.px)
            ) {
                // Mensaje
                SpanText(
                    text = mensaje,
                    modifier = Modifier
                        .fontFamily(Constants.FONT_FAMILY)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Medium)
                        .color(Theme.Secondary.rgb)
                        .textAlign(TextAlign.Center)
                )

                // Botones
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Confirmar
                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px, leftRight = 16.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            text = "Eliminar",
                            modifier = Modifier
                                .fontFamily(Constants.FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Colors.White)
                        )
                    }
                    // Botón Cancelar
                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Colors.LightGray)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px, leftRight = 16.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onCancel() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            text = "Cancelar",
                            modifier = Modifier
                                .fontFamily(Constants.FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Colors.Black)
                        )
                    }
                }
            }
        }
    }
}