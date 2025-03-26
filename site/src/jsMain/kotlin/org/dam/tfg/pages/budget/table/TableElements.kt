package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.ConfirmDialog
import org.dam.tfg.models.Theme
import org.dam.tfg.models.budget.*
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun TableElementsPage() {
    TableElementsScreen()
}

@Composable
fun TableElementsScreen() {
    // Estado para almacenar los elementos seleccionados
    var elementosGenerales by remember { mutableStateOf(listOf<ElementosGenerales>()) }
    var cubetas by remember { mutableStateOf(listOf<Cubeta>()) }

    // Estado para el elemento a eliminar
    var showDeleteDialog by remember { mutableStateOf(false) }
    var elementoAEliminar by remember { mutableStateOf<Pair<String, Int>?>(null) }

    // Elementos predefinidos
    val elementosGeneralesPredefinidos = remember {
        listOf(
            "Peto lateral",
            "Kit lavamanos pulsador",
            "Esquina en chaflán",
            "Kit lavam. pedal simple",
            "Esquina redondeada",
            "Kit lavam. pedal doble",
            "Cajeado columna",
            "Baquetón en seno",
            "Aro de desbarace",
            "Baqueton perimetrico"
        )
    }

    val cubetasPredefinidas = remember {
        listOf(
            "Diametro 300x180",
            "Diametro 360x180",
            "Diametro 380x180",
            "Diametro 420x180",
            "Diametro 460x180",
            "Cuadrada 400x400x250",
            "Cuadrada 400x400×300",
            "Cuadrada 450x450x250",
            "Cuadrada 450x450x300",
            "Cuadrada 500×500×250",
            "Cuadrada 500x500×300",
            "Rectangular 325x300x150",
            "Rectangular 500x300x300",
            "Rectangular 500x400x250",
            "Rectangular 600x450x300",
            "Rectangular 600x500×250",
            "Rectangular 600x500x300",
            "Rectangular 600x500x320",
            "Rectangular 630x510x380",
            "Rectangular 700x450x350",
            "Rectangular 800x500x380",
            "Rectangular 955x510x380",
            "Rectangular 1280x510x380"
        )
    }

    // Función para guardar datos
    fun saveData() {
        val extras = mutableListOf<Extra>().apply {
            addAll(elementosGenerales)
            addAll(cubetas)
        }

        try {
            val extrasJson = Json.encodeToString(extras)
            localStorage.setItem("table_elements", extrasJson)
        } catch (e: Exception) {
            console.log("Error al guardar los elementos: ${e.message}")
        }
    }

    // Función para cargar datos
    LaunchedEffect(Unit) {
        try {
            val savedElementsJson = localStorage.getItem("table_elements")
            if (savedElementsJson != null) {
                val savedElements = Json.decodeFromString<List<Extra>>(savedElementsJson)

                // Filtrar y asignar elementos por tipo
                elementosGenerales = savedElements.filterIsInstance<ElementosGenerales>()
                cubetas = savedElements.filterIsInstance<Cubeta>()
            }
        } catch (e: Exception) {
            console.log("Error al cargar los elementos: ${e.message}")
        }
    }

    // Función para validar datos
    fun validateData(): Boolean {
        return true // Implementa validaciones específicas si es necesario
    }

    // Función para eliminar un elemento
    fun eliminarElemento(tipo: String, indice: Int) {
        when (tipo) {
            "ElementosGenerales" -> elementosGenerales = elementosGenerales.filterIndexed { i, _ -> i != indice }
            "Cubeta" -> cubetas = cubetas.filterIndexed { i, _ -> i != indice }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(title = "Elementos de la mesa")

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(90.percent)
                    .padding(top = 30.px, bottom = 100.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título de la página
                SpanText(
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(24.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb)
                        .margin(bottom = 30.px),
                    text = "Seleccione los elementos adicionales"
                )

                // Sección de Elementos Generales
                ElementosGeneralesSection(
                    elementosGenerales,
                    elementosGeneralesPredefinidos,
                    onElementoAdded = { elemento ->
                        elementosGenerales = elementosGenerales + elemento
                    },
                    onCantidadChanged = { index, cantidad ->
                        elementosGenerales = elementosGenerales.mapIndexed { i, elem ->
                            if (i == index) elem.copy(numero = cantidad) else elem
                        }
                    },
                    onDeleteClick = { index ->
                        elementoAEliminar = Pair("ElementosGenerales", index)
                        showDeleteDialog = true
                    }
                )

                // Sección de Cubetas
                CubetasSection(
                    cubetas,
                    cubetasPredefinidas,
                    onCubetaAdded = { cubeta ->
                        cubetas = cubetas + cubeta
                    },
                    onCantidadChanged = { index, cantidad ->
                        cubetas = cubetas.mapIndexed { i, elem ->
                            if (i == index) elem.copy(numero = cantidad) else elem
                        }
                    },
                    onDeleteClick = { index ->
                        elementoAEliminar = Pair("Cubeta", index)
                        showDeleteDialog = true
                    }
                )

                // Aquí agregarías las demás secciones de forma similar
            }
        }

        // Footer de navegación
        BudgetFooter(
            previousScreen = Screen.TableSelector,
            nextScreen = Screen.Home, // Cambiar a la siguiente pantalla cuando exista
            validateData = { validateData() },
            saveData = { saveData() }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && elementoAEliminar != null) {
        ConfirmDialog(
            message = "¿Está seguro que desea eliminar este elemento?",
            onConfirm = {
                elementoAEliminar?.let { (tipo, indice) ->
                    eliminarElemento(tipo, indice)
                }
                showDeleteDialog = false
                elementoAEliminar = null
            },
            onDismiss = {
                showDeleteDialog = false
                elementoAEliminar = null
            }
        )
    }
}

@Composable
fun ElementosGeneralesSection(
    elementosGenerales: List<ElementosGenerales>,
    elementosPredefinidos: List<String>,
    onElementoAdded: (ElementosGenerales) -> Unit,
    onCantidadChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    val breakpoint = rememberBreakpoint()
    var elementoSeleccionado by remember { mutableStateOf("") }

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
            text = "Elementos Generales"
        )

        // Grid de elementos
        Div(
            attrs = Modifier
                .fillMaxWidth()
                .margin(bottom = 25.px)
                .styleModifier {
                    property("display", "grid")
                    property("grid-template-columns", if (breakpoint <= Breakpoint.MD) "1fr 1fr" else "1fr 1fr 1fr 1fr 1fr")
                    property("gap", "20px")
                }
                .toAttrs()
        ) {
            elementosPredefinidos.forEachIndexed { index, elemento ->
                Div(
                    attrs = Modifier
                        .padding(10.px)
                        .backgroundColor(Theme.LightGray.rgb)
                        .borderRadius(8.px)
                        .border(1.px, LineStyle.Solid, Theme.Secondary.rgb)
                        .styleModifier {
                            property("display", "flex")
                            property("flex-direction", "column")
                            property("align-items", "center")
                            property("justify-content", "space-between")
                        }
                        .height(180.px)
                        .toAttrs()
                ) {
                    // Imagen del elemento
                    Image(
                        modifier = Modifier
                            .size(80.px)
                            .margin(bottom = 10.px),
                        src = when(index) {
                            0 -> Res.Image.petoLateral
                            1 -> Res.Image.kitLavamanosPulsador
                            2 -> Res.Image.esquinaEnChaflan
                            3 -> Res.Image.kitLavamanosPedalSimple
                            4 -> Res.Image.esquinaRedondeada
                            5 -> Res.Image.kitLavamanosPedalDoble
                            6 -> Res.Image.cajeadoColumna
                            7 -> Res.Image.baquetonEnSeno
                            8 -> Res.Image.aroDeDesbrace
                            9 -> Res.Image.baquetonPerimetrico
                            else -> Res.Image.noSeleccionado
                        },
                        alt = elemento
                    )

                    // Nombre del elemento
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .fontWeight(FontWeight.Medium)
                            .color(Theme.Secondary.rgb)
                            .margin(bottom = 10.px)
                            .textAlign(TextAlign.Center),
                        text = elemento
                    )

                    // Botón para añadir
                    Button(
                        attrs = Modifier
                            .backgroundColor(Theme.Primary.rgb)
                            .color(Colors.White)
                            .borderRadius(4.px)
                            .padding(8.px)
                            .border(0.px, LineStyle.None, Colors.Transparent)
                            .cursor(Cursor.Pointer)
                            .onClick {
                                onElementoAdded(ElementosGenerales(tipo = "ElementosGenerales", numero = 1))
                            }
                            .toAttrs()
                    ) {
                        Text("Añadir")
                    }
                }
            }
        }

        // Elementos añadidos
        if (elementosGenerales.isNotEmpty()) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Theme.Secondary.rgb)
                    .margin(bottom = 10.px, top = 20.px),
                text = "Elementos añadidos"
            )

            elementosGenerales.forEachIndexed { index, elemento ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 10.px)
                        .padding(10.px)
                        .backgroundColor(Theme.LightGray.rgb)
                        .borderRadius(4.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre del elemento
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Theme.Secondary.rgb),
                        text = elementosPredefinidos[index % elementosPredefinidos.size]
                    )

                    Spacer()

                    // Selector de cantidad
                    QuantitySelector(
                        value = elemento.numero,
                        onValueChange = { cantidad ->
                            onCantidadChanged(index, cantidad)
                        },
                        min = 1,
                        max = 10
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
        }
    }
}

@Composable
fun CubetasSection(
    cubetas: List<Cubeta>,
    cubetasPredefinidas: List<String>,
    onCubetaAdded: (Cubeta) -> Unit,
    onCantidadChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit

) {
    val breakpoint = rememberBreakpoint()
    var cubetaSeleccionada by remember { mutableStateOf("") }
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
            text = "Cubetas"
        )

        // Selector para añadir cubeta
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
                            cubetaSeleccionada = event.target.value
                        }
                    }
            ) {
                Option(
                    value = "",
                    attrs = Modifier.toAttrs {
                        if (cubetaSeleccionada.isEmpty()) {
                            selected()
                        }
                    }
                ) {
                    Text("Seleccionar cubeta...")
                }

                cubetasPredefinidas.forEach { cubeta ->
                    Option(
                        value = cubeta,
                        attrs = Modifier.toAttrs {
                            if (cubetaSeleccionada == cubeta) {
                                selected()
                            }
                        }
                    ) {
                        Text(cubeta)
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
                        if (cubetaSeleccionada.isNotEmpty()) {
                            // Extraer dimensiones aproximadas del texto
                            val dims = cubetaSeleccionada.split(" ")[1].split("x")
                            val largo = dims.getOrNull(0)?.toDoubleOrNull() ?: 500.0
                            val ancho = dims.getOrNull(1)?.toDoubleOrNull() ?: 400.0

                            onCubetaAdded(Cubeta(
                                tipo = "Cubeta",
                                numero = 1,
                                largo = largo,
                                ancho = ancho
                            ))
                            cubetaSeleccionada = ""
                        }
                    }
                    .toAttrs()
            ) {
                Text("Añadir")
            }
        }

        // Cubetas añadidas
        if (cubetas.isNotEmpty()) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(18.px)
                    .fontWeight(FontWeight.Medium)
                    .color(Theme.Secondary.rgb)
                    .margin(bottom = 10.px, top = 20.px),
                text = "Cubetas añadidas"
            )

            cubetas.forEachIndexed { index, cubeta ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 10.px)
                        .padding(10.px)
                        .backgroundColor(Theme.LightGray.rgb)
                        .borderRadius(4.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de la cubeta
                    Image(
                        modifier = Modifier
                            .size(60.px)
                            .margin(right = 15.px),
                        src = Res.Image.cubeta,
                        alt = "Cubeta"
                    )

                    // Datos de la cubeta
                    Column {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .fontWeight(FontWeight.Medium)
                                .color(Theme.Secondary.rgb),
                            text = cubetasPredefinidas[index % cubetasPredefinidas.size]
                        )

                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Theme.Secondary.rgb),
                            text = "Dimensiones: ${cubeta.largo.toInt()}x${cubeta.ancho.toInt()} mm"
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
        }
    }
}

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