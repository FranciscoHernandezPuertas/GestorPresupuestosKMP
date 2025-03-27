package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontStyle
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaMinus
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.models.Theme
import org.dam.tfg.models.budget.Extra
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.attributes.selected

/**
 * Componente genérico para manejar diferentes tipos de extras
 *
 * @param title Título de la sección
 * @param description Descripción del componente
 * @param imageSrc Ruta de la imagen a mostrar
 * @param items Lista de elementos actuales
 * @param itemOptions Lista de opciones disponibles para añadir
 * @param onItemAdded Callback cuando se añade un nuevo elemento
 * @param onQuantityChanged Callback cuando cambia la cantidad de un elemento
 * @param onDeleteClick Callback cuando se elimina un elemento
 * @param itemRenderer Composable para renderizar cada elemento
 * @param extractDimensions Función para extraer dimensiones del tipo de elemento
 */
@Composable
fun <T : Extra> ExtraItemsSection(
    title: String,
    description: String,
    imageSrc: String,
    items: List<T>,
    itemOptions: List<String>,
    onItemAdded: (String, Int) -> Unit,
    onQuantityChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    itemRenderer: @Composable (T, Int) -> Unit,
    extractDimensions: (String) -> Pair<Double, Double> = { Pair(0.0, 0.0) }
) {
    val breakpoint = rememberBreakpoint()
    var selectedOption by remember { mutableStateOf("") }
    val isMobile = breakpoint <= Breakpoint.MD

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 40.px)
            .padding(20.px)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Colors.LightGray),
    ) {
        // Título de la sección
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(20.px)
                .fontWeight(FontWeight.Medium)
                .color(Theme.Secondary.rgb)
                .margin(bottom = 20.px),
            text = title
        )

        // Selector para añadir elemento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .margin(bottom = 20.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Select(
                attrs = Modifier
                    .fillMaxWidth(if (breakpoint <= Breakpoint.MD) 70.percent else 85.percent)
                    .height(40.px)
                    .margin(right = 10.px)
                    .border(1.px, LineStyle.Solid, Theme.Secondary.rgb)
                    .borderRadius(4.px)
                    .fontFamily(FONT_FAMILY)
                    .toAttrs {
                        onChange { event ->
                            selectedOption = event.target.value
                        }
                    }
            ) {
                Option(
                    value = "",
                    attrs = Modifier.toAttrs {
                        if (selectedOption.isEmpty()) {
                            selected()
                        }
                    }
                ) {
                    Text("Seleccionar $title...")
                }

                itemOptions.forEach { option ->
                    Option(
                        value = option,
                        attrs = Modifier.toAttrs {
                            if (selectedOption == option) {
                                selected()
                            }
                        }
                    ) {
                        Text(option)
                    }
                }
            }

            Button(
                attrs = Modifier
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
                    .borderRadius(4.px)
                    .padding(topBottom = 8.px, leftRight = 16.px)
                    .border(0.px, LineStyle.None, Colors.Transparent)
                    .cursor(Cursor.Pointer)
                    .onClick {
                        if (selectedOption.isNotEmpty()) {
                            onItemAdded(selectedOption, 1)
                            selectedOption = ""
                        }
                    }
                    .toAttrs()
            ) {
                Text("Añadir")
            }
        }

        // Diseño de dos columnas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .styleModifier {
                    if (isMobile) {
                        property("flex-direction", "column")
                    }
                },
            verticalAlignment = Alignment.Top
        ) {
            // Columna izquierda: Imagen
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (isMobile) 100.percent else 25.percent)
                    .margin(bottom = if (isMobile) 20.px else 0.px)
                    .padding(16.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier
                        .size(150.px)
                        .margin(bottom = 16.px),
                    src = imageSrc,
                    alt = title
                )

                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .fontStyle(FontStyle.Italic)
                        .color(Theme.Secondary.rgb)
                        .textAlign(TextAlign.Center),
                    text = description
                )
            }

            // Columna derecha: Listado de elementos
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (isMobile) 100.percent else 75.percent)
                    .padding(left = if (isMobile) 0.px else 20.px)
            ) {
                // Solo mostrar el contenido si hay elementos
                if (items.isNotEmpty()) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Medium)
                            .color(Theme.Secondary.rgb)
                            .margin(bottom = 10.px),
                        text = "$title seleccionados"
                    )

                    items.forEachIndexed { index, item ->
                        itemRenderer(item, index)
                    }
                } else {
                    // Mensaje cuando no hay elementos seleccionados
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.px)
                            .backgroundColor(Theme.LightGray.rgb)
                            .borderRadius(8.px),
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .color(Theme.Secondary.rgb)
                                .padding(20.px),
                            text = "No hay $title seleccionados"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Implementación estándar del renderizador de elementos para la mayoría de extras
 */
@Composable
fun StandardItemRenderer(
    item: Extra,
    index: Int,
    quantitySelector: @Composable (Int, (Int) -> Unit) -> Unit,
    onQuantityChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    getDimensionsText: (Extra) -> String = { "" }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 10.px)
            .padding(16.px)
            .backgroundColor(Theme.LightGray.rgb)
            .borderRadius(8.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Datos del elemento
        Column(
            modifier = Modifier.fillMaxWidth(60.percent)
        ) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Theme.Secondary.rgb),
                text = item.tipo
            )

            // Mostrar dimensiones si están disponibles
            val dimensionesText = getDimensionsText(item)
            if (dimensionesText.isNotEmpty()) {
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .color(Theme.Secondary.rgb),
                    text = dimensionesText
                )
            }
        }

        Spacer()

        // Selector de cantidad
        quantitySelector(item.numero) { newQuantity ->
            onQuantityChanged(index, newQuantity)
        }


        // Botón de eliminar
        Box(
            modifier = Modifier
                .margin(left = 10.px)
                .size(30.px)
                .backgroundColor(Colors.Red)
                .flexShrink(0)
                .borderRadius(4.px)
                .cursor(Cursor.Pointer)
                .onClick { onDeleteClick(index) },
            contentAlignment = Alignment.Center
        ) {
            FaTrash(
                modifier = Modifier.color(Colors.White),
                size = IconSize.SM
            )
        }
    }
}

/**
 * Función de utilidad para extraer dimensiones de un string
 */
fun extractDimensions(text: String): Pair<Double, Double> {
    val dimPattern = "(\\d+)[xX×](\\d+)".toRegex()
    val match = dimPattern.find(text)

    val largo = match?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    val ancho = match?.groupValues?.getOrNull(2)?.toDoubleOrNull() ?: 0.0

    return Pair(largo, ancho)
}

// Función que crea un adaptador para QuantitySelector
fun crearSelectorCantidad(min: Int, max: Int): @Composable (Int, (Int) -> Unit) -> Unit {
    return { valor, onCambioValor ->
        QuantitySelector(
            value = valor,
            onValueChange = onCambioValor,
            min = min,
            max = max,
            showText = false // Opcional: no mostrar texto en el contexto de una lista
        )
    }
}