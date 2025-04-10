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
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaCheck
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleInfo
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.components.*
import org.dam.tfg.constants.ElementosConstantes
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.ModuloSeleccionado
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.NumberInput
import org.jetbrains.compose.web.dom.TextInput
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
    val modulosConstantes = ElementosConstantes.LIMITES_MODULOS

    // Estado de los módulos seleccionados
    var modulosSeleccionados by remember { mutableStateOf<List<ModuloSeleccionado>>(listOf()) }
    var moduloEnEdicion by remember { mutableStateOf<String?>(null) }

    // Estados para las dimensiones del módulo en edición
    var largoEdicion by remember { mutableStateOf("") }
    var fondoEdicion by remember { mutableStateOf("") }
    var altoEdicion by remember { mutableStateOf("") }
    var errorValidacion by remember { mutableStateOf("") }

    // Estados para confirmaciones y mensajes
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var moduloAEliminar by remember { mutableStateOf<ModuloSeleccionado?>(null) }
    var mostrarConfirmacionLimpiar by remember { mutableStateOf(false) }

    // Configuración según tamaño de pantalla
    val numColumnas = if (breakpoint >= Breakpoint.MD) 3 else 1
    val contentWidth = if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent
    val titleFontSize = if (breakpoint >= Breakpoint.MD) 24.px else 20.px
    val isMobile = breakpoint < Breakpoint.MD

    // Función para guardar en localStorage
    fun guardarModulosSeleccionados() {
        BudgetManager.saveModulos(modulosSeleccionados)
        // Agregar verificación de guardado
        console.log("Módulos guardados: ${modulosSeleccionados.size}")
    }

    LaunchedEffect(Unit) {
        val modulosGuardados = BudgetManager.getModulos()
        console.log("Módulos recuperados: ${modulosGuardados.size}")
        modulosSeleccionados = modulosGuardados
    }

    // Función para añadir un nuevo módulo
    fun añadirModulo() {
        if (moduloEnEdicion == null) return

        val largo = largoEdicion.toDoubleOrNull() ?: 0.0
        val fondo = fondoEdicion.toDoubleOrNull() ?: 0.0
        val alto = altoEdicion.toDoubleOrNull() ?: 0.0

        // Validaciones
        when {
            largo <= 0 -> {
                errorValidacion = "El largo debe ser mayor que 0"
                return
            }
            fondo <= 0 -> {
                errorValidacion = "El fondo debe ser mayor que 0"
                return
            }
            alto <= 0 -> {
                errorValidacion = "El alto debe ser mayor que 0"
                return
            }
        }

        // Comprobar si ya existe un módulo idéntico
        val moduloExistente = modulosSeleccionados.find {
            it.nombre == moduloEnEdicion &&
                    it.largo == largo &&
                    it.fondo == fondo &&
                    it.alto == alto
        }

        if (moduloExistente != null) {
            errorValidacion = "Ya existe un módulo con estas dimensiones"
            return
        }

        // Resto de la función igual
        val limite = modulosConstantes[moduloEnEdicion]
        if (limite != null) {
            val nuevoModulo = ModuloSeleccionado(
                nombre = moduloEnEdicion!!,
                largo = largo,
                fondo = fondo,
                alto = alto,
                cantidad = 1,
                limite = limite
            )

            modulosSeleccionados = modulosSeleccionados + nuevoModulo
            guardarModulosSeleccionados()

            // Limpiar estados
            moduloEnEdicion = null
            largoEdicion = ""
            fondoEdicion = ""
            altoEdicion = ""
            errorValidacion = ""
        }
    }

    // Cargar módulos guardados previamente
    LaunchedEffect(Unit) {
        val modulosGuardados = BudgetManager.getModulos()
        if (modulosGuardados.isNotEmpty()) {
            modulosSeleccionados = modulosGuardados
        }
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
            // Título y descripción
            SpanText(
                modifier = Modifier
                    .margin(top = 10.px, bottom = 20.px)
                    .fontSize(titleFontSize)
                    .fontWeight(FontWeight.Bold)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.Secondary.rgb),
                text = "Configuración de Mesa: Módulos"
            )

            // Descripción / información
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

            // Grid de módulos disponibles
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
                        moduloEnEdicion = moduloEnEdicion,
                        onModuloClick = { modulo ->
                            moduloEnEdicion = modulo.name
                            errorValidacion = ""
                        },
                        numColumnas = numColumnas,
                        resourceProvider = resourceProvider,
                        isMobile = isMobile
                    )
                }
            }

            // Formulario para añadir módulo con dimensiones
            if (moduloEnEdicion != null) {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SpanText(
                            modifier = Modifier
                                .margin(bottom = 16.px)
                                .fontSize(18.px)
                                .fontWeight(FontWeight.Bold)
                                .fontFamily(FONT_FAMILY)
                                .color(Theme.Secondary.rgb),
                            text = "Añadir ${moduloEnEdicion}"
                        )

                        if (errorValidacion.isNotEmpty()) {
                            ValidationError(errorValidacion)
                        }

                        // Campos para dimensiones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMobile) Arrangement.SpaceEvenly else Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Campo Largo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(10.px)
                            ) {
                                SpanText(
                                    modifier = Modifier
                                        .fontFamily(FONT_FAMILY)
                                        .fontSize(14.px)
                                        .color(Theme.Secondary.rgb),
                                    text = "Largo (mm)"
                                )
                                NumberInput {
                                    value(largoEdicion)
                                    onInput { event ->
                                        largoEdicion = (event.target as HTMLInputElement).value
                                    }
                                    style {
                                        property("height", "40px")
                                        property("padding", "8px")
                                        property("width", if (isMobile) "80px" else "120px")
                                        property("font-size", "16px")
                                    }
                                }
                            }

                            // Campo Fondo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(10.px)
                            ) {
                                SpanText(
                                    modifier = Modifier
                                        .fontFamily(FONT_FAMILY)
                                        .fontSize(14.px)
                                        .color(Theme.Secondary.rgb),
                                    text = "Fondo (mm)"
                                )
                                NumberInput {
                                    value(fondoEdicion)
                                    onInput { event ->
                                        fondoEdicion = (event.target as HTMLInputElement).value
                                    }
                                    style {
                                        property("height", "40px")
                                        property("padding", "8px")
                                        property("width", if (isMobile) "80px" else "120px")
                                        property("font-size", "16px")
                                    }
                                }
                            }

                            // Campo Alto
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(10.px)
                            ) {
                                SpanText(
                                    modifier = Modifier
                                        .fontFamily(FONT_FAMILY)
                                        .fontSize(14.px)
                                        .color(Theme.Secondary.rgb),
                                    text = "Alto (mm)"
                                )
                                NumberInput {
                                    value(altoEdicion)
                                    onInput { event ->
                                        altoEdicion = (event.target as HTMLInputElement).value
                                    }
                                    style {
                                        property("height", "40px")
                                        property("padding", "8px")
                                        property("width", if (isMobile) "80px" else "120px")
                                        property("font-size", "16px")
                                    }
                                }
                            }
                        }

                        // Botones de acción
                        Row(
                            modifier = Modifier
                                .margin(top = 20.px)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Botón añadir
                            Box(
                                modifier = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .borderRadius(4.px)
                                    .padding(10.px)
                                    .cursor(Cursor.Pointer)
                                    .onClick { añadirModulo() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FaPlus(
                                        modifier = Modifier
                                            .margin(right = 8.px)
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

                            // Botón cancelar
                            Box(
                                modifier = Modifier
                                    .margin(left = 10.px)
                                    .backgroundColor(Colors.LightGray)
                                    .borderRadius(4.px)
                                    .padding(10.px)
                                    .cursor(Cursor.Pointer)
                                    .onClick {
                                        moduloEnEdicion = null
                                        largoEdicion = ""
                                        fondoEdicion = ""
                                        altoEdicion = ""
                                        errorValidacion = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                SpanText(
                                    modifier = Modifier
                                        .color(Theme.Secondary.rgb)
                                        .fontFamily(FONT_FAMILY),
                                    text = "Cancelar"
                                )
                            }
                        }
                    }
                }
            }

            // Lista de módulos seleccionados
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

        // Diálogos de confirmación
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

        // Footer con navegación
        BudgetFooter(
            previousScreen = Screen.TableSelectorCubetas,
            nextScreen = Screen.TableSelectorResume,
            validateData = { true }, // No hay validación específica aquí
            saveData = { guardarModulosSeleccionados() }
        )
    }
}

@Composable
fun ModulosGrid(
    modulos: List<ItemWithLimits>,
    moduloEnEdicion: String?,
    onModuloClick: (ItemWithLimits) -> Unit,
    numColumnas: Int,
    resourceProvider: WebResourceProvider,
    isMobile: Boolean
) {
    val modulosUnicos = modulos.distinctBy { it.name }.sortedBy { it.name }

    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until (modulosUnicos.size + numColumnas - 1) / numColumnas) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.px),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until numColumnas) {
                    val index = i * numColumnas + j
                    Box(
                        modifier = Modifier
                            .width(if (isMobile) (100f / numColumnas).percent else 160.px)
                    ) {
                        if (index < modulosUnicos.size) {
                            ModuloCard(
                                modulo = modulosUnicos[index],
                                estaSeleccionado = moduloEnEdicion == modulosUnicos[index].name,
                                onClick = { onModuloClick(modulosUnicos[index]) },
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
    estaSeleccionado: Boolean,
    onClick: () -> Unit,
    resourceProvider: WebResourceProvider,
    isMobile: Boolean
) {
    val imageKey = getImageKeyFromModuleName(modulo.name)
    val cardWidth = if (isMobile) 100.percent else 160.px
    val cardHeight = if (isMobile) 100.px else 220.px

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .backgroundColor(if (estaSeleccionado) rgba(0, 150, 0, 0.1) else Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Colors.LightGray)
            .padding(10.px)
            .cursor(Cursor.Pointer)
            .onClick { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Imagen del módulo con tamaño controlado
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
                    .margin(bottom = 8.px),
                text = modulo.name
            )

            // Botón seleccionar
            if (estaSeleccionado) {
                Row(
                    modifier = Modifier
                        .padding(topBottom = 8.px),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaCheck(
                        modifier = Modifier.color(Theme.Primary.rgb)
                    )
                    SpanText(
                        modifier = Modifier
                            .margin(left = 5.px)
                            .color(Theme.Primary.rgb)
                            .fontFamily(FONT_FAMILY),
                        text = "Seleccionado"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .backgroundColor(Theme.Primary.rgb)
                        .borderRadius(4.px)
                        .padding(topBottom = 4.px, leftRight = 8.px)
                        .cursor(Cursor.Pointer),
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        modifier = Modifier
                            .color(Colors.White)
                            .fontFamily(FONT_FAMILY),
                        text = "Seleccionar"
                    )
                }
            }
        }
    }
}

@Composable
fun ModuloSeleccionadoItem(
    modulo: ModuloSeleccionado,
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