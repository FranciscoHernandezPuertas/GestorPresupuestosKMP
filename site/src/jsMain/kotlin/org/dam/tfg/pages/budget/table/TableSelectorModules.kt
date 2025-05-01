package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaCheck
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleInfo
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import com.varabyte.kobweb.silk.theme.name
import org.dam.tfg.components.*
import org.dam.tfg.models.table.ElementosConstantesLimites
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.styles.ModuloInputStyle
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.NumberInput
import org.w3c.dom.HTMLInputElement

@Page
@Composable
fun TableSelectorModulesPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorModulesContent()

        }
    }
}

@Composable
fun TableSelectorModulesContent() {
    val breakpoint = rememberBreakpoint()
    val resourceProvider = remember { WebResourceProvider() }
    val modulosConstantes = ElementosConstantesLimites.LIMITES_MODULOS

    // Estado de los módulos seleccionados
    var modulosSeleccionados by remember { mutableStateOf<List<Modulo>>(listOf()) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var moduloAEliminar by remember { mutableStateOf<Modulo?>(null) }
    var mostrarConfirmacionLimpiar by remember { mutableStateOf(false) }
    var mostrarMensajeExito by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf("") }
    var mostrarMensajeAdvertencia by remember { mutableStateOf(false) }
    var mensajeAdvertencia by remember { mutableStateOf("") }

    // Configuración según tamaño de pantalla
    val numColumnas = if (breakpoint >= Breakpoint.MD) 3 else 1
    val contentWidth = if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent
    val titleFontSize = if (breakpoint >= Breakpoint.MD) 24.px else 20.px
    val isMobile = breakpoint < Breakpoint.MD

    // Función para guardar en localStorage
    fun guardarModulosSeleccionados() {
        // Asegurar que cada módulo tenga un precio (aunque sea 0.0 inicialmente)
        val modulosConPrecio = modulosSeleccionados.map { modulo ->
            modulo.copy(precio = modulo.precio)
        }
        BudgetManager.saveModulos(modulosConPrecio)
    }

    // Timer para cerrar automáticamente el mensaje de éxito
    LaunchedEffect(mostrarMensajeExito) {
        if (mostrarMensajeExito) {
            kotlinx.browser.window.setTimeout({
                mostrarMensajeExito = false
            }, 3000) // 3 segundos
        }
    }

    // Cargar módulos guardados previamente
    LaunchedEffect(Unit) {
        val modulosGuardados = BudgetManager.getModulos()
        modulosSeleccionados = modulosGuardados
    }

    // Función para añadir un nuevo módulo
    fun anadirModulo(nombre: String, largo: Double, fondo: Double, alto: Double) {
        // Comprobar si ya existe un módulo idéntico
        val moduloExistente = modulosSeleccionados.find {
            it.nombre == nombre &&
                    it.largo == largo &&
                    it.fondo == fondo &&
                    it.alto == alto
        }

        if (moduloExistente != null) {
            // Mostrar mensaje de advertencia
            mensajeAdvertencia = "Ya existe un módulo con estas características"
            mostrarMensajeAdvertencia = true
            return
        }

        val limite = ElementosConstantesLimites.getItemWithLimitsForModulo(nombre)
        if (limite != null) {
            val nuevoModulo = Modulo(
                nombre = nombre,
                largo = largo,
                fondo = fondo,
                alto = alto,
                cantidad = 1,
                precio = 0.0,
                limite = limite
            )

            modulosSeleccionados = modulosSeleccionados + nuevoModulo
            guardarModulosSeleccionados()

            // Mostrar mensaje de éxito
            mensajeExito = "Módulo añadido correctamente"
            mostrarMensajeExito = true
        }
    }
    if (mostrarMensajeAdvertencia) {
        WarningMessage(
            mensaje = mensajeAdvertencia,
            onClose = { mostrarMensajeAdvertencia = false }
        )
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
                .maxWidth(contentWidth)
                .padding(bottom = 100.px),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título y descripción (sin cambios)
            SpanText(
                modifier = Modifier
                    .margin(top = 10.px, bottom = 20.px)
                    .fontSize(titleFontSize)
                    .fontWeight(FontWeight.Bold)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.Secondary.rgb),
                text = "Configuración de Mesa: Módulos"
            )

            // Descripción / información (sin cambios)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.px)
                    .margin(bottom = 20.px)
                    .backgroundColor(rgba(0, 0, 0, 0.05))
                    .borderRadius(8.px)
                    .padding(16.px)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaCircleInfo(
                        modifier = Modifier
                            .margin(right = 10.px)
                            .color(Theme.Primary.rgb)
                    )
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .color(Theme.Secondary.rgb),
                        text = "Seleccione los módulos que desea incluir en la mesa y especifique sus dimensiones."
                    )
                }
            }

            // Grid de módulos disponibles (modificado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                    .padding(16.px)
                    .margin(bottom = 20.px)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpanText(
                        modifier = Modifier
                            .margin(bottom = 16.px)
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Bold)
                            .fontFamily(FONT_FAMILY)
                            .color(Theme.Secondary.rgb),
                        text = "Módulos disponibles"
                    )

                    ModulosGrid(
                        modulos = modulosConstantes.values.toList(),
                        onAddModule = { nombre, largo, fondo, alto ->
                            anadirModulo(nombre, largo, fondo, alto)
                        },
                        numColumnas = numColumnas,
                        resourceProvider = resourceProvider,
                        isMobile = isMobile,
                        modulosSeleccionados = modulosSeleccionados
                    )
                }
            }

            // Lista de módulos seleccionados (sin cambios significativos)
            if (modulosSeleccionados.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .backgroundColor(Colors.White)
                        .borderRadius(8.px)
                        .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                        .padding(16.px)
                        .margin(bottom = 20.px)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cabecera con título y botón limpiar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpanText(
                                modifier = Modifier
                                    .fontSize(18.px)
                                    .fontWeight(FontWeight.Bold)
                                    .fontFamily(FONT_FAMILY)
                                    .color(Theme.Secondary.rgb),
                                text = "Módulos seleccionados"
                            )

                            // Botón limpiar all
                            Box(
                                modifier = Modifier
                                    .backgroundColor(Colors.Red)
                                    .borderRadius(4.px)
                                    .padding(6.px)
                                    .cursor(Cursor.Pointer)
                                    .onClick { mostrarConfirmacionLimpiar = true },
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
                                            .margin(left = 5.px)
                                            .color(Colors.White)
                                            .fontFamily(FONT_FAMILY),
                                        text = "Limpiar todo"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.px))

                        // Lista de módulos seleccionados
                        modulosSeleccionados.forEach { modulo ->
                            ModuloSeleccionadoItem(
                                modulo = modulo,
                                onCantidadChange = { nuevaCantidad ->
                                    // Actualizar cantidad del módulo
                                    val modulosActualizados = modulosSeleccionados.map {
                                        if (it == modulo) it.copy(cantidad = nuevaCantidad) else it
                                    }
                                    modulosSeleccionados = modulosActualizados
                                    guardarModulosSeleccionados()
                                },
                                onEliminar = {
                                    moduloAEliminar = modulo
                                    mostrarConfirmacionEliminar = true
                                },
                                breakpoint = breakpoint,
                                resourceProvider = resourceProvider
                            )
                        }
                    }
                }
            }
        }

        // Diálogos de confirmación (sin cambios)
        if (mostrarConfirmacionEliminar && moduloAEliminar != null) {
            ConfirmationDialog(
                mensaje = "¿Está seguro de que desea eliminar este módulo?",
                onConfirm = {
                    modulosSeleccionados = modulosSeleccionados.filter { it != moduloAEliminar }
                    guardarModulosSeleccionados()
                    moduloAEliminar = null
                    mostrarConfirmacionEliminar = false
                },
                onCancel = {
                    moduloAEliminar = null
                    mostrarConfirmacionEliminar = false
                }
            )
        }

        if (mostrarConfirmacionLimpiar) {
            ConfirmationDialog(
                mensaje = "¿Está seguro de que desea eliminar todos los módulos?",
                onConfirm = {
                    modulosSeleccionados = emptyList()
                    guardarModulosSeleccionados()
                    mostrarConfirmacionLimpiar = false
                },
                onCancel = {
                    mostrarConfirmacionLimpiar = false
                }
            )
        }

        // Mensaje de éxito
        if (mostrarMensajeExito) {
            SuccessMessage(
                mensaje = mensajeExito,
                onClose = { mostrarMensajeExito = false }
            )
        }

        // Footer con navegación (sin cambios)
        BudgetFooter(
            previousScreen = Screen.TableSelectorCubetas,
            nextScreen = Screen.TableSelectorResume,
            validateData = { true },
            saveData = { guardarModulosSeleccionados() }
        )
    }
}

@Composable
fun ModulosGrid(
    modulos: List<ItemWithLimits>,
    onAddModule: (moduloNombre: String, largo: Double, fondo: Double, alto: Double) -> Unit,
    numColumnas: Int,
    resourceProvider: WebResourceProvider,
    isMobile: Boolean,
    modulosSeleccionados: List<Modulo>
) {
    val modulosUnicos = modulos.distinctBy { it.name }.sortedBy { it.name }

    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until (modulosUnicos.size + numColumnas - 1) / numColumnas) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.px),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until numColumnas) {
                    val index = i * numColumnas + j
                    Box(
                        modifier = Modifier
                            .width(if (isMobile) (100f / numColumnas).percent else 240.px)
                            .padding(5.px)
                    ) {
                        if (index < modulosUnicos.size) {
                            ModuloCard(
                                modulo = modulosUnicos[index],
                                onClick = { largo, fondo, alto ->
                                    onAddModule(modulosUnicos[index].name, largo, fondo, alto)
                                },
                                resourceProvider = resourceProvider,
                                isMobile = isMobile
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuloCard(
    modulo: ItemWithLimits,
    onClick: (Double, Double, Double) -> Unit,
    resourceProvider: WebResourceProvider,
    isMobile: Boolean
) {
    val imageKey = getImageKeyFromModuleName(modulo.name)
    val cardWidth = if (isMobile) 100.percent else 220.px
    val cardHeight = if (isMobile) 380.px else 400.px // Aumenté la altura para asegurar espacio suficiente

    var largo by remember { mutableStateOf("") }
    var fondo by remember { mutableStateOf("") }
    var alto by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Colors.LightGray)
            .padding(16.px)
            .styleModifier { // Añadir para asegurar que nada interfiere con el contenedor
                property("position", "relative")
                property("overflow", "hidden")  // Evita que el contenido se salga
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .styleModifier {
                    property("display", "flex")
                    property("flex-direction", "column")
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Cambiado para distribuir el espacio correctamente
        ) {
            // Contenido superior (imagen y nombre)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen del módulo
                Box(
                    modifier = Modifier
                        .height(80.px)
                        .width(80.px)
                        .margin(bottom = 8.px),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        src = resourceProvider.getImagePath(imageKey),
                        alt = modulo.name,
                        modifier = Modifier.maxWidth(100.percent).maxHeight(100.percent)
                    )
                }

                // Nombre del módulo
                SpanText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fontFamily(FONT_FAMILY)
                        .fontSize(14.px)
                        .color(Theme.Secondary.rgb)
                        .textAlign(TextAlign.Center)
                        .margin(bottom = 12.px),
                    text = modulo.name
                )
            }

            // Campos de dimensiones (centro)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo Largo
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isMobile) 90.percent else 100.percent)
                        .margin(bottom = 8.px)
                        .padding(leftRight = 10.px)
                ) {
                    // El contenido de los campos es igual que antes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(12.px)
                                .color(Theme.Secondary.rgb)
                                .textAlign(TextAlign.Center),
                            text = "Largo (mm)"
                        )
                        NumberInput(
                            attrs = {
                                value(largo)
                                onInput { event ->
                                    largo = (event.target as HTMLInputElement).value
                                    errorMsg = ""
                                }
                                classes(ModuloInputStyle.name)
                                style {
                                    property("width", "100%")
                                    property("padding", "6px")
                                    property("font-size", "14px")
                                    property("text-align", "center")
                                }
                            }
                        )
                    }
                }

                // Campo Fondo
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isMobile) 90.percent else 100.percent)
                        .margin(bottom = 8.px)
                        .padding(leftRight = 10.px)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(12.px)
                                .color(Theme.Secondary.rgb)
                                .textAlign(TextAlign.Center),
                            text = "Fondo (mm)"
                        )
                        NumberInput {
                            value(fondo)
                            onInput { event ->
                                fondo = (event.target as HTMLInputElement).value
                                errorMsg = ""
                            }
                            classes(ModuloInputStyle.name)
                            style {
                                property("width", "100%")
                                property("padding", "6px")
                                property("font-size", "14px")
                                property("text-align", "center")
                            }
                        }
                    }
                }

                // Campo Alto
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isMobile) 90.percent else 100.percent)
                        .margin(bottom = 8.px)
                        .padding(leftRight = 10.px)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(12.px)
                                .color(Theme.Secondary.rgb)
                                .textAlign(TextAlign.Center),
                            text = "Alto (mm)"
                        )
                        NumberInput {
                            value(alto)
                            onInput { event ->
                                alto = (event.target as HTMLInputElement).value
                                errorMsg = ""
                            }
                            classes(ModuloInputStyle.name)
                            style {
                                property("width", "100%")
                                property("padding", "6px")
                                property("font-size", "14px")
                                property("text-align", "center")
                            }
                        }
                    }
                }

                // Mensaje de error si existe
                if (errorMsg.isNotEmpty()) {
                    SpanText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .color(Colors.Red)
                            .fontSize(12.px)
                            .textAlign(TextAlign.Center),
                        text = errorMsg
                    )
                }
            }

            // Botón añadir (parte inferior)
            Box(
                modifier = Modifier
                    .margin(top = 8.px, bottom = 0.px) // Asegurar margen adecuado
                    .backgroundColor(Theme.Primary.rgb)
                    .borderRadius(4.px)
                    .padding(8.px)
                    .fillMaxWidth(if (isMobile) 90.percent else 100.percent)
                    .cursor(Cursor.Pointer)
                    .onClick {
                        val largoVal = largo.toDoubleOrNull() ?: 0.0
                        val fondoVal = fondo.toDoubleOrNull() ?: 0.0
                        val altoVal = alto.toDoubleOrNull() ?: 0.0

                        when {
                            largoVal <= 0 -> errorMsg = "Largo debe ser mayor que 0"
                            fondoVal <= 0 -> errorMsg = "Fondo debe ser mayor que 0"
                            altoVal <= 0 -> errorMsg = "Alto debe ser mayor que 0"
                            else -> onClick(largoVal, fondoVal, altoVal)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FaPlus(
                        modifier = Modifier
                            .margin(right = 5.px)
                            .color(Colors.White)
                    )
                    SpanText(
                        modifier = Modifier
                            .color(Colors.White)
                            .fontFamily(FONT_FAMILY),
                        text = "Añadir"
                    )
                }
            }
        }
    }
}

@Composable
fun ModuloSeleccionadoItem(
    modulo: Modulo,
    onCantidadChange: (Int) -> Unit,
    onEliminar: () -> Unit,
    breakpoint: Breakpoint,
    resourceProvider: WebResourceProvider
) {
    val isMobile = breakpoint < Breakpoint.MD
    val imageKey = getImageKeyFromModuleName(modulo.nombre)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 10.px)
            .backgroundColor(rgba(0, 0, 0, 0.02))
            .borderRadius(4.px)
            .padding(10.px)
    ) {
        if (isMobile) {
            // Layout para móvil: layout vertical con imagen, datos y controles
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen y nombre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .width(40.px)
                            .height(40.px)
                            .objectFit(ObjectFit.Contain),
                        src = resourceProvider.getImagePath(imageKey),
                        alt = modulo.nombre
                    )
                    SpanText(
                        modifier = Modifier
                            .margin(left = 8.px)
                            .fontFamily(FONT_FAMILY)
                            .color(Theme.Secondary.rgb),
                        text = modulo.nombre
                    )
                }

                // Dimensiones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(top = 8.px),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DimensionDisplay("Largo", "${modulo.largo.toInt()} mm")
                    DimensionDisplay("Fondo", "${modulo.fondo.toInt()} mm")
                    DimensionDisplay("Alto", "${modulo.alto.toInt()} mm")
                }

                // Controles de cantidad y eliminar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(top = 8.px),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantitySelector(
                        value = modulo.cantidad,
                        onValueChange = { onCantidadChange(it) },
                        min = 1,
                        max = modulo.limite.maxQuantity
                    )

                    Box(
                        modifier = Modifier
                            .size(30.px)
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
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
        } else {
            // Layout desktop - una sola fila con all
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen y nombre
                Row(
                    modifier = Modifier.width(200.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier.width(40.px).height(40.px),
                        src = resourceProvider.getImagePath(imageKey),
                        alt = modulo.nombre
                    )
                    SpanText(
                        modifier = Modifier
                            .margin(left = 8.px)
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Theme.Secondary.rgb),
                        text = modulo.nombre
                    )
                }

                // Dimensiones
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.width(300.px)
                ) {
                    DimensionDisplay("Largo", "${modulo.largo.toInt()} mm")
                    DimensionDisplay("Fondo", "${modulo.fondo.toInt()} mm")
                    DimensionDisplay("Alto", "${modulo.alto.toInt()} mm")
                }

                Spacer()

                // Controles de cantidad y eliminar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantitySelector(
                        value = modulo.cantidad,
                        onValueChange = { onCantidadChange(it) },
                        min = 1,
                        max = modulo.limite.maxQuantity
                    )

                    Box(
                        modifier = Modifier
                            .margin(left = 10.px)
                            .size(30.px)
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
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
fun DimensionDisplay(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(12.px)
                .color(Theme.Black.rgb),
            text = label
        )
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .fontWeight(FontWeight.Bold)
                .color(Theme.Secondary.rgb),
            text = value
        )
    }
}

@Composable
fun SuccessMessage(
    mensaje: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
            .zIndex(1000)
            .styleModifier {
                property("top", "0")
                property("left", "0")
                property("right", "0")
                property("bottom", "0")
            }
            .backgroundColor(rgba(0, 0, 0, 0.3))
            .onClick { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(300.px)
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .border(1.px, color = Colors.LightGray)
                .padding(20.px)
                .onClick { it.stopPropagation() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.px)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .cursor(Cursor.Pointer)
                            .onClick { onClose() }
                    ) {
                        // Icono X (puedes usar un icono FA si está disponible)
                        SpanText(
                            text = "✕",
                            modifier = Modifier
                                .fontSize(18.px)
                                .color(Theme.Secondary.rgb)
                        )
                    }
                }

                // Símbolo de éxito (check)
                Box(
                    modifier = Modifier
                        .size(40.px)
                        .backgroundColor(rgba(0, 150, 0, 0.1))
                        .borderRadius(50.percent),
                    contentAlignment = Alignment.Center
                ) {
                    FaCheck(
                        modifier = Modifier.color(Theme.Primary.rgb)
                    )
                }

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
            }
        }
    }
}

@Composable
fun WarningMessage(
    mensaje: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(Position.Fixed)
            .zIndex(1000)
            .styleModifier {
                property("top", "0")
                property("left", "0")
                property("right", "0")
                property("bottom", "0")
            }
            .backgroundColor(rgba(0, 0, 0, 0.3))
            .onClick { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(300.px)
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .border(1.px, color = Colors.LightGray)
                .padding(20.px)
                .onClick { it.stopPropagation() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.px)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .cursor(Cursor.Pointer)
                            .onClick { onClose() }
                    ) {
                        SpanText(
                            text = "✕",
                            modifier = Modifier
                                .fontSize(18.px)
                                .color(Theme.Secondary.rgb)
                        )
                    }
                }

                // Símbolo de advertencia
                Box(
                    modifier = Modifier
                        .size(40.px)
                        .backgroundColor(rgba(255, 150, 0, 0.1))
                        .borderRadius(50.percent),
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        text = "⚠️",
                        modifier = Modifier.fontSize(20.px)
                    )
                }

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
            }
        }
    }
}

// Función auxiliar para convertir el nombre del módulo a una clave de imagen
private fun getImageKeyFromModuleName(name: String): String {
    return when (name) {
        "Bastidor sin estante" -> "BASTIDOR_SIN_ESTANTE"
        "Bastidor con estante" -> "BASTIDOR_CON_ESTANTE"
        "Bastidor con dos estantes" -> "BASTIDOR_CON_DOS_ESTANTES"
        "Bastidor con armario abierto" -> "BASTIDOR_CON_ARMARIO_ABIERTO"
        "Bastidor con armario puertas abatibles" -> "BASTIDOR_CON_ARMARIO_PUERTAS_ABATIBLES"
        "Bastidor con armario puertas correderas" -> "BASTIDOR_CON_ARMARIO_PUERTAS_CORREDERAS"
        "Bastidor con cajonera tres cajones" -> "BASTIDOR_CON_CAJONERA_TRES_CAJONES"
        "Bastidor con cajonera cuatro cajones" -> "BASTIDOR_CON_CAJONERA_CUATRO_CAJONES"
        "Bastidor para fregadero o seno" -> "BASTIDOR_PARA_FREGADERO_O_SENO"
        else -> ""
    }
}
