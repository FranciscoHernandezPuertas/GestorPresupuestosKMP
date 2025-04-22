package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.style.KobwebComposeStyleSheet.attr
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.classNames
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.SwitchSize
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Ul

@Composable
fun ComponentesForm() {
    val breakpoint = rememberBreakpoint()
    var switch1 by remember { mutableStateOf(false) }
    var switch2 by remember { mutableStateOf(false) }
    var switch3 by remember { mutableStateOf(false) }

    SimpleGrid(
        numColumns = numColumns(base = 1, sm = 3),
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- Row de los 3 switches ---
        Row(
            modifier = Modifier
                .margin(
                    right = if (breakpoint < Breakpoint.SM) 0.px else 24.px,
                    bottom = if (breakpoint < Breakpoint.SM) 24.px else 0.px
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch1,
                onCheckedChange = { switch1 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 1",
            )
        }
        Row(
            modifier = Modifier
                .margin(
                    right = if (breakpoint < Breakpoint.SM) 0.px else 24.px,
                    bottom = if (breakpoint < Breakpoint.SM) 24.px else 0.px
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch2,
                onCheckedChange = { switch2 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 2",
            )
        }
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch3,
                onCheckedChange = { switch3 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 3",
            )
        }

        // --- Input 1 ocupa fila completa ---
        Input(
            type = InputType.Text,
            attrs = Modifier
                .fillMaxWidth()
                .height(54.px)
                .margin(topBottom = 12.px)
                .backgroundColor(Theme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .border(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                .styleModifier {
                    property("outline", "none")
                    // aquí decimos que ocupe de la columna 1 a la última
                    property("grid-column", "1 / -1")
                }
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .toAttrs {
                    attr("placeholder", "Placeholder 1")
                }
        )

        // --- Input 2 ocupa fila completa ---
        Input(
            type = InputType.Text,
            attrs = Modifier
                .fillMaxWidth()
                .height(54.px)
                .margin(bottom = 12.px)
                .backgroundColor(Theme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .border(width = 0.px, style = LineStyle.None, color = Colors.Transparent)
                .styleModifier {
                    property("outline", "none")
                    property("grid-column", "1 / -1")
                }
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .toAttrs {
                    attr("placeholder", "Placeholder 2")
                }
        )

        // --- Dropdown ocupa fila completa ---
        Box(
            modifier = Modifier
                .classNames("dropdown")
                .margin(topBottom = 12.px)
                .fillMaxWidth()
                .height(54.px)
                .backgroundColor(Theme.LightGray.rgb)
                .cursor(Cursor.Pointer)
                .styleModifier {
                    property("grid-column", "1 / -1")
                }
                .onClick { /* toggle interno */ }
        ) {
            ElementDropdown(
                selectedElement = ElementoSeleccionado(
                    nombre = "Elemento 1",
                    cantidad = 1,
                    limite = ItemWithLimits("Elemento 1", "Elemento 1", 10)
                ),
                onElementSelected = { /* ... */ }
            )
        }
    }
}


@Composable
fun ElementDropdown(
    selectedElement: ElementoSeleccionado,
    onElementSelected: (ElementoSeleccionado) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    // Lista de elementos disponibles para seleccionar
    val elementosDisponibles = remember {
        listOf(
            ElementoSeleccionado(
                nombre = "Elemento 1",
                cantidad = 1,
                limite = ItemWithLimits(name = "Elemento 1", minQuantity = 0, maxQuantity = 10)
            ),
            ElementoSeleccionado(
                nombre = "Elemento 2",
                cantidad = 1,
                limite = ItemWithLimits(name = "Elemento 2", minQuantity = 0, maxQuantity = 10)
            ),
            ElementoSeleccionado(
                nombre = "Elemento 3",
                cantidad = 1,
                limite = ItemWithLimits(name = "Elemento 3", minQuantity = 0, maxQuantity = 10)
            )
        )
    }

    Box(
        modifier = Modifier
            .classNames("dropdown")
            .margin(topBottom = 12.px)
            .fillMaxWidth()
            .height(54.px)
            .backgroundColor(Theme.LightGray.rgb)
            .cursor(Cursor.Pointer)
            .onClick { isOpen = !isOpen }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(leftRight = 20.px),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SpanText(
                modifier = Modifier
                    .fillMaxWidth()
                    .fontSize(16.px)
                    .fontFamily(FONT_FAMILY),
                text = selectedElement.nombre
            )
            Box(
                modifier = Modifier
                    .classNames("dropdown-toggle")
            )
        }

        if (isOpen) {
            Ul(
                attrs = Modifier
                    .fillMaxWidth()
                    .classNames("dropdown-menu")
                    .toAttrs()
            ) {
                elementosDisponibles.forEach { elemento ->
                    Li {
                        A(
                            attrs = Modifier
                                .classNames("dropdown-item")
                                .color(Colors.Black)
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .onClick {
                                    onElementSelected(elemento)
                                    isOpen = false
                                }
                                .toAttrs()
                        ) {

                        }
                    }
                }
            }
        }
    }
}