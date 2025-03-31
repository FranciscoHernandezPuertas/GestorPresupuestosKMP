package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.*
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.components.AdvertenciaOverlay
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.ConfirmDialog
import org.dam.tfg.components.ExtraItemsSection
import org.dam.tfg.components.QuantitySelector
import org.dam.tfg.components.StandardItemRenderer
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.*
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun TableSelectorElementsPage() {
    TableSelectorElementsScreen()
}

@Composable
fun TableSelectorElementsScreen() {
    var elementosGenerales by remember { mutableStateOf(emptyList<ElementosGenerales>()) }
    var cubetas by remember { mutableStateOf(emptyList<Cubeta>()) }

    // Estados para diálogo de confirmación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var elementoAEliminar by remember { mutableStateOf<Pair<String, Int>?>(null) }

    var elementoParaConfirmarCantidadCero by remember { mutableStateOf<Triple<String, Int, Int>?>(null) }

    // Listas predefinidas (sin cambios)...
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

    // Definir límites para cada tipo de cubeta
    val cubetasConLimites = remember {
        listOf(
            ItemWithLimits(
                id = "cubeta_d300",
                name = "Diametro 300x180",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_d360",
                name = "Diametro 360x180",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_d380",
                name = "Diametro 380x180",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_d420",
                name = "Diametro 420x180",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_d460",
                name = "Diametro 460x180",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            // Cubetas cuadradas
            ItemWithLimits(
                id = "cubeta_c400_250",
                name = "Cuadrada 400x400x250",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_c400_300",
                name = "Cuadrada 400x400×300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_c450_250",
                name = "Cuadrada 450x450x250",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_c450_300",
                name = "Cuadrada 450x450x300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_c500_250",
                name = "Cuadrada 500×500×250",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_c500_300",
                name = "Cuadrada 500x500×300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            // Cubetas rectangulares
            ItemWithLimits(
                id = "cubeta_r325",
                name = "Rectangular 325x300x150",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r500_300",
                name = "Rectangular 500x300x300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r500_400",
                name = "Rectangular 500x400x250",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r600_450",
                name = "Rectangular 600x450x300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r600_500_250",
                name = "Rectangular 600x500×250",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r600_500_300",
                name = "Rectangular 600x500x300",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r600_500_320",
                name = "Rectangular 600x500x320",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r630",
                name = "Rectangular 630x510x380",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r700",
                name = "Rectangular 700x450x350",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r800",
                name = "Rectangular 800x500x380",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r955",
                name = "Rectangular 955x510x380",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "cubeta_r1280",
                name = "Rectangular 1280x510x380",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            )
        )
    }

    // Límites para elementos generales
    val elementosGeneralesConLimites = remember {
        listOf(
            ItemWithLimits(
                id = "elemento_1",
                name = "Peto lateral",
                minQuantity = 0,
                maxQuantity = 10,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_2",
                name = "Kit lavamanos pulsador",
                minQuantity = 0,
                maxQuantity = 5,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_3",
                name = "Esquina en chaflán",
                minQuantity = 0,
                maxQuantity = 8,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_4",
                name = "Kit lavam. pedal simple",
                minQuantity = 0,
                maxQuantity = 3,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_5",
                name = "Esquina redondeada",
                minQuantity = 0,
                maxQuantity = 8,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_6",
                name = "Kit lavam. pedal doble",
                minQuantity = 0,
                maxQuantity = 2,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_7",
                name = "Cajeado columna",
                minQuantity = 0,
                maxQuantity = 4,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_8",
                name = "Baquetón en seno",
                minQuantity = 0,
                maxQuantity = 15,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_9",
                name = "Aro de desbarace",
                minQuantity = 0,
                maxQuantity = 6,
                initialQuantity = 1
            ),
            ItemWithLimits(
                id = "elemento_10",
                name = "Baqueton perimetrico",
                minQuantity = 0,
                maxQuantity = 4,
                initialQuantity = 1
            )
        )
    }


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

    LaunchedEffect(Unit) {
        // Filtramos cubetas vacías para evitar la 'cubeta fantasma'
        try {
            val savedElementsJson = localStorage.getItem("table_elements")
            if (!savedElementsJson.isNullOrBlank()) {
                val savedElements = Json.decodeFromString<List<Extra>>(savedElementsJson)

                elementosGenerales = savedElements
                    .filterIsInstance<ElementosGenerales>()
                    .filter { it.tipo.isNotBlank() }

                cubetas = savedElements
                    .filterIsInstance<Cubeta>()
                    .filter { it.tipo.isNotBlank() }  // Evita cubetas de tipo vacío
            }
        } catch (e: Exception) {
            console.log("Error al cargar los elementos: ${e.message}")
        }
    }

    fun validateData(): Boolean {
        return true
    }

    // Aquí, cada vez que agreguemos o modifiquemos algo, llamamos a saveData()
    fun eliminarElemento(tipo: String, indice: Int) {
        when (tipo) {
            "ElementosGenerales" -> {
                elementosGenerales = elementosGenerales.filterIndexed { i, _ -> i != indice }
            }

            "Cubeta" -> {
                cubetas = cubetas.filterIndexed { i, _ -> i != indice }
            }
        }
        saveData()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(title = "Elementos de la mesa")

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                    elementosGenerales = elementosGenerales,
                    elementosPredefinidos = elementosGeneralesPredefinidos, // Mantén la lista original
                    elementosConLimites = elementosGeneralesConLimites, // Añade la nueva lista con límites
                    onElementoAdded = { elemento ->
                        elementosGenerales = elementosGenerales + elemento
                        saveData() // Guardar enseguida
                    },
                    onCantidadChanged = { index, cantidad ->
                        elementosGenerales = elementosGenerales.mapIndexed { i, elem ->
                            if (i == index) elem.copy(numero = cantidad) else elem
                        }
                        saveData() // Guardar enseguida
                    },
                    onDeleteClick = { index ->
                        elementoAEliminar = Pair("ElementosGenerales", index)
                        showDeleteDialog = true
                    },
                    onQuantityZero = { index ->
                        elementoParaConfirmarCantidadCero = Triple("ElementosGenerales", index, 0)
                    }
                )

                // Sección de Cubetas
                CubetasSection(
                    cubetas = cubetas,
                    cubetasConLimites = cubetasConLimites,
                    onCubetaAdded = { cubeta ->
                        cubetas = cubetas + cubeta
                        saveData()
                    },
                    onCantidadChanged = { index, cantidad ->
                        cubetas = cubetas.mapIndexed { i, elem ->
                            if (i == index) elem.copy(numero = cantidad) else elem
                        }
                        saveData()
                    },
                    onDeleteClick = { index ->
                        elementoAEliminar = Pair("Cubeta", index)
                        showDeleteDialog = true
                    },
                    onQuantityZero = { index ->
                        elementoParaConfirmarCantidadCero = Triple("Cubeta", index, 0)
                    }
                )

            }
        }

        BudgetFooter(
            previousScreen = Screen.TableSelector,
            nextScreen = Screen.Home,
            validateData = { validateData() },
            saveData = { saveData() }
        )
    }

    if (elementoParaConfirmarCantidadCero != null) {
        ConfirmDialog(
            message = "La cantidad ha llegado a 0. ¿Desea eliminar este elemento?",
            onConfirm = {
                val (tipo, indice, _) = elementoParaConfirmarCantidadCero!!
                eliminarElemento(tipo, indice)
                elementoParaConfirmarCantidadCero = null
            },
            onDismiss = {
                val (tipo, indice, cantidadAnterior) = elementoParaConfirmarCantidadCero!!
                // Restaurar la cantidad a 1 si se cancela
                when (tipo) {
                    "ElementosGenerales" -> {
                        elementosGenerales = elementosGenerales.mapIndexed { i, elem ->
                            if (i == indice) elem.copy(numero = 1) else elem
                        }
                    }
                    "Cubeta" -> {
                        cubetas = cubetas.mapIndexed { i, elem ->
                            if (i == indice) elem.copy(numero = 1) else elem
                        }
                    }
                }
                saveData()
                elementoParaConfirmarCantidadCero = null
            }
        )
    }

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
    elementosConLimites: List<ItemWithLimits>,
    onElementoAdded: (ElementosGenerales) -> Unit,
    onCantidadChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onQuantityZero: (Int) -> Unit
) {
    val breakpoint = rememberBreakpoint()
    val elementosPorFila =
        if (breakpoint > Breakpoint.MD) 3 else if (breakpoint > Breakpoint.SM) 2 else 1

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

        // Grid de elementos predefinidos con imágenes - Ahora visible en todas las pantallas
        val gridTemplateColumns = when (elementosPorFila) {
            1 -> "1fr"
            2 -> "1fr 1fr"
            else -> "1fr 1fr 1fr"
        }

        Div(
            attrs = Modifier
                .fillMaxWidth()
                .margin(bottom = 20.px)
                .styleModifier {
                    property("display", "grid")
                    property("grid-template-columns", gridTemplateColumns)
                    property("grid-gap", "20px")
                }
                .toAttrs()
        ) {
            elementosPredefinidos.forEach { tipo ->
                // Comprobar si ya existe en la lista
                val yaExiste = elementosGenerales.any { it.tipo == tipo }
                // Obtener los límites para este elemento
                val itemWithLimits = elementosConLimites.find { it.name == tipo }

                val imageSrc = when (tipo) {
                    "Peto lateral" -> Res.Image.petoLateral
                    "Kit lavamanos pulsador" -> Res.Image.kitLavamanosPulsador
                    "Esquina en chaflán" -> Res.Image.esquinaEnChaflan
                    "Kit lavam. pedal simple" -> Res.Image.kitLavamanosPedalSimple
                    "Esquina redondeada" -> Res.Image.esquinaRedondeada
                    "Kit lavam. pedal doble" -> Res.Image.kitLavamanosPedalDoble
                    "Cajeado columna" -> Res.Image.cajeadoColumna
                    "Baquetón en seno" -> Res.Image.baquetonEnSeno
                    "Aro de desbarace" -> Res.Image.aroDeDesbrace
                    "Baqueton perimetrico" -> Res.Image.baquetonPerimetrico
                    else -> Res.Image.noSeleccionado
                }

                // Tarjeta mejorada con más definición visual
                Div(
                    attrs = Modifier
                        .padding(if (breakpoint <= Breakpoint.SM) 8.px else 15.px)
                        .backgroundColor(if (yaExiste) Theme.LightGray.rgb else Colors.White)
                        .borderRadius(12.px)
                        .border(
                            width = 2.px,
                            style = LineStyle.Solid,
                            color = if (yaExiste) Theme.Primary.rgb else Theme.LightGray.rgb
                        )
                        .boxShadow(
                            offsetX = 0.px,
                            offsetY = 3.px,
                            blurRadius = 8.px,
                            color = rgba(0, 0, 0, 0.1)
                        )
                        .toAttrs()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Imagen
                        Box(
                            modifier = Modifier
                                .size(if (breakpoint <= Breakpoint.SM) 70.px else 100.px)
                                .margin(bottom = 10.px),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                src = imageSrc,
                                alt = tipo,
                                modifier = Modifier.size(if (breakpoint <= Breakpoint.SM) 60.px else 80.px)
                            )
                        }

                        // Nombre del elemento
                        SpanText(
                            text = tipo,
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(if (breakpoint <= Breakpoint.SM) 14.px else 16.px)
                                .fontWeight(FontWeight.Medium)
                                .color(Theme.Secondary.rgb)
                                .textAlign(TextAlign.Center)
                                .margin(bottom = 10.px)
                        )

                        // Botón de añadir o mensaje de ya añadido
                        if (!yaExiste) {
                            Button(
                                attrs = Modifier
                                    .width(if (breakpoint <= Breakpoint.SM) 100.px else 120.px)
                                    .height(36.px)
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .borderRadius(4.px)
                                    .border(0.px)
                                    .cursor(Cursor.Pointer)
                                    .margin(top = 8.px, bottom = 8.px)
                                    .onClick {
                                        val initialQuantity = itemWithLimits?.initialQuantity ?: 1
                                        onElementoAdded(
                                            ElementosGenerales(
                                                tipo = tipo,
                                                numero = initialQuantity
                                            )
                                        )
                                    }
                                    .toAttrs()
                            ) {
                                Text("Añadir")
                            }
                        } else {
                            SpanText(
                                text = "Ya añadido",
                                modifier = Modifier
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(14.px)
                                    .fontStyle(FontStyle.Italic)
                                    .color(Theme.Primary.rgb)
                                    .margin(top = 8.px, bottom = 8.px)
                            )
                        }
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
                    .margin(top = 20.px, bottom = 15.px),
                text = "Elementos añadidos"
            )

            elementosGenerales.forEachIndexed { index, elemento ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 10.px)
                        .padding(16.px)
                        .backgroundColor(Theme.LightGray.rgb)
                        .borderRadius(8.px)
                        .boxShadow(
                            offsetX = 0.px,
                            offsetY = 2.px,
                            blurRadius = 4.px,
                            color = rgba(0, 0, 0, 0.1)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del elemento
                    val imageSrc = when (elemento.tipo) {
                        "Peto lateral" -> Res.Image.petoLateral
                        "Kit lavamanos pulsador" -> Res.Image.kitLavamanosPulsador
                        "Esquina en chaflán" -> Res.Image.esquinaEnChaflan
                        "Kit lavam. pedal simple" -> Res.Image.kitLavamanosPedalSimple
                        "Esquina redondeada" -> Res.Image.esquinaRedondeada
                        "Kit lavam. pedal doble" -> Res.Image.kitLavamanosPedalDoble
                        "Cajeado columna" -> Res.Image.cajeadoColumna
                        "Baquetón en seno" -> Res.Image.baquetonEnSeno
                        "Aro de desbarace" -> Res.Image.aroDeDesbrace
                        "Baqueton perimetrico" -> Res.Image.baquetonPerimetrico
                        else -> Res.Image.noSeleccionado
                    }

                    Box(
                        modifier = Modifier
                            .size(50.px)
                            .margin(right = 16.px)
                            .backgroundColor(Colors.White)
                            .borderRadius(8.px)
                            .padding(5.px),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier.size(40.px),
                            src = imageSrc,
                            alt = elemento.tipo
                        )
                    }

                    // Nombre del elemento
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Medium)
                            .color(Theme.Secondary.rgb),
                        text = elemento.tipo
                    )

                    Spacer()

                    // Aquí aplicamos los límites personalizados para el selector de cantidad
                    val itemLimit = elementosConLimites.find { it.name == elemento.tipo }
                    val min = itemLimit?.minQuantity ?: 0
                    val max = itemLimit?.maxQuantity ?: 10

                    QuantitySelector(
                        value = elemento.numero,
                        onValueChange = { cantidad ->
                            if (cantidad == 0) {
                                onQuantityZero(index)
                            } else {
                                onCantidadChanged(index, cantidad)
                            }
                        },
                        min = min,
                        max = max,
                        showText = false
                    )

                    // Botón de eliminar
                    Box(
                        modifier = Modifier
                            .margin(left = 10.px)
                            .size(30.px)
                            .backgroundColor(Colors.Red)
                            .flexShrink(0)
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.px),
                contentAlignment = Alignment.Center
            ) {
                SpanText(
                    text = "No ha añadido ningún elemento adicional",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .fontStyle(FontStyle.Italic)
                        .color(Theme.Secondary.rgb)
                )
            }
        }
    }
}

@Composable
fun CubetasSection(
    cubetas: List<Cubeta>,
    cubetasConLimites: List<ItemWithLimits>,
    onCubetaAdded: (Cubeta) -> Unit,
    onCantidadChanged: (Int, Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onQuantityZero: (Int) -> Unit
) {
    // Cargar la mesa desde BudgetManager
    val mesa = remember { mutableStateOf(BudgetManager.loadMesa()) }

    // Calcular área total de la mesa
    val areaTotal = remember(mesa.value) {
        mesa.value.tramos.sumOf { it.largo * it.ancho }
    }

    // Calcular área ocupada por las cubetas actuales
    val areaOcupada = remember(cubetas) {
        cubetas.sumOf { it.largo * it.ancho * it.numero }
    }

    // Espacio disponible restante
    val areaDisponible = areaTotal - areaOcupada

    // Dimensiones mínimas para validación
    val dimensionesDisponibles = remember(mesa.value) {
        var minLargo = Double.MAX_VALUE
        var minAncho = Double.MAX_VALUE

        mesa.value.tramos.forEach { tramo ->
            if (tramo.largo > 0 && tramo.largo < minLargo) minLargo = tramo.largo
            if (tramo.ancho > 0 && tramo.ancho < minAncho) minAncho = tramo.ancho
        }

        if (minLargo == Double.MAX_VALUE) minLargo = 0.0
        if (minAncho == Double.MAX_VALUE) minAncho = 0.0

        Pair(minLargo, minAncho)
    }

    // Filtrar y calcular límites dinámicos para las cubetas
    val cubetasDisponiblesDinamicas = remember(cubetas, mesa.value, areaDisponible) {
        val tiposYaAñadidos = cubetas.map { it.tipo }

        cubetasConLimites.mapNotNull { item ->
            // Si ya está añadida, no la mostramos en opciones disponibles
            if (tiposYaAñadidos.contains(item.name)) return@mapNotNull null

            // Extraer dimensiones de la cubeta
            val dimensionesCubeta = extraerDimensionesCubeta(item.name)
            val largoCubeta = dimensionesCubeta.first
            val anchoCubeta = dimensionesCubeta.second

            // Verificar si la cubeta cabe en la mesa (dimensionalmente)
            if (largoCubeta > dimensionesDisponibles.first ||
                anchoCubeta > dimensionesDisponibles.second) {
                return@mapNotNull null
            }

            // Calcular cuántas cubetas de este tipo cabrían en el área disponible
            val areaCubeta = largoCubeta * anchoCubeta
            val maximoCubetasPorArea = if (areaCubeta > 0) (areaDisponible / areaCubeta).toInt() else 0

            // Limitar cantidad por dimensiones físicas y por límite original
            val nuevoMaximo = minOf(maximoCubetasPorArea, item.maxQuantity)

            // Solo mostrar si al menos cabe una
            if (nuevoMaximo > 0) {
                item.copy(maxQuantity = nuevoMaximo)
            } else {
                null
            }
        }
    }

    var mostrarAdvertencia by remember { mutableStateOf(false) }
    var mensajeAdvertencia by remember { mutableStateOf("") }

    ExtraItemsSection(
        title = "Cubetas",
        description = "Seleccione cubetas que se ajusten a las dimensiones de su mesa (${dimensionesDisponibles.first.toInt()}x${dimensionesDisponibles.second.toInt()}mm)",
        imageSrc = Res.Image.cubeta,
        items = cubetas,
        itemOptions = cubetasDisponiblesDinamicas,
        onItemAdded = { tipo, numero ->
            val dimensiones = extraerDimensionesCubeta(tipo)
            val areaCubeta = dimensiones.first * dimensiones.second

            // Verificar si hay suficiente espacio para añadir esta cubeta
            if (areaCubeta * numero <= areaDisponible) {
                onCubetaAdded(Cubeta(
                    tipo = tipo,
                    largo = dimensiones.first,
                    ancho = dimensiones.second,
                    numero = numero,
                    maxQuantity = (areaDisponible / areaCubeta).toInt()
                ))
            } else {
                mensajeAdvertencia = "No hay suficiente espacio disponible para añadir esta cubeta."
                mostrarAdvertencia = true
            }
        },
        onQuantityChanged = { index, cantidad ->
            // Validar que la cantidad no exceda el límite disponible
            val cubeta = cubetas[index]
            val areaCubeta = cubeta.largo * cubeta.ancho
            val espacioUsadoActual = cubeta.largo * cubeta.ancho * cubeta.numero
            val espacioTotalDisponible = areaDisponible + espacioUsadoActual
            val maxDisponible = if (areaCubeta > 0) (espacioTotalDisponible / areaCubeta).toInt() else 0

            // Sólo permitir el cambio si no excede el límite
            if (cantidad <= maxDisponible) {
                onCantidadChanged(index, cantidad)
            } else {
                mensajeAdvertencia = "No hay suficiente espacio disponible para añadir más cubetas de este tipo."
                mostrarAdvertencia = true
            }
        },
        onDeleteClick = onDeleteClick,
        itemRenderer = { cubeta, index, limites ->
            // Calcular el límite máximo dinámico para esta cubeta ya añadida
            val areaCubeta = cubeta.largo * cubeta.ancho
            val cubetasActuales = cubeta.numero
            val espacioRestante = areaDisponible + (areaCubeta * cubetasActuales)
            val maxDisponible = if (areaCubeta > 0) (espacioRestante / areaCubeta).toInt() else 0
            val limiteDinamico = minOf(maxDisponible, limites?.maxQuantity ?: 3)

            StandardItemRenderer(
                item = cubeta,
                index = index,
                itemWithLimits = limites?.copy(maxQuantity = limiteDinamico),
                onQuantityChanged = { idx, cantidad ->
                    if (cantidad == 0) {
                        onQuantityZero(idx)
                    } else if (cantidad <= limiteDinamico) {  // Verificación adicional
                        onCantidadChanged(idx, cantidad)
                    }
                },
                onDeleteClick = onDeleteClick,
                getDimensionsText = { extra ->
                    val dimensiones = extra.tipo.replace(Regex(".*?(\\d+[xX×]\\d+([xX×]\\d+)?).*"), "$1")
                    "Dimensiones: $dimensiones mm"
                }
            )
        }
    )
    if (mostrarAdvertencia) {
        AdvertenciaOverlay(
            mensaje = mensajeAdvertencia,
            onDismiss = { mostrarAdvertencia = false },
            autoHide = true,
            duracionMs = 5000
        )
    }

}
private fun extraerDimensionesCubeta(nombre: String): Pair<Double, Double> {
    // Para cubetas circulares (diámetro)
    val regexDiametro = Regex("Diametro (\\d+)x(\\d+)")
    regexDiametro.find(nombre)?.let {
        val diametro = it.groupValues[1].toDoubleOrNull() ?: 0.0
        return Pair(diametro, diametro)
    }

    // Para cubetas rectangulares o cuadradas (LxAxA)
    val regexDimensiones = Regex("(\\d+)x(\\d+)x(\\d+)")
    regexDimensiones.find(nombre)?.let {
        val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
        val ancho = it.groupValues[2].toDoubleOrNull() ?: 0.0
        return Pair(largo, ancho)
    }

    // Formato alternativo con × o X
    val regexAlternativo = Regex("(\\d+)[×X](\\d+)[×X](\\d+)")
    regexAlternativo.find(nombre)?.let {
        val largo = it.groupValues[1].toDoubleOrNull() ?: 0.0
        val ancho = it.groupValues[2].toDoubleOrNull() ?: 0.0
        return Pair(largo, ancho)
    }

    // Si no se puede extraer, devolver dimensiones mínimas
    return Pair(0.0, 0.0)
}
