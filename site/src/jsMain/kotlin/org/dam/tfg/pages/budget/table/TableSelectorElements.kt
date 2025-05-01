package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
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
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaCheck
import com.varabyte.kobweb.silk.components.icons.fa.FaCircleInfo
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.ConfirmationDialog
import org.dam.tfg.components.QuantitySelector
import org.dam.tfg.models.table.ElementosConstantesLimites
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba

@Page
@Composable
fun TableSelectorElementsPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            // Contenido principal separado del header
            TableSelectorElementsContent()
        }
    }
}


@Composable
fun TableSelectorElementsContent() {
    val breakpoint = rememberBreakpoint()
    val resourceProvider = remember { WebResourceProvider() }
    val elementosConstantes = ElementosConstantesLimites.LIMITES_ELEMENTOS_GENERALES
    var elementosSeleccionados by remember { mutableStateOf<List<ElementoSeleccionado>>(listOf()) }

    // Estados para confirmaciones y advertencias
    var mostrarAdvertencia by remember { mutableStateOf(false) }
    var mensajeAdvertencia by remember { mutableStateOf("") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var elementoAEliminar by remember { mutableStateOf<ElementoSeleccionado?>(null) }
    var mostrarConfirmacionLimpiar by remember { mutableStateOf(false) }

    // Configuración según tamaño de pantalla
    val numColumnas = if (breakpoint >= Breakpoint.MD) 4 else 2
    val contentWidth = if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent
    val titleFontSize = if (breakpoint >= Breakpoint.MD) 24.px else 20.px

    // Función para guardar en localStorage
    // Modificación 1: Cambiar la estructura de datos para elementos
    fun guardarEnLocalStorage() {
        val elementosConPrecio = elementosSeleccionados.associate { elemento ->
            elemento.nombre to mapOf(
                "cantidad" to elemento.cantidad,
                "precio" to elemento.precio.toInt() // Asumiendo que precio es Double
            )
        }
        BudgetManager.saveElementosData(elementosConPrecio)
    }

// Al cargar los datos guardados (en el LaunchedEffect)
    LaunchedEffect(Unit) {
        val elementosGuardados = BudgetManager.getElementosData()
        val elementosCargados = elementosGuardados.mapNotNull { (nombre, datos) ->
            val limite = ElementosConstantesLimites.getItemWithLimitsForElementoGeneral(nombre)
            limite?.let {
                ElementoSeleccionado(
                    nombre = nombre,
                    cantidad = datos["cantidad"] ?: 1, // Valor por defecto si no existe
                    precio = (datos["precio"] ?: 0).toDouble(), // Valor por defecto si no existe
                    limite = limite
                )
            }
        }
        elementosSeleccionados = elementosCargados
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
            // Título y descripción (se mantienen igual)
            SpanText(
                modifier = Modifier
                    .margin(top = 10.px, bottom = 20.px)
                    .fontSize(titleFontSize)
                    .fontWeight(FontWeight.Bold)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.Secondary.rgb),
                text = "Configuración de Mesa: Elementos"
            )

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
                        text = "Seleccione los elementos adicionales para su mesa. Cada elemento tiene una cantidad máxima permitida."
                    )
                }
            }

            // Grid de elementos disponibles (se mantiene igual)
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
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Medium)
                            .fontFamily(FONT_FAMILY)
                            .color(Theme.Secondary.rgb)
                            .margin(bottom = 16.px),
                        text = "Elementos disponibles"
                    )

                    ElementosGrid(
                        elementos = ElementosConstantesLimites.LIMITES_ELEMENTOS_GENERALES.values.toList(),
                        elementosSeleccionados = elementosSeleccionados.map { it.nombre },
                        numColumnas = numColumnas,
                        onElementoClick = { elemento ->
                            // Verificar si ya está seleccionado
                            if (elementosSeleccionados.any { it.nombre == elemento.name }) {
                                mostrarAdvertencia = true
                                mensajeAdvertencia = "Este elemento ya ha sido añadido"
                            } else {
                                // Añadir el elemento
                                elementosSeleccionados = elementosSeleccionados + ElementoSeleccionado(
                                    nombre = elemento.name,
                                    cantidad = 1,
                                    limite = elemento
                                )
                                // Guardar en localStorage después de añadir
                                guardarEnLocalStorage()
                            }
                        },
                        resourceProvider = resourceProvider
                    )
                }
            }

            // Lista de elementos seleccionados con el botón "Eliminar"
            if (elementosSeleccionados.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .backgroundColor(Colors.White)
                        .borderRadius(8.px)
                        .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                        .padding(16.px)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cabecera con título y botón eliminar
                        Row(
                            modifier = Modifier.fillMaxWidth().margin(bottom = 16.px),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpanText(
                                modifier = Modifier
                                    .fontSize(18.px)
                                    .fontWeight(FontWeight.Medium)
                                    .fontFamily(FONT_FAMILY)
                                    .color(Theme.Secondary.rgb),
                                text = "Elementos seleccionados"
                            )

                            // Botón de eliminar all
                            Box(
                                modifier = Modifier
                                    .backgroundColor(Colors.Red)
                                    .borderRadius(4.px)
                                    .padding(8.px)
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
                                            .margin(left = 8.px)
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Colors.White),
                                        text = "Eliminar todo"
                                    )
                                }
                            }
                        }

                        // Lista de elementos seleccionados
                        elementosSeleccionados.forEach { elemento ->
                            ElementoSeleccionadoItem(
                                elemento = elemento,
                                onCantidadChange = { nuevaCantidad ->
                                    elementosSeleccionados = elementosSeleccionados.map {
                                        if (it.nombre == elemento.nombre) it.copy(cantidad = nuevaCantidad)
                                        else it
                                    }
                                    // Guardar en localStorage después de cambiar cantidad
                                    guardarEnLocalStorage()
                                },
                                onEliminar = {
                                    elementoAEliminar = elemento
                                    mostrarConfirmacion = true
                                },
                                breakpoint = breakpoint,
                                resourceProvider = resourceProvider
                            )
                        }
                    }
                }
            }
        }

        // Popups y diálogos
        if (mostrarAdvertencia) {
            /* AdvertenciaOverlay(
                mensaje = mensajeAdvertencia,
                onDismiss = { mostrarAdvertencia = false }
            ) */
        }

        // Diálogo de confirmación para eliminar un elemento
        if (mostrarConfirmacion && elementoAEliminar != null) {
            ConfirmationDialog(
                mensaje = "¿Está seguro de que desea eliminar el elemento '${elementoAEliminar?.nombre}'?",
                onConfirm = {
                    elementosSeleccionados = elementosSeleccionados.filter { it.nombre != elementoAEliminar?.nombre }
                    elementoAEliminar = null
                    mostrarConfirmacion = false
                    // Guardar en localStorage después de eliminar
                    guardarEnLocalStorage()
                },
                onCancel = {
                    elementoAEliminar = null
                    mostrarConfirmacion = false
                }
            )
        }

        // Diálogo de confirmación para eliminar todos los elementos
        if (mostrarConfirmacionLimpiar) {
            ConfirmationDialog(
                mensaje = "¿Está seguro de que desea eliminar todos los elementos?",
                onConfirm = {
                    elementosSeleccionados = emptyList()
                    mostrarConfirmacionLimpiar = false
                    // Guardar en localStorage después de eliminar todos
                    guardarEnLocalStorage()
                },
                onCancel = {
                    mostrarConfirmacionLimpiar = false
                }
            )
        }

        // Footer (mantenemos la función saveData pero ya guardamos en cada cambio)
        BudgetFooter(
            previousScreen = Screen.TableSelectorDimensions,
            nextScreen = Screen.TableSelectorCubetas,
            validateData = {
                true // No hay validación específica aquí
            },
            saveData = {
                // En realidad ya guardamos en cada cambio, pero mantenemos esto por consistencia
                guardarEnLocalStorage()
            }
        )
    }
}

@Composable
fun ElementosGrid(
    elementos: List<ItemWithLimits>,
    elementosSeleccionados: List<String>,
    numColumnas: Int,
    onElementoClick: (ItemWithLimits) -> Unit,
    resourceProvider: WebResourceProvider
) {
    // Eliminar posibles duplicados y ordenar
    val elementosUnicos = elementos.distinctBy { it.name }.sortedBy { it.name }
    val filas = (elementosUnicos.size + numColumnas - 1) / numColumnas

    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until filas) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.px),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until numColumnas) {
                    val index = i * numColumnas + j
                    if (index < elementosUnicos.size) {
                        Box(
                            modifier = Modifier
                                .width(170.px)
                                .padding(leftRight = 5.px),
                            contentAlignment = Alignment.Center
                        ) {
                            val elemento = elementosUnicos[index]
                            ElementoCard(
                                elemento = elemento,
                                estaSeleccionado = elemento.name in elementosSeleccionados,
                                onClick = { onElementoClick(elemento) },
                                resourceProvider = resourceProvider
                            )
                        }
                    } else {
                        // Espacio vacío para mantener la distribución uniforme
                        Box(
                            modifier = Modifier.width(170.px).padding(leftRight = 5.px)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoCard(
    elemento: ItemWithLimits,
    estaSeleccionado: Boolean,
    onClick: () -> Unit,
    resourceProvider: WebResourceProvider
) {
    val imageKey = getImageKeyFromElementName(elemento.name)

    Box(
        modifier = Modifier
            .width(160.px)
            .height(240.px)
            .backgroundColor(if (estaSeleccionado) rgba(0, 150, 0, 0.1) else Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Colors.LightGray)
            .padding(10.px)
            .cursor(if (!estaSeleccionado) Cursor.Pointer else Cursor.Default)
            .onClick { if (!estaSeleccionado) onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen en la parte superior (con tamaño reducido)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.px)
                    .padding(bottom = 5.px),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .maxWidth(100.percent)
                        .height(100.percent),
                    src = resourceProvider.getImagePath(imageKey),
                    alt = elemento.name
                )
            }

            // Espacio para separar secciones
            Spacer()

            // Sección central con textos (con altura explícita)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.px), // Aumentado para dar más espacio al texto
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Nombre del elemento
                    SpanText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .color(Theme.Secondary.rgb)
                            .textAlign(TextAlign.Center)
                            .margin(bottom = 6.px),
                        text = elemento.name
                    )

                    // Límite máximo
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(12.px)
                            .color(Theme.Black.rgb),
                        text = "Máx: ${elemento.maxQuantity}"
                    )
                }
            }

            // Espacio para separar secciones
            Spacer()

            // Botón en la parte inferior (altura fija)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.px)
            ) {
                // Botón de añadir o indicador de seleccionado
                if (estaSeleccionado) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FaCheck(
                            modifier = Modifier.color(Colors.White)
                        )
                        SpanText(
                            modifier = Modifier
                                .margin(left = 8.px)
                                .fontFamily(FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Colors.White),
                            text = "Añadido"
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onClick() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FaPlus(
                            modifier = Modifier.color(Colors.White)
                        )
                        SpanText(
                            modifier = Modifier
                                .margin(left = 8.px)
                                .fontFamily(FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Colors.White),
                            text = "Añadir"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElementoSeleccionadoItem(
    elemento: ElementoSeleccionado,
    onCantidadChange: (Int) -> Unit,
    onEliminar: () -> Unit,
    breakpoint: Breakpoint,
    resourceProvider: WebResourceProvider
) {
    val isMobile = breakpoint < Breakpoint.MD
    val imageKey = getImageKeyFromElementName(elemento.nombre)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 10.px)
            .backgroundColor(rgba(0, 0, 0, 0.02))
            .borderRadius(4.px)
            .padding(10.px)
    ) {
        if (isMobile) {
            // Layout para móvil: imagen, texto y controles en una fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen a la izquierda
                Image(
                    modifier = Modifier.width(40.px).height(40.px),
                    src = resourceProvider.getImagePath(imageKey),
                    alt = elemento.nombre
                )

                // Texto del elemento al lado de la imagen
                SpanText(
                    modifier = Modifier
                        .margin(left = 10.px)
                        .weight(1f) // Para que tome el espacio disponible
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .color(Theme.Secondary.rgb),
                    text = elemento.nombre
                )

                // Selector de cantidad y botón eliminar a la derecha
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector de cantidad
                    QuantitySelector(
                        value = elemento.cantidad,
                        onValueChange = onCantidadChange,
                        min = 1,
                        max = elemento.limite.maxQuantity
                    )

                    // Botón de eliminar
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
        } else {
            // El layout para desktop se mantiene igual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(250.px)
                ) {
                    Image(
                        modifier = Modifier.width(40.px).height(40.px),
                        src = resourceProvider.getImagePath(imageKey),
                        alt = elemento.nombre
                    )

                    SpanText(
                        modifier = Modifier
                            .margin(left = 10.px)
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Theme.Secondary.rgb),
                        text = elemento.nombre
                    )
                }

                Spacer()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantitySelector(
                        value = elemento.cantidad,
                        onValueChange = onCantidadChange,
                        min = 1,
                        max = elemento.limite.maxQuantity
                    )

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

// Función auxiliar para convertir el nombre del elemento a una clave de imagen
private fun getImageKeyFromElementName(name: String): String {
    return when (name) {
        "Peto lateral" -> "PETO_LATERAL"
        "Kit lavamanos pulsador" -> "KIT_LAVAMANOS_PULSADOR"
        "Esquina en chaflán" -> "ESQUINA_EN_CHAFLAN"
        "Kit lavam. pedal simple" -> "KIT_LAVAMANOS_PEDAL_SIMPLE"
        "Esquina redondeada" -> "ESQUINA_REDONDEADA"
        "Kit lavam. pedal doble" -> "KIT_LAVAMANOS_PEDAL_DOBLE"
        "Cajeado columna" -> "CAJEADO_COLUMNA"
        "Baquetón en seno" -> "BAQUETON_EN_SENO"
        "Aro de desbarace" -> "ARO_DE_DESBARACE"
        "Baqueton perimetrico" -> "BAQUETON_PERIMETRICO"
        else -> ""
    }
}