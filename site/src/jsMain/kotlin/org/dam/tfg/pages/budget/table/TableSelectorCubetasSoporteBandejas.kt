package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
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
import com.varabyte.kobweb.compose.ui.modifiers.bottom
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxHeight
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.right
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleInfo
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.ConfirmationDialog
import org.dam.tfg.components.QuantitySelector
import org.dam.tfg.constants.ElementosConstantes
import org.dam.tfg.di.DependencyProvider
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.services.DimensionService
import org.dam.tfg.styles.DropdownItemStyle
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.DimensionExtractors
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba

@Page
@Composable
fun TableSelectorCubetasSoporteBandejasPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorCubetasSoporteBandejasContent()

        }
    }
}

@Composable
fun TableSelectorCubetasSoporteBandejasContent() {
    val context = rememberPageContext()
    val breakpoint = rememberBreakpoint()
    val resourceProvider = remember { WebResourceProvider() }
    val dimensionService = remember { DimensionService() }

    // Estado para las cubetas y soportes de bandejas
    var cubetasSeleccionadas by remember { mutableStateOf<List<Cubeta>>(emptyList()) }
    var soportesSeleccionados by remember { mutableStateOf<List<Cubeta>>(emptyList()) }

    // Estados para confirmaciones y mensajes
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var elementoAEliminar by remember { mutableStateOf<Pair<Cubeta, Boolean>?>(null) } // Cubeta, esCubeta
    var mensajeError by remember { mutableStateOf("") }
    var mostrarMensajeError by remember { mutableStateOf(false) }

    // Calcular dimensiones y área disponible
    val tramos = remember(Unit) { BudgetManager.getMesaTramos() }
    val areaTotalMesa = remember(tramos) { BudgetManager.calcularAreaTotalMesa() }

    // Calcular dimensiones máximas disponibles de la mesa
    val dimensionesDisponibles = remember(tramos) {
        if (tramos.isEmpty()) {
            Pair(0.0, 0.0)
        } else {
            val maxLargo = tramos.maxOf { it.largo }
            val maxAncho = tramos.maxOf { it.ancho }
            Pair(maxLargo, maxAncho)
        }
    }

    // Opciones disponibles filtradas según espacio
    var opcionesCubetasDisponibles by remember { mutableStateOf<List<ItemWithLimits>>(emptyList()) }
    var opcionesSoportesDisponibles by remember { mutableStateOf<List<ItemWithLimits>>(emptyList()) }

    // Función para actualizar las opciones disponibles
    fun actualizarOpcionesDisponibles() {
        // Convertir las entradas del mapa a lista de ItemWithLimits para cubetas
        val cubetasItems = ElementosConstantes.LIMITES_CUBETAS.map { (nombre, item) ->
            ItemWithLimits(
                id = item.id.ifEmpty { "cubeta_${nombre.replace(" ", "_").lowercase()}" },
                name = nombre,
                minQuantity = item.minQuantity,
                maxQuantity = item.maxQuantity,
                initialQuantity = item.initialQuantity
            )
        }

        // Filtrar las opciones de cubetas disponibles
        opcionesCubetasDisponibles = dimensionService.filtrarOpcionesPorDimensiones(
            opciones = cubetasItems,
            itemsActuales = cubetasSeleccionadas,
            dimensionesDisponibles = dimensionesDisponibles,
            extractorDimensiones = { DimensionExtractors.extractCubetaDimensions(it) }
        )

        // Supongamos que tenemos un mapa similar para soportes de bandejas (añadirlo a ElementosConstantes si no existe)
        // Aquí usaré el mismo pero en una situación real se usaría el mapa correspondiente
        val soportesItems = ElementosConstantes.LIMITES_CUBETAS.map { (nombre, item) ->
            ItemWithLimits(
                id = "soporte_${item.id.ifEmpty { nombre.replace(" ", "_").lowercase() }}",
                name = "Soporte ${nombre}",
                minQuantity = item.minQuantity,
                maxQuantity = item.maxQuantity,
                initialQuantity = item.initialQuantity
            )
        }

        // Filtrar las opciones de soportes disponibles
        opcionesSoportesDisponibles = dimensionService.filtrarOpcionesPorDimensiones(
            opciones = soportesItems,
            itemsActuales = soportesSeleccionados,
            dimensionesDisponibles = dimensionesDisponibles,
            extractorDimensiones = { DimensionExtractors.extractCubetaDimensions(it.removePrefix("Soporte ")) }
        )
    }

    // Cargar datos guardados y actualizar opciones
    LaunchedEffect(Unit) {
        // Intentar cargar cubetas guardadas
        val cubetasGuardadas = BudgetManager.getCubetas()
        if (cubetasGuardadas.isNotEmpty()) {
            cubetasSeleccionadas = cubetasGuardadas
        }

        // Actualizar opciones disponibles
        actualizarOpcionesDisponibles()
    }

    // Actualizar opciones cada vez que cambian las selecciones
    LaunchedEffect(cubetasSeleccionadas, soportesSeleccionados) {
        actualizarOpcionesDisponibles()
    }

    // Funciones para añadir y eliminar elementos
    fun añadirCubeta(nombre: String) {
        val dimensiones = DimensionExtractors.extractCubetaDimensions(nombre)
        val largo = dimensiones.first
        val ancho = dimensiones.second

        if (BudgetManager.agregarCubeta(nombre, largo, ancho)) {
            // Éxito - actualizar la lista local
            val nuevaCubeta = Cubeta(
                tipo = nombre,
                numero = 1,
                largo = largo,
                ancho = ancho,
                maxQuantity = opcionesCubetasDisponibles.find { it.name == nombre }?.maxQuantity ?: 1
            )
            cubetasSeleccionadas = cubetasSeleccionadas + nuevaCubeta
        } else {
            // Error - mostrar mensaje
            mensajeError = "No hay suficiente espacio disponible para añadir esta cubeta"
            mostrarMensajeError = true
        }
    }

    fun añadirSoporte(nombre: String) {
        val nombreBaseCubeta = nombre.removePrefix("Soporte ")
        val dimensiones = DimensionExtractors.extractCubetaDimensions(nombreBaseCubeta)
        val largo = dimensiones.first
        val ancho = dimensiones.second

        val nuevoSoporte = Cubeta(
            tipo = nombre,
            numero = 1,
            largo = largo,
            ancho = ancho,
            maxQuantity = opcionesSoportesDisponibles.find { it.name == nombre }?.maxQuantity ?: 1
        )
        soportesSeleccionados = soportesSeleccionados + nuevoSoporte
    }

    fun actualizarCantidadCubeta(cubeta: Cubeta, nuevaCantidad: Int) {
        cubetasSeleccionadas = cubetasSeleccionadas.map {
            if (it == cubeta) it.copy(numero = nuevaCantidad) else it
        }
    }

    fun actualizarCantidadSoporte(soporte: Cubeta, nuevaCantidad: Int) {
        soportesSeleccionados = soportesSeleccionados.map {
            if (it == soporte) it.copy(numero = nuevaCantidad) else it
        }
    }

    fun eliminarCubeta(cubeta: Cubeta) {
        cubetasSeleccionadas = cubetasSeleccionadas.filter { it != cubeta }
    }

    fun eliminarSoporte(soporte: Cubeta) {
        soportesSeleccionados = soportesSeleccionados.filter { it != soporte }
    }

    // Función para guardar datos
    fun guardarSelecciones(): Boolean {
        // Guardar cubetas
        val todasLasCubetas = cubetasSeleccionadas + soportesSeleccionados
        // Puedes implementar lógica personalizada para guardar aquí si es necesario
        return true
    }

    // Calcular valores responsivos
    val contentWidth = if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent
    val isFlexColumn = breakpoint < Breakpoint.MD

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .maxWidth(contentWidth)
                .padding(top = 20.px)
        ) {
            // Título y descripción
            SpanText(
                modifier = Modifier
                    .fillMaxWidth()
                    .fontFamily(FONT_FAMILY)
                    .fontSize(24.px)
                    .fontWeight(FontWeight.Bold)
                    .color(Theme.Secondary.rgb)
                    .textAlign(TextAlign.Center)
                    .margin(bottom = 20.px),
                text = "Selección de Cubetas y Soportes"
            )

            // Información del área disponible
            InfoAreaDisponible(
                areaTotalMesa = areaTotalMesa,
                areaOcupadaCubetas = cubetasSeleccionadas.sumOf { it.largo * it.ancho * it.numero },
                areaOcupadaSoportes = soportesSeleccionados.sumOf { it.largo * it.ancho * it.numero },
                dimensionesMax = dimensionesDisponibles
            )

            // Layout de selección - adaptable a móvil/escritorio
            if (isFlexColumn) {
                // Versión móvil (apilada)
                SelectorElementos(
                    titulo = "Cubetas",
                    imagen = resourceProvider.getImagePath("CUBETA"),
                    opciones = opcionesCubetasDisponibles,
                    elementosSeleccionados = cubetasSeleccionadas,
                    onAñadir = { añadirCubeta(it) },
                    onActualizarCantidad = { cubeta, cantidad -> actualizarCantidadCubeta(cubeta, cantidad) },
                    onEliminar = {
                        elementoAEliminar = Pair(it, true)
                        mostrarConfirmacionEliminar = true
                    },
                    breakpoint = breakpoint,
                    resourceProvider = resourceProvider
                )

                Spacer()

                SelectorElementos(
                    titulo = "Soportes de Bandejas",
                    imagen = resourceProvider.getImagePath("CUBETA"), // Usar imagen apropiada
                    opciones = opcionesSoportesDisponibles,
                    elementosSeleccionados = soportesSeleccionados,
                    onAñadir = { añadirSoporte(it) },
                    onActualizarCantidad = { soporte, cantidad -> actualizarCantidadSoporte(soporte, cantidad) },
                    onEliminar = {
                        elementoAEliminar = Pair(it, false)
                        mostrarConfirmacionEliminar = true
                    },
                    breakpoint = breakpoint,
                    resourceProvider = resourceProvider
                )
            } else {
                // Versión escritorio (lado a lado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.width(48.percent)) {
                        SelectorElementos(
                            titulo = "Cubetas",
                            imagen = resourceProvider.getImagePath("CUBETA"),
                            opciones = opcionesCubetasDisponibles,
                            elementosSeleccionados = cubetasSeleccionadas,
                            onAñadir = { añadirCubeta(it) },
                            onActualizarCantidad = { cubeta, cantidad -> actualizarCantidadCubeta(cubeta, cantidad) },
                            onEliminar = {
                                elementoAEliminar = Pair(it, true)
                                mostrarConfirmacionEliminar = true
                            },
                            breakpoint = breakpoint,
                            resourceProvider = resourceProvider
                        )
                    }

                    Box(modifier = Modifier.width(48.percent)) {
                        SelectorElementos(
                            titulo = "Soportes de Bandejas",
                            imagen = resourceProvider.getImagePath("CUBETA"), // Usar imagen apropiada
                            opciones = opcionesSoportesDisponibles,
                            elementosSeleccionados = soportesSeleccionados,
                            onAñadir = { añadirSoporte(it) },
                            onActualizarCantidad = { soporte, cantidad -> actualizarCantidadSoporte(soporte, cantidad) },
                            onEliminar = {
                                elementoAEliminar = Pair(it, false)
                                mostrarConfirmacionEliminar = true
                            },
                            breakpoint = breakpoint,
                            resourceProvider = resourceProvider
                        )
                    }
                }
            }
        }

        // Diálogo de confirmación para eliminar
        if (mostrarConfirmacionEliminar && elementoAEliminar != null) {
            ConfirmationDialog(
                mensaje = "¿Está seguro que desea eliminar ${elementoAEliminar!!.first.tipo}?",
                onConfirm = {
                    if (elementoAEliminar!!.second) {
                        eliminarCubeta(elementoAEliminar!!.first)
                    } else {
                        eliminarSoporte(elementoAEliminar!!.first)
                    }
                    mostrarConfirmacionEliminar = false
                    elementoAEliminar = null
                },
                onCancel = {
                    mostrarConfirmacionEliminar = false
                    elementoAEliminar = null
                }
            )
        }

        // Mensaje de error
        if (mostrarMensajeError) {
            MensajeError(
                mensaje = mensajeError,
                onDismiss = { mostrarMensajeError = false }
            )
        }
    }

    // Footer con navegación
    BudgetFooter(
        previousScreen = Screen.TableSelectorElements,
        nextScreen = Screen.TableSelectorDimensions,
        validateData = { guardarSelecciones() },
        saveData = { /* Opcional: lógica adicional al guardar */ }
    )
}

@Composable
fun InfoAreaDisponible(
    areaTotalMesa: Double,
    areaOcupadaCubetas: Double,
    areaOcupadaSoportes: Double,
    dimensionesMax: Pair<Double, Double>
) {
    val areaTotal = areaTotalMesa
    val areaOcupada = areaOcupadaCubetas + areaOcupadaSoportes
    val porcentajeOcupado = if (areaTotal > 0) ((areaOcupada / areaTotal) * 100).toInt() else 0
    val colorPorcentaje = when {
        porcentajeOcupado < 50 -> Colors.Green
        porcentajeOcupado < 80 -> Colors.Orange
        else -> Colors.Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(topBottom = 15.px)
            .backgroundColor(rgba(0, 0, 0, 0.05f))
            .borderRadius(8.px)
            .padding(15.px)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.margin(bottom = 8.px)
            ) {
                FaCircleInfo()
                SpanText(
                    modifier = Modifier
                        .margin(left = 8.px)
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Bold),
                    text = "Información del espacio"
                )
            }

            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px),
                text = "Dimensiones máximas disponibles: ${dimensionesMax.first.toInt()} x ${dimensionesMax.second.toInt()} mm"
            )

            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px),
                text = "Área total de la mesa: ${areaTotal.toInt()} mm²"
            )

            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px),
                text = "Área ocupada por cubetas: ${areaOcupadaCubetas.toInt()} mm²"
            )

            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px),
                text = "Área ocupada por soportes: ${areaOcupadaSoportes.toInt()} mm²"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.margin(top = 8.px)
            ) {
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .fontWeight(FontWeight.Bold),
                    text = "Espacio ocupado: "
                )

                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .fontWeight(FontWeight.Bold)
                        .color(colorPorcentaje),
                    text = "$porcentajeOcupado%"
                )
            }

            // Barra de progreso visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.px)
                    .margin(top = 8.px)
                    .backgroundColor(rgba(0, 0, 0, 0.1f))
                    .borderRadius(5.px)
            ) {
                Box(
                    modifier = Modifier
                        .width(porcentajeOcupado.percent)
                        .height(10.px)
                        .backgroundColor(colorPorcentaje)
                        .borderRadius(5.px)
                )
            }
        }
    }
}

@Composable
fun SelectorElementos(
    titulo: String,
    imagen: String,
    opciones: List<ItemWithLimits>,
    elementosSeleccionados: List<Cubeta>,
    onAñadir: (String) -> Unit,
    onActualizarCantidad: (Cubeta, Int) -> Unit,
    onEliminar: (Cubeta) -> Unit,
    breakpoint: Breakpoint,
    resourceProvider: WebResourceProvider
) {
    val isMobile = breakpoint < Breakpoint.MD
    var seleccionActual by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(topBottom = 15.px)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 5.px, color = rgba(0, 0, 0, 0.1f))
            .padding(15.px)
    ) {
        // Título del selector
        SpanText(
            modifier = Modifier
                .fillMaxWidth()
                .fontFamily(FONT_FAMILY)
                .fontSize(18.px)
                .fontWeight(FontWeight.Bold)
                .color(Theme.Primary.rgb)
                .textAlign(TextAlign.Center)
                .margin(bottom = 15.px),
            text = titulo
        )

        // Sección de selección y añadir
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Image(
                src = imagen,
                modifier = Modifier
                    .width(if (isMobile) 60.px else 80.px)
                    .margin(right = 15.px)
            )

            // Desplegable para seleccionar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .margin(right = 15.px)
                    .border(width = 1.px, style = LineStyle.Solid, color = rgba(0, 0, 0, 0.2f))
                    .borderRadius(4.px)
                    .padding(10.px)
            ) {
                androidx.compose.runtime.key(opciones) {
                    DropdownSelector(
                        opciones = opciones.map { it.name },
                        seleccionActual = seleccionActual,
                        onSeleccion = { seleccionActual = it }
                    )
                }
            }

            // Botón de añadir
            Box(
                modifier = Modifier
                    .backgroundColor(if (seleccionActual.isEmpty()) Colors.LightGray else Theme.Primary.rgb)
                    .borderRadius(4.px)
                    .padding(10.px)
                    .cursor(if (seleccionActual.isEmpty()) Cursor.Default else Cursor.Pointer)
                    .onClick { if (seleccionActual.isNotEmpty()) onAñadir(seleccionActual) },
                contentAlignment = Alignment.Center
            ) {
                FaPlus(
                    modifier = Modifier.color(Colors.White)
                )
            }
        }

        // Lista de elementos seleccionados
        if (elementosSeleccionados.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .margin(top = 20.px)
            ) {
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Bold)
                        .margin(bottom = 10.px),
                    text = "Elementos seleccionados:"
                )

                elementosSeleccionados.forEach { elemento ->
                    ElementoSeleccionadoItem(
                        elemento = elemento,
                        onCantidadChange = { nuevaCantidad -> onActualizarCantidad(elemento, nuevaCantidad) },
                        onEliminar = { onEliminar(elemento) },
                        isMobile = isMobile
                    )
                }
            }
        } else {
            // Mensaje cuando no hay elementos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .margin(top = 20.px)
                    .padding(topBottom = 15.px),
                contentAlignment = Alignment.Center
            ) {
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .color(rgba(0, 0, 0, 0.5f)),
                    text = "No hay elementos seleccionados"
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(
    opciones: List<String>,
    seleccionActual: String,
    onSeleccion: (String) -> Unit
) {
    var mostrarOpciones by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .position(Position.Relative)
    ) {
        // Selector visible
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.px, style = LineStyle.Solid, color = rgba(0, 0, 0, 0.2f))
                .borderRadius(4.px)
                .padding(10.px)
                .cursor(Cursor.Pointer)
                .onClick { mostrarOpciones = !mostrarOpciones },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SpanText(
                text = seleccionActual.ifEmpty { "Seleccionar..." },
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .color(if (seleccionActual.isEmpty()) Colors.Gray else Colors.Black)
            )

            // Flecha indicadora
            SpanText(
                text = if (mostrarOpciones) "▲" else "▼",
                modifier = Modifier.fontSize(12.px)
            )
        }

        // Menú desplegable
        if (mostrarOpciones) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .zIndex(99)
                    .onClick { mostrarOpciones = false }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .position(Position.Absolute)
                    .zIndex(100)
                    .top(100.percent)
                    .left(0.px)
                    .right(0.px)
                    .backgroundColor(Colors.White)
                    .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = rgba(0, 0, 0, 0.2f))
                    .border(1.px, style = LineStyle.Solid, color = rgba(0, 0, 0, 0.1f))
                    .borderRadius(4.px)
                    .maxHeight(200.px)
                    .overflow(Overflow.Auto)
                    .onClick { it.stopPropagation() },
                verticalArrangement = Arrangement.Top
            ) {
                opciones.forEach { opcion ->
                    Box(
                        modifier = DropdownItemStyle.toModifier()
                            .fillMaxWidth()
                            .padding(10.px)
                            .cursor(Cursor.Pointer)
                            .onClick {
                                onSeleccion(opcion)
                                mostrarOpciones = false
                            }
                            .backgroundColor(
                                if (opcion == seleccionActual) rgba(0, 0, 0, 0.05f) else Colors.Transparent
                            )
                    ) {
                        SpanText(
                            text = opcion,
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(14.px)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoSeleccionadoItem(
    elemento: Cubeta,
    onCantidadChange: (Int) -> Unit,
    onEliminar: () -> Unit,
    isMobile: Boolean
) {
    val dimensiones = "${elemento.largo.toInt()} x ${elemento.ancho.toInt()} mm"
    val maxCantidad = elemento.maxQuantity ?: Int.MAX_VALUE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 10.px)
            .border(width = 1.px, style = LineStyle.Solid, color = rgba(0, 0, 0, 0.1f))
            .borderRadius(4.px)
            .padding(10.px)
    ) {
        if (isMobile) {
            // Versión móvil (apilada)
            Column {
                // Nombre y botón eliminar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .fontWeight(FontWeight.Bold),
                        text = elemento.tipo
                    )

                    Box(
                        modifier = Modifier
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
                            .padding(8.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onEliminar() },
                        contentAlignment = Alignment.Center
                    ) {
                        FaTrash(
                            modifier = Modifier.color(Colors.White)
                        )
                    }
                }

                // Dimensiones
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(12.px)
                        .color(rgba(0, 0, 0, 0.7f))
                        .margin(topBottom = 8.px),
                    text = "Dimensiones: $dimensiones"
                )

                // Selector de cantidad
                QuantitySelector(
                    value = elemento.numero,
                    min = 1,
                    max = maxCantidad,
                    onValueChange = { onCantidadChange(it) }
                )
            }
        } else {
            // Versión escritorio (en línea)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info del elemento
                Column(
                    modifier = Modifier.width(60.percent)
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .fontWeight(FontWeight.Bold),
                        text = elemento.tipo
                    )

                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(12.px)
                            .color(rgba(0, 0, 0, 0.7f)),
                        text = "Dimensiones: $dimensiones"
                    )
                }

                // Selector de cantidad
                Box(
                    modifier = Modifier.width(30.percent),
                    contentAlignment = Alignment.Center
                ) {
                    QuantitySelector(
                        value = elemento.numero,
                        min = 1,
                        max = maxCantidad,
                        onValueChange = { onCantidadChange(it) }
                    )
                }

                // Botón eliminar
                Box(
                    modifier = Modifier
                        .width(10.percent)
                        .backgroundColor(Colors.Red)
                        .borderRadius(4.px)
                        .padding(8.px)
                        .cursor(Cursor.Pointer)
                        .onClick { onEliminar() },
                    contentAlignment = Alignment.Center
                ) {
                    FaTrash(
                        modifier = Modifier.color(Colors.White)
                    )
                }
            }
        }
    }
}

@Composable
fun MensajeError(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(org.jetbrains.compose.web.css.Position.Fixed)
            .top(0.px)
            .left(0.px)
            .backgroundColor(rgba(0, 0, 0, 0.5f))
            .zIndex(100)
            .onClick { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(350.px)
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .padding(20.px)
                .boxShadow(offsetY = 4.px, blurRadius = 8.px, color = rgba(0, 0, 0, 0.2f))
                .onClick { it.stopPropagation() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(18.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Colors.Red)
                        .margin(bottom = 15.px),
                    text = "Error"
                )

                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .textAlign(TextAlign.Center)
                        .margin(bottom = 20.px),
                    text = mensaje
                )

                Box(
                    modifier = Modifier
                        .backgroundColor(Theme.Primary.rgb)
                        .borderRadius(4.px)
                        .padding(topBottom = 8.px, leftRight = 16.px)
                        .cursor(Cursor.Pointer)
                        .onClick { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .color(Colors.White),
                        text = "Aceptar"
                    )
                }
            }
        }
    }
}