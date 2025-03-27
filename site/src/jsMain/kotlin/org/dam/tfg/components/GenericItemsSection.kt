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
import org.dam.tfg.models.budget.Cubeta
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select

/**
 * Componente genérico para selección de elementos
 */
@Composable
fun <T> GenericItemsSection(
    title: String,
    description: String,
    imageSrc: String,
    items: List<T>,
    itemOptions: List<String>,
    onItemAdded: (String, Int, Double, Double) -> Unit,
    onQuantityChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    itemRenderer: @Composable (T, Int) -> Unit,
    itemCreator: (String, Int, Double, Double) -> T
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
                            // Extraer dimensiones de manera robusta
                            val dimPattern = "(\\d+)[xX×](\\d+)".toRegex()
                            val match = dimPattern.find(selectedOption)

                            val largo = match?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 500.0
                            val ancho = match?.groupValues?.getOrNull(2)?.toDoubleOrNull() ?: 400.0

                            onItemAdded(selectedOption, 1, largo, ancho)
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
 * Implementación específica para cubetas
 */
@Composable
fun CubetasSection(
    cubetas: List<Cubeta>,
    cubetasPredefinidas: List<String>,
    onCubetaAdded: (Cubeta) -> Unit,
    onCantidadChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    GenericItemsSection(
        title = "Cubetas",
        description = "Seleccione cubetas desde el desplegable superior",
        imageSrc = Res.Image.cubeta,
        items = cubetas,
        itemOptions = cubetasPredefinidas,
        onItemAdded = { tipo, numero, largo, ancho ->
            onCubetaAdded(Cubeta(
                tipo = tipo,
                numero = numero,
                largo = largo,
                ancho = ancho
            ))
        },
        onQuantityChanged = onCantidadChanged,
        onDeleteClick = onDeleteClick,
        itemCreator = { tipo, numero, largo, ancho ->
            Cubeta(tipo = tipo, numero = numero, largo = largo, ancho = ancho)
        },
        itemRenderer = { cubeta, index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .margin(bottom = 10.px)
                    .padding(16.px)
                    .backgroundColor(Theme.LightGray.rgb)
                    .borderRadius(8.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Datos de la cubeta
                Column(
                    modifier = Modifier.fillMaxWidth(60.percent)
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Medium)
                            .color(Theme.Secondary.rgb),
                        text = cubeta.tipo
                    )

                    // Extraer dimensiones del nombre de la cubeta para mostrar
                    val dimensiones = cubeta.tipo.replace(Regex(".*?(\\d+[xX×]\\d+([xX×]\\d+)?).*"), "$1")

                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .color(Theme.Secondary.rgb),
                        text = "Dimensiones: $dimensiones mm"
                    )
                }

                Spacer()

                // Selector de cantidad
                QuantitySelector(
                    value = cubeta.numero,
                    onValueChange = { cantidad ->
                        onCantidadChanged(index, cantidad)
                    },
                    min = 1,
                    max = 5
                )

                // Botón de eliminar
                Box(
                    modifier = Modifier
                        .margin(left = 15.px)
                        .size(30.px)
                        .backgroundColor(Colors.Red)
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
    )
}

/**
 * QuantitySelector modularizado
 */
@Composable
fun QuantitySelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 10
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .fontWeight(FontWeight.Normal)
                .color(Theme.Secondary.rgb),
            text = "Cantidad: "
        )
        Box(
            modifier = Modifier
                .size(30.px)
                .backgroundColor(if (value > min) Theme.Primary.rgb else Theme.LightGray.rgb)
                .borderRadius(4.px)
                .cursor(if (value > min) Cursor.Pointer else Cursor.NotAllowed)
                .onClick { if (value > min) onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            FaMinus(
                modifier = Modifier.color(Colors.White),
                size = IconSize.SM
            )
        }

        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .color(Theme.Secondary.rgb)
                .margin(leftRight = 15.px)
                .width(20.px)
                .textAlign(TextAlign.Center),
            text = value.toString()
        )

        Box(
            modifier = Modifier
                .size(30.px)
                .backgroundColor(if (value < max) Theme.Primary.rgb else Theme.LightGray.rgb)
                .borderRadius(4.px)
                .cursor(if (value < max) Cursor.Pointer else Cursor.NotAllowed)
                .onClick { if (value < max) onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            FaPlus(
                modifier = Modifier.color(Colors.White),
                size = IconSize.SM
            )
        }
    }
}