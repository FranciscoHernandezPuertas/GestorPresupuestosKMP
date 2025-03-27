package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.dam.tfg.models.budget.Tramo
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.LoginInputStyle
import org.dam.tfg.styles.TableSelectorStyle
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
    // Inicializamos la mesa desde BudgetManager
    var mesa by remember { mutableStateOf(BudgetManager.loadMesa()) }
    val breakpoint = rememberBreakpoint()
    var errorMessage by remember { mutableStateOf("") }

    // Determinar el número de tramos basado en el tipo de mesa guardado
    var selectedTable by remember {
        val numTramos = when {
            mesa.tipo.contains("4") -> 4
            mesa.tipo.contains("3") -> 3
            mesa.tipo.contains("2") -> 2
            else -> 1
        }
        mutableStateOf(numTramos)
    }

    // Mapa para almacenar las dimensiones de los tramos
    var dimensiones by remember {
        val dims = mutableMapOf<String, String>()
        // Inicializar con valores guardados o vacíos
        for (i in 0 until 4) {
            if (i < mesa.tramos.size) {
                dims["largo${i+1}"] = mesa.tramos[i].largo.toString()
                dims["ancho${i+1}"] = mesa.tramos[i].ancho.toString()
            } else {
                dims["largo${i+1}"] = ""
                dims["ancho${i+1}"] = ""
            }
        }
        mutableStateOf(dims)
    }

    fun validateData(): Boolean {
        // Crear tramos actualizados basados en las dimensiones ingresadas
        val nuevosTramos = (0 until selectedTable).map { i ->
            val largo = dimensiones["largo${i+1}"]?.toDoubleOrNull() ?: 0.0
            val ancho = dimensiones["ancho${i+1}"]?.toDoubleOrNull() ?: 0.0
            Tramo(numero = i + 1, largo = largo, ancho = ancho)
        }

        // Verificar que todos los tramos sean válidos
        val isValid = nuevosTramos.all { it.isValid() }

        if (!isValid) {
            errorMessage = "Por favor, rellene correctamente todos los campos de dimensiones para la mesa de $selectedTable tramo${if (selectedTable > 1) "s" else ""}"
        } else {
            errorMessage = ""
        }

        return isValid
    }

    fun saveData() {
        // Primero guardamos el tipo de mesa
        BudgetManager.setTipoMesa("Mesa ${selectedTable} tramo${if (selectedTable > 1) "s" else ""}")

        // Convertimos las dimensiones en tramos
        val tramosActualizados = mutableListOf<Tramo>()

        // Validamos que tengamos los datos necesarios
        for (i in 1..selectedTable) {
            val largo = dimensiones["largo$i"]?.toDoubleOrNull() ?: 0.0
            val ancho = dimensiones["ancho$i"]?.toDoubleOrNull() ?: 0.0

            // Crear el tramo con sus dimensiones
            tramosActualizados.add(Tramo(
                numero = i,
                largo = largo,
                ancho = ancho
            ))
        }

        // Guardar todos los tramos en el BudgetManager
        BudgetManager.setTramos(tramosActualizados)
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
                        RadioOptions(selectedTable) {
                            selectedTable = it
                            errorMessage = ""
                        }
                    }

                    // Campos de dimensiones
                    Column(
                        modifier = Modifier.width(60.percent).padding(left = 20.px)
                    ) {
                        DimensionFields(
                            selectedTable = selectedTable,
                            dimensiones = dimensiones,
                            onDimensionChange = { key, value ->
                                errorMessage = ""
                                dimensiones = dimensiones.toMutableMap().apply {
                                    this[key] = value
                                }
                            }
                        )
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

                    DimensionFields(selectedTable, dimensiones) { key, value ->
                        dimensiones = dimensiones.toMutableMap().apply {
                            this[key] = value
                        }
                    }
                }
            }

            // Mensaje de error (solo se muestra cuando hay un error)
            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(90.percent)
                        .margin(top = 20.px)
                        .backgroundColor(Colors.LightPink)
                        .borderRadius(4.px)
                        .padding(10.px),
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Colors.Red),
                        text = errorMessage
                    )
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
                modifier = TableSelectorStyle.toModifier()
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
    fun DimensionFields(
        selectedTable: Int,
        dimensiones: Map<String, String>,
        onDimensionChange: (String, String) -> Unit
    ) {
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
                    DimensionField(
                        id = "largo1",
                        label = "Largo tramo 1 (mm)",
                        value = dimensiones["largo1"] ?: "",
                        onValueChange = { onDimensionChange("largo1", it) }
                    )
                }

                if (!isMobile) {
                    Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                        DimensionField(
                            id = "ancho1",
                            label = "Ancho tramo 1 (mm)",
                            value = dimensiones["ancho1"] ?: "",
                            onValueChange = { onDimensionChange("ancho1", it) }
                        )
                    }
                }
            }

            if (isMobile) {
                Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                    DimensionField(
                        id = "ancho1",
                        label = "Ancho tramo 1 (mm)",
                        value = dimensiones["ancho1"] ?: "",
                        onValueChange = { onDimensionChange("ancho1", it) }
                    )
                }
            }

            // Sección 2 (visible si selectedTable >= 2)
            if (selectedTable >= 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(if (isMobile) 100.percent else 50.percent).padding(right = 10.px)) {
                        DimensionField("largo2", "Largo tramo 2 (mm)", dimensiones["largo2"] ?: "") {
                            onDimensionChange("largo2", it)
                        }
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho2", "Ancho tramo 2 (mm)", dimensiones["ancho2"] ?: "") {
                                onDimensionChange("ancho2", it)
                            }
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho2", "Ancho tramo 2 (mm)", dimensiones["ancho2"] ?: "") {
                            onDimensionChange("ancho2", it)
                        }
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
                        DimensionField("largo3", "Largo tramo 3 (mm)", dimensiones["largo3"] ?: "") {
                            onDimensionChange("largo3", it)
                        }
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho3", "Ancho tramo 3 (mm)", dimensiones["ancho3"] ?: "") {
                                onDimensionChange("ancho3", it)
                            }
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho3", "Ancho tramo 3 (mm)", dimensiones["ancho3"] ?: "") {
                            onDimensionChange("ancho3", it)
                        }
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
                        DimensionField("largo4", "Largo tramo 4 (mm)", dimensiones["largo4"] ?: "") {
                            onDimensionChange("largo4", it)
                        }
                    }
                    if (!isMobile) {
                        Column(modifier = Modifier.width(50.percent).padding(left = 10.px)) {
                            DimensionField("ancho4", "Ancho tramo 4 (mm)", dimensiones["ancho4"] ?: "") {
                                onDimensionChange("ancho4", it)
                            }
                        }
                    }
                }

                if (isMobile) {
                    Column(modifier = Modifier.width(100.percent).padding(right = 10.px)) {
                        DimensionField("ancho4", "Ancho tramo 4 (mm)", dimensiones["ancho4"] ?: "") {
                            onDimensionChange("ancho4", it)
                        }
                    }
                }
            }
        }
    }

@Composable
fun DimensionField(
    id: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
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
            attrs = TableSelectorStyle.toModifier()
                .id(id)
                .width(100.percent)
                .height(40.px)
                .padding(leftRight = 15.px)
                .fontSize(14.px)
                .fontFamily(FONT_FAMILY)
                .toAttrs {
                    attr("placeholder", "0")
                    attr("step", "1")
                    attr("min", "0")
                    attr("max", "10000")
                    attr("value", value)
                    onChange { event ->
                        onValueChange(event.target.value)
                    }
                }
        )
    }
}