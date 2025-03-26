package org.dam.tfg.pages.budget.table

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
import com.varabyte.kobweb.compose.foundation.layout.Spacer
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
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.outline
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.toModifier
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.LoginInputStyle
import org.dam.tfg.util.BudgetManager
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label

@Page
@Composable
fun TableSelectorPage() {
    isUserLoggedInCheck {
        TableSelectorPageContent()
    }
}

@Composable
fun TableSelectorPageContent() {
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
            // Contenido específico para usuarios
            TableSelectorContent()
        }
    }
}

    @Composable
    fun TableSelectorContent() {
        var mesa by remember { mutableStateOf(BudgetManager.loadMesa()) }
        val breakpoint = rememberBreakpoint()
        var selectedTable by remember { mutableStateOf(1) }

        fun validateData(): Boolean {
            return mesa.tramos.all { it.isValid() }
        }

        fun saveData() {
            BudgetManager.updateMesa(mesa)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(title = "Selección de Mesa")

            SpanText(
                modifier = Modifier
                    .margin(top = 40.px, bottom = 30.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(24.px)
                    .fontWeight(FontWeight.Bold)
                    .color(Theme.Secondary.rgb),
                text = "Seleccione el tipo de mesa"
            )

            // Contenedor principal
            if (breakpoint > Breakpoint.MD) {
                // Vista desktop
                Row(
                    modifier = Modifier
                        .fillMaxWidth(80.percent)
                        .maxWidth(1200.px)
                        .height(400.px)
                        .margin(bottom = 2.px)
                        .padding(20.px)
                        .backgroundColor(Colors.White)
                        .borderRadius(8.px)
                        .boxShadow(
                            offsetX = 0.px,
                            offsetY = 2.px,
                            blurRadius = 4.px,
                            color = Colors.LightGray
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Opciones de selección
                    Column(
                        modifier = Modifier.width(40.percent).padding(right = 20.px)
                    ) {
                        RadioOptions(selectedTable) { selectedTable = it }
                    }

                    // Campos de dimensiones
                    Column(
                        modifier = Modifier.width(60.percent).padding(left = 20.px)
                    ) {
                        DimensionFields(selectedTable)
                    }
                }
            } else {
                // Vista móvil
                Column(
                    modifier = Modifier
                        .fillMaxWidth(90.percent)
                        .margin(bottom = 30.px)
                        .padding(20.px)
                        .backgroundColor(Colors.White)
                        .borderRadius(8.px)
                        .boxShadow(
                            offsetX = 0.px,
                            offsetY = 2.px,
                            blurRadius = 4.px,
                            color = Colors.LightGray
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioOptions(selectedTable) { selectedTable = it }

                    Spacer()

                    DimensionFields(selectedTable)
                }
            }

            // Imagen de la mesa
            Box(
                modifier = Modifier
                    .margin(top = 2.px, bottom = 80.px)
                    .fillMaxWidth(80.percent)
                    .maxWidth(600.px),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    src = when(selectedTable) {
                        1 -> Res.Image.mesaTramos1
                        2 -> Res.Image.mesaTramos2
                        3 -> Res.Image.mesaTramos3
                        4 -> Res.Image.mesaTramos4
                        else -> Res.Image.noSeleccionado
                    },
                    alt = "Mesa de ${selectedTable} tramo${if (selectedTable > 1) "s" else ""}"
                )
            }

            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                BudgetFooter(
                    previousScreen = Screen.Home,
                    nextScreen = Screen.TableElements,
                    validateData = { validateData() },
                    saveData = { saveData() }
                )
            }
        }
    }
    @Composable
    fun RadioOptions(selectedTable: Int, onTableSelected: (Int) -> Unit) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.px),
            verticalArrangement = Arrangement.spacedBy(15.px)
        ) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Theme.Secondary.rgb)
                    .margin(bottom = 10.px),
                text = "Tipo de mesa:"
            )

            // Radio buttons para los tipos de mesa
            RadioButtonWithLabel(
                id = "table1",
                label = "Mesa 1 tramo",
                isSelected = selectedTable == 1,
                onClick = { onTableSelected(1) }
            )

            RadioButtonWithLabel(
                id = "table2",
                label = "Mesa 2 tramos",
                isSelected = selectedTable == 2,
                onClick = { onTableSelected(2) }
            )

            RadioButtonWithLabel(
                id = "table3",
                label = "Mesa 3 tramos",
                isSelected = selectedTable == 3,
                onClick = { onTableSelected(3) }
            )

            RadioButtonWithLabel(
                id = "table4",
                label = "Mesa 4 tramos",
                isSelected = selectedTable == 4,
                onClick = { onTableSelected(4) }
            )
        }
    }

    @Composable
    fun RadioButtonWithLabel(id: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cursor(Cursor.Pointer)
                .onClick { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.px)
                    .border(
                        width = 2.px,
                        style = LineStyle.Solid,
                        color = if (isSelected) Theme.Primary.rgb else Theme.Secondary.rgb
                    )
                    .borderRadius(50.percent)
                    .margin(right = 10.px),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(50.percent)
                    )
                }
            }

            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .color(Theme.Secondary.rgb),
                text = label
            )
        }
    }

    @Composable
    fun DimensionFields(selectedTable: Int) {
        val breakpoint = rememberBreakpoint()
        val isMobile = breakpoint <= Breakpoint.MD

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.px)
        ) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Theme.Secondary.rgb)
                    .margin(bottom = 10.px),
                text = "Dimensiones de la encimera:"
            )

            // Sección 1 (siempre visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.width(if (isMobile) 100.percent else 50.percent).padding(right = 10.px)) {
                    DimensionField("largo1", "Largo tramo 1 (mm)")
                }
                if (!isMobile) {
                    Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                        DimensionField("ancho1", "Ancho tramo 1 (mm)")
                    }
                }
            }

            if (isMobile) {
                Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                    DimensionField("ancho1", "Ancho tramo 1 (mm)")
                }
            }

            // Sección 2 (visible si selectedTable >= 2)
            if (selectedTable >= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(if (isMobile) 100.percent else 50.percent).padding(right = 10.px)) {
                        DimensionField("largo2", "Largo tramo 2 (mm)")
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho2", "Ancho tramo 2 (mm)")
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho2", "Ancho tramo 2 (mm)")
                    }
                }
            }

            // Sección 3 (visible si selectedTable >= 3)
            if (selectedTable >= 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(if (isMobile) 100.percent else 50.percent).padding(right = 10.px)) {
                        DimensionField("largo3", "Largo tramo 3 (mm)")
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho3", "Ancho tramo 3 (mm)")
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho3", "Ancho tramo 3 (mm)")
                    }
                }
            }

            // Sección 4 (visible si selectedTable == 4)
            if (selectedTable >= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(if (isMobile) 100.percent else 50.percent).padding(right = 10.px)) {
                        DimensionField("largo4", "Largo tramo 4 (mm)")
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho4", "Ancho tramo 4 (mm)")
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho4", "Ancho tramo 4 (mm)")
                    }
                }
            }
        }
    }

    @Composable
    fun DimensionField(id: String, label: String) {
        Column(
            modifier = Modifier.fillMaxWidth().margin(bottom = 10.px)
        ) {
            Label(
                attrs = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .color(Theme.Secondary.rgb)
                    .margin(bottom = 5.px)
                    .toAttrs(),
                forId = id
            ) {
                SpanText(text = label)
            }

            Input(
                type = InputType.Number,
                attrs = LoginInputStyle.toModifier()
                    .id(id)
                    .width(100.percent)
                    .height(40.px)
                    .padding(leftRight = 15.px)
                    .backgroundColor(Colors.White)
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .outline(
                        width = 0.px,
                        style = LineStyle.None,
                        color = Colors.Transparent
                    )
                    .toAttrs {
                        attr("placeholder", "0")
                        attr("step", "1")
                        attr("min", "0")
                        attr("max", "10000")
                    }
            )
        }
    }