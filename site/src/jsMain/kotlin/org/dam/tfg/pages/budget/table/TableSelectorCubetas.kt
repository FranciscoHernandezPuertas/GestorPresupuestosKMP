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
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
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
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.styles.DropdownItemStyle
import org.dam.tfg.styles.DropdownSelectorStyle
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.DimensionExtractors
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.vh

@Page
@Composable
fun TableSelectorCubetasPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorCubetasContent()

        }
    }
}

@Composable
fun TableSelectorCubetasContent() {
    val context = rememberPageContext()
    val breakpoint = rememberBreakpoint()
    val resourceProvider = remember { WebResourceProvider() }

    // Estado para las cubetas y soportes de bandejas
    var cubetasSeleccionadas by remember { mutableStateOf<List<Cubeta>>(emptyList()) }
    // Estados para confirmaciones y mensajes
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminarTodo by remember { mutableStateOf(false) }
    var elementoAEliminar by remember { mutableStateOf<Pair<Cubeta, Boolean>?>(null) } // Cubeta, esCubeta
    var mensajeError by remember { mutableStateOf("") }
    var mostrarMensajeError by remember { mutableStateOf(false) }

    var seleccionActual: String
    fun getOpcionesDisponibles(seleccionadas: List<Cubeta>): List<ItemWithLimits> {
        val tiposSeleccionados = seleccionadas.map { it.tipo }.toSet()
        return ElementosConstantes.LIMITES_CUBETAS
            .filter { (nombre, _) -> nombre !in tiposSeleccionados }
            .map { (nombre, item) ->
                ItemWithLimits(
                    id = item.id,
                    name = nombre,
                    minQuantity = item.minQuantity,
                    maxQuantity = item.maxQuantity,
                    initialQuantity = item.initialQuantity
                )
            }
    }

    // Opciones disponibles
    var opcionesCubetasDisponibles by remember {
        mutableStateOf(getOpcionesDisponibles(emptyList()))
    }

    // Cargar datos guardados y actualizar opciones
    LaunchedEffect(Unit) {
        val cubetasGuardadas = BudgetManager.getCubetas()
        if (cubetasGuardadas.isNotEmpty()) {
            cubetasSeleccionadas = cubetasGuardadas
            opcionesCubetasDisponibles = getOpcionesDisponibles(cubetasGuardadas)
        }
    }

    // Funciones para añadir y eliminar elementos
    fun anadirCubeta(nombre: String) {
        val dimensiones = DimensionExtractors.extractCubetaDimensions(nombre)
        val largo = dimensiones.first
        val ancho = dimensiones.second
        val alto = dimensiones.third
        val maxQuantity = ElementosConstantes.LIMITES_CUBETAS[nombre]?.maxQuantity ?: 1

        val nuevaCubeta = Cubeta(
            tipo = nombre,
            numero = 1,
            largo = largo,
            fondo = ancho,
            alto = alto,
            maxQuantity = maxQuantity
        )

        val nuevaListaCubetas = cubetasSeleccionadas + nuevaCubeta
        cubetasSeleccionadas = nuevaListaCubetas
        // Actualizar opciones disponibles
        opcionesCubetasDisponibles = getOpcionesDisponibles(nuevaListaCubetas)
        // Guardar en BudgetManager
        BudgetManager.saveCubetas(nuevaListaCubetas)
        // Limpiar selección actual
        seleccionActual = ""
    }

    fun actualizarCantidadCubeta(cubeta: Cubeta, nuevaCantidad: Int) {
        // Actualiza el estado local
        cubetasSeleccionadas = cubetasSeleccionadas.map {
            if (it == cubeta) it.copy(numero = nuevaCantidad) else it
        }
        // Guarda los cambios persistentemente
        BudgetManager.saveCubetas(cubetasSeleccionadas)
    }

    fun eliminarCubeta(cubeta: Cubeta) {
        val nuevaListaCubetas = cubetasSeleccionadas.filter { it != cubeta }
        cubetasSeleccionadas = nuevaListaCubetas
        // Actualizar opciones disponibles
        opcionesCubetasDisponibles = getOpcionesDisponibles(nuevaListaCubetas)
        // Guardar cambios
        BudgetManager.saveCubetas(nuevaListaCubetas)
    }

    // Función para guardar datos
    fun guardarSelecciones(): Boolean {
        // Guardar cubetas explícitamente
        BudgetManager.saveCubetas(cubetasSeleccionadas)
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
                text = "Configuración de Mesa: Selección de Cubetas"
            )

            // Layout de selección - adaptable a móvil/escritorio
            if (isFlexColumn) {
                // Versión móvil (apilada)
                SelectorElementos(
                    titulo = "Cubetas",
                    imagen = resourceProvider.getImagePath("CUBETA"),
                    opciones = opcionesCubetasDisponibles,
                    elementosSeleccionados = cubetasSeleccionadas,
                    onAnadir = { anadirCubeta(it) },
                    onActualizarCantidad = { cubeta, cantidad -> actualizarCantidadCubeta(cubeta, cantidad) },
                    onEliminar = {
                        elementoAEliminar = Pair(it, true)
                        mostrarConfirmacionEliminar = true
                    },
                    onEliminarTodo = {
                        mostrarConfirmacionEliminarTodo = true
                    },
                    breakpoint = breakpoint,
                    resourceProvider = resourceProvider
                )

                Spacer()
            } else {
                // Versión escritorio (lado a lado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.width(100.percent)) {
                        SelectorElementos(
                            titulo = "Cubetas",
                            imagen = resourceProvider.getImagePath("CUBETA"),
                            opciones = opcionesCubetasDisponibles,
                            elementosSeleccionados = cubetasSeleccionadas,
                            onAnadir = { anadirCubeta(it) },
                            onActualizarCantidad = { cubeta, cantidad -> actualizarCantidadCubeta(cubeta, cantidad) },
                            onEliminar = {
                                elementoAEliminar = Pair(it, true)
                                mostrarConfirmacionEliminar = true
                            },
                            onEliminarTodo = {
                                mostrarConfirmacionEliminarTodo = true
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

        // Diálogo de confirmación para eliminar todas las cubetas
        if (mostrarConfirmacionEliminarTodo) {
            ConfirmationDialog(
                mensaje = "¿Está seguro que desea eliminar todas las cubetas?",
                onConfirm = {
                    cubetasSeleccionadas = emptyList()
                    opcionesCubetasDisponibles = getOpcionesDisponibles(emptyList())
                    BudgetManager.saveCubetas(emptyList())
                    mostrarConfirmacionEliminarTodo = false
                },
                onCancel = {
                    mostrarConfirmacionEliminarTodo = false
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
        nextScreen = Screen.TableSelectorModules,
        validateData = { guardarSelecciones() },
        saveData = { /* Opcional: lógica adicional al guardar */ }
    )
}

@Composable
fun SelectorElementos(
    titulo: String,
    imagen: String,
    opciones: List<ItemWithLimits>,
    elementosSeleccionados: List<Cubeta>,
    onAnadir: (String) -> Unit,
    onActualizarCantidad: (Cubeta, Int) -> Unit,
    onEliminar: (Cubeta) -> Unit,
    onEliminarTodo: () -> Unit,
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
        // Título y botón eliminar all
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Bold)
                    .color(Theme.Primary.rgb),
                text = titulo
            )
        }

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
                    .onClick {
                        if (seleccionActual.isNotEmpty())
                        onAnadir(seleccionActual)
                        seleccionActual = ""
                    },
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
                // Título y botón de eliminar en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Bold),
                        text = "Elementos seleccionados:"
                    )

                    // Botón eliminar a la derecha
                    Box(
                        modifier = Modifier
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
                            .padding(8.px)
                            .margin(bottom = 4.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onEliminarTodo() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FaTrash(
                                modifier = Modifier.color(Colors.White)
                            )
                            SpanText(
                                modifier = Modifier
                                    .margin(left = 8.px)
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(14.px)
                                    .color(Colors.White),
                                text = "Eliminar todo"
                            )
                        }
                    }
                }

                Spacer()

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
        // Selector visible sin borde adicional
        Row(
            modifier = DropdownSelectorStyle.toModifier()
                .fillMaxWidth()
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
                    .maxHeight(50.vh)
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
    val dimensiones = if (elemento.tipo.startsWith("Diametro")) {
        // Extraemos directamente los valores del tipo
        val match = Regex("Diametro (\\d+)x(\\d+)").find(elemento.tipo)
        val diametro = match?.groupValues?.get(1) ?: "0"
        val alto = match?.groupValues?.get(2) ?: "0"
        "Diámetro: $diametro mm, Alto: $alto mm"
    } else {
        // Para cubetas rectangulares y cuadradas
        "Largo: ${elemento.largo.toInt()} mm, Fondo: ${elemento.fondo.toInt()} mm" +
                (elemento.alto?.let { ", Alto: ${it.toInt()} mm" } ?: "")
    }
    val maxCantidad = elemento.maxQuantity ?: Int.MAX_VALUE
    val minCantidad = ElementosConstantes.LIMITES_CUBETAS[elemento.tipo]?.minQuantity ?: 0

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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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

                    // Botón eliminar
                    Box(
                        modifier = Modifier
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
                            .padding(8.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onEliminar() },
                        contentAlignment = Alignment.Center
                    ) {
                        FaTrash(modifier = Modifier.color(Colors.White))
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

                // Selector de cantidad (alineado a la derecha)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    QuantitySelector(
                        value = elemento.numero,
                        min = minCantidad,
                        max = maxCantidad,
                        onValueChange = { onCantidadChange(it) }
                    )
                }
            }
        } else {
            // Versión escritorio (en línea)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start, // Cambiado de SpaceBetween a Start
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info del elemento
                Column(
                    modifier = Modifier.width(350.px)
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

                // Espaciador flexible para empujar los controles a la derecha
                Spacer()

                // Controles a la derecha
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector de cantidad
                    QuantitySelector(
                        value = elemento.numero,
                        min = minCantidad,
                        max = maxCantidad,
                        onValueChange = { onCantidadChange(it) }
                    )

                    // Botón eliminar
                    Box(
                        modifier = Modifier
                            .margin(left = 10.px)
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
}

@Composable
fun MensajeError(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
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