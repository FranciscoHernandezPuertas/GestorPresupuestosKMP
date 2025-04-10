package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.ValidationError
import org.dam.tfg.constants.ElementosConstantes
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.LimiteTramo
import org.dam.tfg.models.table.TipoTramo
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.styles.RadioButtonStyle
import org.dam.tfg.styles.TableSelectorStyle
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text
@Page
@Composable
fun TableSelectorDimensionsPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorDimensionsContent()
        }
    }
}

@Composable
fun TableSelectorDimensionsContent() {
    val breakpoint = rememberBreakpoint()
    val resourceProvider = remember { WebResourceProvider() }
    var selectedTramoCount by remember { mutableStateOf(1) }
    var tramos by remember { mutableStateOf<List<Tramo>>(listOf()) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(mapOf()) }
    val limites = remember { ElementosConstantes.MESAS_LIMITES }
    var tramosInputValues by remember { mutableStateOf<Map<String, String>>(mapOf()) }

    // Valores responsivos según el breakpoint
    val contentWidth = if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent
    val containerPadding = if (breakpoint >= Breakpoint.MD) 16.px else 10.px
    val titleFontSize = if (breakpoint >= Breakpoint.MD) 24.px else 20.px

    val mesaImagePath = remember { mutableStateOf("") }

    fun generateMesaImageKey(): String {
        val count = tramos.size

        if (count == 1) {
            return "MESA_1TRAMO_" + if (tramos[0].tipo == TipoTramo.CENTRAL) "CENTRAL" else "MURAL"
        }

        val tipoKey = tramos.take(count)
            .map { if (it.tipo == TipoTramo.CENTRAL) "C" else "M" }
            .joinToString("")

        return "MESA_${count}TRAMOS_$tipoKey"
    }

    fun updateMesaImagePath() {
        mesaImagePath.value = resourceProvider.getImagePath(generateMesaImageKey())
    }

    val onTipoTramoChange = { index: Int, tipoTramo: TipoTramo ->
        tramos = tramos.toMutableList().also { list ->
            if (index < list.size) {
                list[index] = list[index].copy(tipo = tipoTramo)
            }
        }
        updateMesaImagePath()
    }

    // Cargar datos guardados (se mantiene igual)
    LaunchedEffect(Unit) {
        val savedTramos = BudgetManager.getMesaTramos()
        val savedTipo = BudgetManager.getMesaTipo()

        if (savedTipo.isNotEmpty()) {
            selectedTramoCount = savedTipo.toIntOrNull() ?: 1
        }

        if (savedTramos.isNotEmpty()) {
            tramos = savedTramos
            val inputValues = mutableMapOf<String, String>()
            savedTramos.forEachIndexed { index, tramo ->
                inputValues["largo_${index + 1}"] = if (tramo.largo > 0) tramo.largo.toInt().toString() else ""
                inputValues["ancho_${index + 1}"] = if (tramo.ancho > 0) tramo.ancho.toInt().toString() else ""
            }
            tramosInputValues = inputValues
        } else {
            tramos = List(selectedTramoCount) { index ->
                Tramo(
                    numero = index + 1,
                    largo = 0.0,
                    ancho = 0.0,
                    tipo = TipoTramo.CENTRAL
                )
            }
        }
    }

    // Actualizar tramos cuando cambia la cantidad (se mantiene igual)
    LaunchedEffect(selectedTramoCount) {
        if (tramos.size != selectedTramoCount) {
            val newTramos = if (selectedTramoCount > tramos.size) {
                tramos + List(selectedTramoCount - tramos.size) { index ->
                    Tramo(
                        numero = tramos.size + index + 1,
                        largo = 0.0,
                        ancho = 0.0,
                        tipo = TipoTramo.CENTRAL
                    )
                }
            } else {
                tramos.take(selectedTramoCount)
            }
            tramos = newTramos

            val inputValues = tramosInputValues.toMutableMap()
            tramos.forEachIndexed { index, tramo ->
                if (!inputValues.containsKey("largo_${index + 1}")) {
                    inputValues["largo_${index + 1}"] = if (tramo.largo > 0) tramo.largo.toInt().toString() else ""
                }
                if (!inputValues.containsKey("ancho_${index + 1}")) {
                    inputValues["ancho_${index + 1}"] = if (tramo.ancho > 0) tramo.ancho.toInt().toString() else ""
                }
            }
            tramosInputValues = inputValues
        }
    }

    LaunchedEffect(selectedTramoCount, tramos) {
        updateMesaImagePath()
    }

    // Actualizar imagen al iniciar el componente
    LaunchedEffect(Unit) {
        updateMesaImagePath()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.px),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .maxWidth(contentWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            SpanText(
                modifier = Modifier
                    .margin(top = 10.px, bottom = 10.px)
                    .fontSize(titleFontSize)
                    .fontWeight(FontWeight.Bold)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.Secondary.rgb),
                text = "Configuración de Mesa: Dimensiones"
            )

            // Contenedor principal con altura dinámica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Usar altura fija en lugar de minHeight
                    .height(if (breakpoint >= Breakpoint.MD) 500.px else (400 + 4 * 140).px) // Altura para acomodar 4 tramos
                    .padding(containerPadding)
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .border(1.px, LineStyle.Solid, Theme.LightGray.rgb)
                    .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                    .margin(bottom = 10.px),
            ) {
                // Layout adaptativo: Row para desktop, Column para móvil
                if (breakpoint >= Breakpoint.MD) {
                    // Layout para desktop (se mantiene igual)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.px),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top // Cambiado de CenterVertically a Top
                    ) {
                        // Columna izquierda - Selección de tramos
                        Column(
                            modifier = Modifier
                                .width(30.percent)
                                .padding(right = 16.px)
                                .padding(6.px),
                            horizontalAlignment = Alignment.Start
                        ) {
                            TramoSelector(selectedTramoCount) { selectedTramoCount = it }
                        }

                        // Columna derecha - Campos para dimensiones
                        Column(
                            modifier = Modifier.width(65.percent),
                            horizontalAlignment = Alignment.Start
                        ) {
                            DimensionFields(
                                tramos = tramos,
                                tramosInputValues = tramosInputValues,
                                selectedTramoCount = selectedTramoCount,
                                limites = limites,
                                validationErrors = validationErrors,
                                isMobile = breakpoint < Breakpoint.MD,
                                onValueChange = { tramoIndex, fieldType, newValue ->
                                    tramosInputValues = tramosInputValues.toMutableMap().apply {
                                        put("${fieldType}_${tramoIndex + 1}", newValue)
                                    }

                                    val value = newValue.toDoubleOrNull() ?: 0.0
                                    val tramoActualizado = if (fieldType == "largo") {
                                        tramos[tramoIndex].copy(largo = value)
                                    } else {
                                        tramos[tramoIndex].copy(ancho = value)
                                    }

                                    tramos = tramos.toMutableList().apply {
                                        this[tramoIndex] = tramoActualizado
                                    }
                                },
                                onTipoTramoChange = onTipoTramoChange  // Aquí pasamos el nuevo parámetro
                            )
                        }
                    }
                } else {
                    // Layout para móvil mejorado con más espacio
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.px),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.px) // Espacio consistente entre elementos
                    ) {
                        // Selección de tramos para móvil
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.px),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TramoSelector(selectedTramoCount) { selectedTramoCount = it }
                        }

                        // Campos para dimensiones en móvil
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.px) // Más espacio entre tramos
                        ) {
                            DimensionFields(
                                tramos = tramos,
                                tramosInputValues = tramosInputValues,
                                selectedTramoCount = selectedTramoCount,
                                limites = limites,
                                validationErrors = validationErrors,
                                isMobile = breakpoint < Breakpoint.MD,
                                onValueChange = { tramoIndex, fieldType, newValue ->
                                    tramosInputValues = tramosInputValues.toMutableMap().apply {
                                        put("${fieldType}_${tramoIndex + 1}", newValue)
                                    }

                                    val value = newValue.toDoubleOrNull() ?: 0.0
                                    val tramoActualizado = if (fieldType == "largo") {
                                        tramos[tramoIndex].copy(largo = value)
                                    } else {
                                        tramos[tramoIndex].copy(ancho = value)
                                    }

                                    tramos = tramos.toMutableList().apply {
                                        this[tramoIndex] = tramoActualizado
                                    }
                                },
                                onTipoTramoChange = onTipoTramoChange
                            )
                        }
                    }
                }
            }

            // Visualización de la mesa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (breakpoint >= Breakpoint.MD) 180.px else 160.px)
                    .padding(if (breakpoint >= Breakpoint.MD) 20.px else 10.px)
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .border(1.px, LineStyle.Solid, Theme.LightGray.rgb)
                    .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                    .margin(bottom = if (breakpoint >= Breakpoint.MD) 120.px else 80.px),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .width(if (breakpoint >= Breakpoint.MD) 350.px else 250.px)
                        .height(if (breakpoint >= Breakpoint.MD) 200.px else 140.px)
                        .padding(10.px),
                    src = mesaImagePath.value,
                    alt = "Mesa con $selectedTramoCount tramos"
                )
            }
        }

        // Footer - mantenerlo igual
        BudgetFooter(
            previousScreen = Screen.Home,
            nextScreen = Screen.TableSelectorElements,
            validateData = {
                val errors = mutableMapOf<String, String>()
                tramos.forEachIndexed { index, tramo ->
                    val clave = if (selectedTramoCount == 1) "1 tramo" else "$selectedTramoCount tramos"
                    val limiteTramo = limites[clave]?.get(index + 1)

                    if (limiteTramo != null) {
                        if (tramo.largo < limiteTramo.minLargo || tramo.largo > limiteTramo.maxLargo) {
                            errors["tramo_${index + 1}"] =
                                "El largo debe estar entre ${limiteTramo.minLargo.toInt()} y ${limiteTramo.maxLargo.toInt()} mm"
                        }
                        if (tramo.ancho < limiteTramo.minAncho || tramo.ancho > limiteTramo.maxAncho) {
                            errors["tramo_${index + 1}"] =
                                "El ancho debe estar entre ${limiteTramo.minAncho.toInt()} y ${limiteTramo.maxAncho.toInt()} mm"
                        }
                        if(tramo.largo < limiteTramo.minLargo && tramo.ancho < limiteTramo.minAncho) {
                            errors["tramo_${index + 1}"] = "El largo y el ancho deben ser mayores que los mínimos"
                        }
                        if(tramo.largo > limiteTramo.maxLargo && tramo.ancho > limiteTramo.maxAncho) {
                            errors["tramo_${index + 1}"] = "El largo y el ancho deben ser menores que los máximos"
                        }
                        if(tramo.largo <= 0 && tramo.ancho <= 0) {
                            errors["tramo_${index + 1}"] = "Debe ingresar un valor para el largo y el ancho"
                        } else if (tramo.largo <= 0) {
                            errors["tramo_${index + 1}"] = "Debe ingresar un valor para el largo"
                        } else if (tramo.ancho <= 0) {
                            errors["tramo_${index + 1}"] = "Debe ingresar un valor para el ancho"
                        }
                    }
                }
                validationErrors = errors
                errors.isEmpty()
            },
            saveData = {
                BudgetManager.saveMesaData(
                    tipoMesa = selectedTramoCount.toString(),
                    tramos = tramos,
                    extras = BudgetManager.getElementosNombres(),
                    precioTotal = 0.0
                )
            }
        )
    }
}

@Composable
fun TramoSelector(selectedTramoCount: Int, onTramoSelected: (Int) -> Unit) {
    SpanText(
        modifier = Modifier
            .margin(bottom = 16.px)
            .fontSize(20.px)
            .fontWeight(FontWeight.Medium)
            .fontFamily(FONT_FAMILY)
            .color(Theme.Secondary.rgb),
        text = "Número de tramos:"
    )

    for (i in 1..4) {
        RadioOption(
            id = "tramos_$i",
            text = "$i ${if (i == 1) "tramo" else "tramos"}",
            selected = selectedTramoCount == i,
            onSelected = { onTramoSelected(i) }
        )
    }
}

@Composable
fun DimensionFields(
    tramos: List<Tramo>,
    tramosInputValues: Map<String, String>,
    selectedTramoCount: Int,
    limites: Map<String, Map<Int, LimiteTramo>>,
    validationErrors: Map<String, String>,
    isMobile: Boolean = false,
    onValueChange: (Int, String, String) -> Unit,
    onTipoTramoChange: (Int, TipoTramo) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (isMobile) 24.px else 10.px),
    ) {
        tramos.forEachIndexed { index, tramo ->
            val clave = if (selectedTramoCount == 1) "1 tramo" else "$selectedTramoCount tramos"
            val limiteTramo = limites[clave]?.get(index + 1)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.px)
            ) {
                // Cabecera con título y radio buttons de tipo de tramo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start // Cambiado de SpaceBetween a Start
                ) {
                    SpanText(
                        modifier = Modifier
                            .margin(right = 16.px) // Añadido margen a la derecha en lugar de margen inferior
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Medium)
                            .fontFamily(FONT_FAMILY)
                            .color(Theme.Secondary.rgb),
                        text = "Tramo ${index + 1}"
                    )

                    // Radio buttons para tipo de tramo (ahora junto al texto)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.px)
                    ) {
                        TipoTramoRadioOption(
                            id = "tramo_${index + 1}_central",
                            text = "Central",
                            selected = tramo.tipo == TipoTramo.CENTRAL,
                            onSelected = { onTipoTramoChange(index, TipoTramo.CENTRAL) }
                        )

                        TipoTramoRadioOption(
                            id = "tramo_${index + 1}_mural",
                            text = "Mural",
                            selected = tramo.tipo == TipoTramo.MURAL,
                            onSelected = { onTipoTramoChange(index, TipoTramo.MURAL) }
                        )
                    }
                }

                if (isMobile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.px)
                    ) {
                        // Campo largo
                        DimensionField(
                            id = "largo_${index + 1}",
                            label = "Largo (mm):",
                            value = tramosInputValues["largo_${index + 1}"] ?: "",
                            minValue = limiteTramo?.minLargo ?: 400.0,
                            maxValue = limiteTramo?.maxLargo ?: 2000.0,
                            isFullWidth = true,
                            onValueChange = { newValue ->
                                onValueChange(index, "largo", newValue)
                            }
                        )

                        // Campo ancho
                        DimensionField(
                            id = "ancho_${index + 1}",
                            label = "Ancho (mm):",
                            value = tramosInputValues["ancho_${index + 1}"] ?: "",
                            minValue = limiteTramo?.minAncho ?: 400.0,
                            maxValue = limiteTramo?.maxAncho ?: 800.0,
                            isFullWidth = true,
                            onValueChange = { newValue ->
                                onValueChange(index, "ancho", newValue)
                            }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Campo largo
                        DimensionField(
                            id = "largo_${index + 1}",
                            label = "Largo (mm):",
                            value = tramosInputValues["largo_${index + 1}"] ?: "",
                            minValue = limiteTramo?.minLargo ?: 400.0,
                            maxValue = limiteTramo?.maxLargo ?: 2000.0,
                            onValueChange = { newValue ->
                                onValueChange(index, "largo", newValue)
                            }
                        )

                        // Campo ancho
                        DimensionField(
                            id = "ancho_${index + 1}",
                            label = "Ancho (mm):",
                            value = tramosInputValues["ancho_${index + 1}"] ?: "",
                            minValue = limiteTramo?.minAncho ?: 400.0,
                            maxValue = limiteTramo?.maxAncho ?: 800.0,
                            onValueChange = { newValue ->
                                onValueChange(index, "ancho", newValue)
                            }
                        )
                    }
                }

                // Mensaje de error si existe
                validationErrors["tramo_${index + 1}"]?.let { error ->
                    ValidationError(error)
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
    minValue: Double = 0.0,
    maxValue: Double = 10000.0,
    isFullWidth: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(if (isFullWidth) 100.percent else 48.percent)
            .margin(bottom = 2.px)
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
            SpanText(
                text = label,
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .color(Theme.Secondary.rgb)
            )
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
                    attr("placeholder", minValue.toInt().toString())
                    attr("step", "1")
                    attr("min", minValue.toInt().toString())
                    attr("max", maxValue.toInt().toString())
                    attr("value", value)
                    onChange { event ->
                        onValueChange(event.target.value)
                    }
                }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SpanText(
                text = "Mín: ${minValue.toInt()}mm",
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(10.px)
                    .color(Theme.Secondary.rgb)
                    .margin(top = 1.px)
            )

            SpanText(
                text = "Máx: ${maxValue.toInt()}mm",
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(10.px)
                    .color(Theme.Secondary.rgb)
                    .margin(top = 1.px)
            )
        }
    }
}

@Composable
fun RadioOption(
    id: String,
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.margin(bottom = 12.px)
    ) {
        Box(
            modifier = Modifier.width(20.px).height(20.px)
        ) {
            // Input radio real pero escondido
            Input(
                type = InputType.Radio,
                attrs = Modifier
                    .id(id)
                    .toAttrs {
                        name("tramos")
                        checked(selected)
                        onChange { onSelected() }
                        style {
                            property("opacity", "0")
                            property("position", "absolute")
                        }
                    }
            )

            // Círculo personalizado visible
            Box(
                modifier = TableSelectorStyle.toModifier()
                    .width(20.px)
                    .height(20.px)
                    .border(
                        width = 2.px,
                        style = LineStyle.Solid,
                        color = if (selected) Theme.Primary.rgb else Theme.HalfBlack.rgb
                    )
                    .borderRadius(50.percent)
                    .backgroundColor(Colors.White),
                contentAlignment = Alignment.Center  // Añadir centrado aquí
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .width(12.px)
                            .height(12.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(50.percent)
                    )
                }
            }
        }

        Label(
            attrs = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .margin(left = 8.px)
                .color(Theme.Secondary.rgb)
                .toAttrs {
                    attr("for", id)
                }
        ) {
            Text(text)
        }
    }
}

@Composable
fun TipoTramoRadioOption(
    id: String,
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(20.px).height(20.px)
        ) {
            // Input radio real pero escondido
            Input(
                type = InputType.Radio,
                attrs = Modifier
                    .id(id)
                    .toAttrs {
                        name(id)
                        checked(selected)
                        onChange { onSelected() }
                        style {
                            property("opacity", "0")
                            property("position", "absolute")
                        }
                    }
            )

            // Círculo personalizado visible
            Box(
                modifier = TableSelectorStyle.toModifier()
                    .width(20.px)
                    .height(20.px)
                    .border(
                        width = 2.px,
                        style = LineStyle.Solid,
                        color = if (selected) Theme.Primary.rgb else Theme.HalfBlack.rgb
                    )
                    .borderRadius(50.percent)
                    .backgroundColor(Colors.White),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .width(12.px)
                            .height(12.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(50.percent)
                    )
                }
            }
        }

        Label(
            attrs = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .margin(left = 8.px)
                .color(Theme.Secondary.rgb)
                .toAttrs {
                    attr("for", id)
                }
        ) {
            Text(text)
        }
    }
}