package org.dam.tfg.pages.admin

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.css.margin
import com.varabyte.kobweb.compose.css.textAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.icons.fa.FaChevronDown
import com.varabyte.kobweb.silk.components.icons.fa.FaChevronUp
import com.varabyte.kobweb.silk.components.icons.fa.FaMagnifyingGlass
import com.varabyte.kobweb.silk.components.icons.fa.FaPenToSquare
import com.varabyte.kobweb.silk.components.icons.fa.FaSearchengin
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.components.ConfirmationDialog
import org.dam.tfg.components.LoadingIndicator
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.dam.tfg.util.deleteMesa
import org.dam.tfg.util.getAllMesas
import org.dam.tfg.util.isAdminCheck
import org.dam.tfg.util.updateMesa
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import kotlin.js.Date

@Page
@Composable
fun AdminListPage() {
    isAdminCheck {
        AdminListScreenContent()
    }
}

@Composable
fun AdminListScreenContent() {
    val breakpoint = rememberBreakpoint()
    val coroutineScope = rememberCoroutineScope()

    // Estados
    var presupuestos by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var filteredPresupuestos by remember { mutableStateOf<List<Mesa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var editingMesa by remember { mutableStateOf<Mesa?>(null) }
    var mesaToDelete by remember { mutableStateOf<Mesa?>(null) }
    var expandedItems by remember { mutableStateOf(setOf<String>()) }

    // Cargar presupuestos desde la API
    LaunchedEffect(Unit) {
        isLoading = true
        presupuestos = getAllMesas().sortedByDescending { it.fechaCreacion }
        filteredPresupuestos = presupuestos
        isLoading = false
    }

    // Filtrar presupuestos cuando cambia el texto de búsqueda
    LaunchedEffect(searchText, presupuestos) {
        filteredPresupuestos = if (searchText.isBlank()) {
            presupuestos
        } else {
            presupuestos.filter { mesa ->
                // Filtrar por usuario
                mesa.username.contains(searchText, ignoreCase = true) ||
                        // O por fecha (formato simplificado)
                        mesa.fechaCreacion?.contains(searchText) == true
            }
        }
    }

    AdminPageLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .margin(topBottom = 50.px)
                .padding(left = if(breakpoint > Breakpoint.MD) SIDE_PANEL_WIDTH.px else 0.px),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .maxWidth(1000.px)
                    .padding(all = 20.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                H1(
                    attrs = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(28.px)
                        .color(Theme.Primary.rgb)
                        .textAlign(TextAlign.Center)
                        .margin(bottom = 20.px)
                        .toAttrs()
                ) {
                    Text("Gestión de Presupuestos")
                }

                // Barra de búsqueda
                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it }
                )

                if (isLoading) {
                    LoadingIndicator()
                } else if (editingMesa != null) {
                    // Formulario de edición
                    BudgetEditForm(
                        mesa = editingMesa!!,
                        onSave = { updatedMesa ->
                            coroutineScope.launch {
                                // Actualizar fecha al momento actual
                                val currentDate = Date().toISOString()
                                val mesaToUpdate = updatedMesa.let { mesa ->
                                    Mesa(
                                        id = mesa.id,
                                        username = mesa.username,
                                        tipo = mesa.tipo,
                                        fechaCreacion = currentDate,
                                        tramos = mesa.tramos,
                                        cubetas = mesa.cubetas,
                                        modulos = mesa.modulos,
                                        elementosGenerales = mesa.elementosGenerales,
                                        error = "",
                                        precioTotal = mesa.precioTotal
                                    )
                                }

                                val success = updateMesa(mesaToUpdate)
                                if (success) {
                                    // Actualizar la lista de presupuestos
                                    presupuestos = getAllMesas().sortedByDescending { it.fechaCreacion }
                                    editingMesa = null
                                }
                            }
                        },
                        onCancel = { editingMesa = null }
                    )
                } else if (filteredPresupuestos.isEmpty()) {
                    // Mensaje si no hay presupuestos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.px),
                        contentAlignment = Alignment.Center
                    ) {
                        P(
                            attrs = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(18.px)
                                .color(Theme.Secondary.rgb)
                                .textAlign(TextAlign.Center)
                                .toAttrs()
                        ) {
                            Text("No se encontraron presupuestos")
                        }
                    }
                } else {
                    // Lista de presupuestos
                    BudgetList(
                        presupuestos = filteredPresupuestos,
                        expandedItems = expandedItems,
                        onItemClick = { id ->
                            expandedItems = if (id in expandedItems) {
                                expandedItems.minus(id)
                            } else {
                                expandedItems.plus(id)
                            }
                        },
                        onEdit = { mesa -> editingMesa = mesa },
                        onDelete = { mesa -> mesaToDelete = mesa },
                        breakpoint = breakpoint
                    )
                }

                // Diálogo de confirmación de eliminación
                if (mesaToDelete != null) {
                    ConfirmationDialog(
                        mensaje = "¿Está seguro que desea eliminar este presupuesto? Esta acción no se puede deshacer.",
                        onConfirm = {
                            coroutineScope.launch {
                                val success = deleteMesa(mesaToDelete!!.id)
                                if (success) {
                                    // Actualizar lista eliminando el presupuesto
                                    presupuestos = presupuestos.filter { it.id != mesaToDelete!!.id }
                                    filteredPresupuestos = filteredPresupuestos.filter { it.id != mesaToDelete!!.id }
                                }
                                mesaToDelete = null
                            }
                        },
                        onCancel = {
                            mesaToDelete = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    val breakpoint = rememberBreakpoint()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 20.px)
            .backgroundColor(Colors.White)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Theme.LightGray.rgb)
            .padding(all = 10.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FaMagnifyingGlass(
            modifier = Modifier
                .color(Theme.Secondary.rgb)
                .margin(right = 8.px)
        )

        TextInput(
            text = searchText,
            onTextChange = onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .backgroundColor(Colors.Transparent),
            placeholder = if (breakpoint < Breakpoint.MD) "Buscar..." else "Buscar por usuario o fecha..."
        )
    }
}

@Composable
fun BudgetList(
    presupuestos: List<Mesa>,
    expandedItems: Set<String>,
    onItemClick: (String) -> Unit,
    onEdit: (Mesa) -> Unit,
    onDelete: (Mesa) -> Unit,
    breakpoint: Breakpoint
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.px)
    ) {
        presupuestos.forEach { mesa ->
            BudgetCard(
                mesa = mesa,
                isExpanded = mesa.id in expandedItems,
                onClick = { onItemClick(mesa.id) },
                onEdit = { onEdit(mesa) },
                onDelete = { onDelete(mesa) },
                breakpoint = breakpoint
            )
        }
    }
}

@Composable
fun BudgetCard(
    mesa: Mesa,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    breakpoint: Breakpoint
) {
    val resourceProvider = WebResourceProvider()
    val date = remember(mesa.fechaCreacion) {
        try {
            val jsDate = Date(mesa.fechaCreacion ?: "")
            val day = jsDate.getDate().toString().padStart(2, '0')
            val month = (jsDate.getMonth() + 1).toString().padStart(2, '0')
            val year = jsDate.getFullYear().toString()
            "$day/$month/$year"
        } catch (e: Exception) {
            "Fecha desconocida"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.px, color = Theme.LightGray.rgb)
            .borderRadius(8.px)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Theme.LightGray.rgb)
            .backgroundColor(Colors.White)
            .margin(bottom = 16.px)
            .transition(Transition.of("all", 300.ms))
    ) {
        // Cabecera del presupuesto (siempre visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.px)
                .backgroundColor(Theme.Primary.rgb)
                .borderRadius(topLeft = 8.px, topRight = 8.px)
                .cursor(Cursor.Pointer)
                .onClick { onClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                // Primera fila: Usuario y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Usuario
                    SpanText(
                        text = "Usuario: ${mesa.username}",
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Bold)
                            .color(Theme.Secondary.rgb)
                    )

                    if (breakpoint > Breakpoint.SM) {
                        // Fecha (solo visible en pantallas no muy pequeñas)
                        SpanText(
                            text = "Fecha: $date",
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(14.px)
                                .color(Theme.Secondary.rgb)
                        )
                    }
                }

                if (breakpoint <= Breakpoint.SM) {
                    // Fecha en dispositivos pequeños (debajo del usuario)
                    SpanText(
                        text = "Fecha: $date",
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(14.px)
                            .color(Theme.Secondary.rgb)
                            .margin(top = 4.px)
                    )
                }

                // Segunda fila: Tipo de mesa y precio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(top = 8.px),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tipo de mesa
                    SpanText(
                        text = "Mesa de ${mesa.tipo} tramos",
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .color(Theme.Secondary.rgb)
                    )

                    // Precio total
                    SpanText(
                        text = "Precio: ${formatPrice(mesa.precioTotal)}€",
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Bold)
                            .color(Theme.Primary.rgb)
                    )
                }
            }

            // Icono de expansión
            Box(
                modifier = Modifier.margin(left = 16.px),
                contentAlignment = Alignment.Center
            ) {
                if (isExpanded) {
                    FaChevronUp(
                        modifier = Modifier
                            .color(Theme.Secondary.rgb)
                            .fontSize(16.px)
                    )
                } else {
                    FaChevronDown(
                        modifier = Modifier
                            .color(Theme.Secondary.rgb)
                            .fontSize(16.px)
                    )
                }
            }
        }

        // Detalles del presupuesto (visibles solo cuando está expandido)
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.px)
            ) {
                // Tramos
                if (mesa.tramos.isNotEmpty()) {
                    H3(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .color(Theme.Primary.rgb)
                            .margin(bottom = 8.px, top = 8.px)
                            .toAttrs()
                    ) {
                        Text("Tramos")
                    }

                    mesa.tramos.forEach { tramo ->
                        DetailCard(
                            title = "Tramo ${tramo.numero}",
                            properties = mapOf(
                                "Tipo" to tramo.tipo.toString(),
                                "Largo" to "${tramo.largo} cm",
                                "Ancho" to "${tramo.ancho} cm",
                                "Precio" to "${formatPrice(tramo.precio)}€"
                            )
                        )
                    }
                }

                // Cubetas
                if (mesa.cubetas.isNotEmpty()) {
                    H3(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .color(Theme.Primary.rgb)
                            .margin(bottom = 8.px, top = 16.px)
                            .toAttrs()
                    ) {
                        Text("Cubetas")
                    }

                    mesa.cubetas.forEach { cubeta ->
                        DetailCard(
                            title = "${cubeta.tipo} (${cubeta.numero})",
                            properties = mapOf(
                                "Largo" to "${cubeta.largo} mm",
                                "Fondo" to "${cubeta.fondo} mm",
                                "Alto" to "${cubeta.alto ?: 0} mm",
                                "Precio" to "${formatPrice(cubeta.precio)}€"
                            )
                        )
                    }
                }

                // Módulos
                if (mesa.modulos.isNotEmpty()) {
                    H3(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .color(Theme.Primary.rgb)
                            .margin(bottom = 8.px, top = 16.px)
                            .toAttrs()
                    ) {
                        Text("Módulos")
                    }

                    mesa.modulos.forEach { modulo ->
                        val imagePath = resourceProvider.getImagePath(
                            modulo.nombre.uppercase()
                                .replace(" ", "_")
                                .replace("Ó", "O")
                                .replace("Á", "A")
                        )

                        DetailCard(
                            title = "${modulo.nombre} (${modulo.cantidad})",
                            properties = mapOf(
                                "Largo" to "${modulo.largo} mm",
                                "Fondo" to "${modulo.fondo} mm",
                                "Alto" to "${modulo.alto} mm",
                                "Precio unitario" to "${formatPrice(modulo.precio / modulo.cantidad)}€",
                                "Precio total" to "${formatPrice(modulo.precio)}€"
                            ),
                            imagePath = imagePath
                        )
                    }
                }

                // Elementos generales
                if (mesa.elementosGenerales.isNotEmpty()) {
                    H3(
                        attrs = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(18.px)
                            .color(Theme.Primary.rgb)
                            .margin(bottom = 8.px, top = 16.px)
                            .toAttrs()
                    ) {
                        Text("Elementos generales")
                    }

                    mesa.elementosGenerales.forEach { elemento ->
                        val imagePath = resourceProvider.getImagePath(
                            elemento.nombre.uppercase()
                                .replace(" ", "_")
                                .replace("Ó", "O")
                                .replace("Á", "A")
                        )

                        DetailCard(
                            title = "${elemento.nombre} (${elemento.cantidad})",
                            properties = mapOf(
                                "Precio unitario" to "${formatPrice(elemento.precio / elemento.cantidad)}€",
                                "Precio total" to "${formatPrice(elemento.precio)}€"
                            ),
                            imagePath = imagePath
                        )
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(top = 20.px),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Editar
                    Box(
                        modifier = Modifier
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px, leftRight = 16.px)
                            .cursor(Cursor.Pointer)
                            .margin(right = 12.px)
                            .onClick { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.px)
                        ) {
                            FaPenToSquare(
                                modifier = Modifier.color(Colors.White)
                            )
                            if (breakpoint >= Breakpoint.SM) {
                                SpanText(
                                    text = "Editar",
                                    modifier = Modifier
                                        .fontFamily(FONT_FAMILY)
                                        .color(Colors.White)
                                        .fontSize(14.px)
                                )
                            }
                        }
                    }

                    // Botón Eliminar
                    Box(
                        modifier = Modifier
                            .backgroundColor(Colors.Red)
                            .borderRadius(4.px)
                            .padding(topBottom = 8.px, leftRight = 16.px)
                            .cursor(Cursor.Pointer)
                            .onClick { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.px)
                        ) {
                            FaTrash(
                                modifier = Modifier.color(Colors.White)
                            )
                            if (breakpoint >= Breakpoint.SM) {
                                SpanText(
                                    text = "Eliminar",
                                    modifier = Modifier
                                        .fontFamily(FONT_FAMILY)
                                        .color(Colors.White)
                                        .fontSize(14.px)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    properties: Map<String, String>,
    imagePath: String? = null
) {
    val breakpoint = rememberBreakpoint()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 12.px)
            .border(1.px, color = Theme.LightGray.rgb)
            .borderRadius(4.px)
            .padding(all = 12.px)
            .backgroundColor(rgba(245, 245, 250, 0.8))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen (opcional)
            if (imagePath != null && breakpoint >= Breakpoint.MD) {
                Box(
                    modifier = Modifier
                        .width(60.px)
                        .height(60.px)
                        .margin(right = 16.px),
                    contentAlignment = Alignment.Center
                ) {
                    Img(
                        src = imagePath,
                        alt = title,
                        attrs = Modifier
                            .maxWidth(60.px)
                            .maxHeight(60.px)
                            .toAttrs()
                    )
                }
            }

            // Contenido
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título
                SpanText(
                    text = title,
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb)
                        .margin(bottom = 8.px)
                )

                // Propiedades
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // En dispositivos pequeños, mostrar como columna
                    if (breakpoint < Breakpoint.MD) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.px)
                        ) {
                            properties.forEach { (property, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SpanText(
                                        text = "$property:",
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                    )
                                    SpanText(
                                        text = value,
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                            .fontWeight(FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    } else {
                        // En dispositivos más grandes, organizar en dos columnas
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.px)
                        ) {
                            properties.entries.take(properties.size / 2 + properties.size % 2).forEach { (property, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SpanText(
                                        text = "$property:",
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                    )
                                    SpanText(
                                        text = value,
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                            .fontWeight(FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .margin(left = 24.px),
                            verticalArrangement = Arrangement.spacedBy(4.px)
                        ) {
                            properties.entries.drop(properties.size / 2 + properties.size % 2).forEach { (property, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SpanText(
                                        text = "$property:",
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                    )
                                    SpanText(
                                        text = value,
                                        modifier = Modifier
                                            .fontFamily(FONT_FAMILY)
                                            .fontSize(14.px)
                                            .color(Theme.Secondary.rgb)
                                            .fontWeight(FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetEditForm(
    mesa: Mesa,
    onSave: (Mesa) -> Unit,
    onCancel: () -> Unit
) {
    var editedMesa by remember { mutableStateOf(mesa) }
    var username by remember { mutableStateOf(mesa.username) }
    val breakpoint = rememberBreakpoint()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.px)
            .border(1.px, color = Theme.LightGray.rgb)
            .borderRadius(8.px)
            .backgroundColor(Colors.White)
            .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 4.px, color = Theme.LightGray.rgb)
    ) {
        // Título del formulario
        H3(
            attrs = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(24.px)
                .color(Theme.Primary.rgb)
                .margin(bottom = 16.px)
                .textAlign(TextAlign.Center)
                .fillMaxWidth()
                .toAttrs()
        ) {
            Text("Editar Presupuesto")
        }

        // Datos básicos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.px)
        ) {
            // Campo de usuario
            SpanText(
                text = "Usuario",
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .fontWeight(FontWeight.Bold)
                    .margin(bottom = 8.px)
            )

            TextInput(
                text = username,
                onTextChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.px)
                    .margin(bottom = 16.px)
                    .padding(leftRight = 12.px)
                    .border(1.px, color = Theme.LightGray.rgb)
                    .borderRadius(4.px)
                    .fontFamily(FONT_FAMILY)
            )
        }

        // Nota informativa
        P(
            attrs = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .color(Theme.Secondary.rgb)
                .padding(8.px)
                .backgroundColor(Theme.Primary.rgb)
                .borderRadius(4.px)
                .margin(bottom = 20.px)
                .toAttrs()
        ) {
            Text("Al guardar, se actualizará la fecha del presupuesto a la fecha actual.")
        }

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if (breakpoint < Breakpoint.MD) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Cancelar
            Box(
                modifier = Modifier
                    .backgroundColor(Theme.LightGray.rgb)
                    .borderRadius(4.px)
                    .padding(topBottom = 12.px, leftRight = if (breakpoint < Breakpoint.MD) 16.px else 20.px)
                    .cursor(Cursor.Pointer)
                    .onClick { onCancel() }
                    .let { if (breakpoint >= Breakpoint.MD) it.margin(right = 16.px) else it },
                contentAlignment = Alignment.Center
            ) {
                SpanText(
                    text = "Cancelar",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .color(Theme.Secondary.rgb)
                )
            }

            // Botón Guardar
            Box(
                modifier = Modifier
                    .backgroundColor(Theme.Primary.rgb)
                    .borderRadius(4.px)
                    .padding(topBottom = 12.px, leftRight = if (breakpoint < Breakpoint.MD) 16.px else 20.px)
                    .cursor(Cursor.Pointer)
                    .onClick {
                        // Actualizar el nombre de usuario y guardar
                        val mesaActualizada = Mesa(
                            id = editedMesa.id,
                            username = username,  // Actualizamos el username
                            tipo = editedMesa.tipo,
                            fechaCreacion = editedMesa.fechaCreacion,
                            tramos = editedMesa.tramos,
                            cubetas = editedMesa.cubetas,
                            modulos = editedMesa.modulos,
                            elementosGenerales = editedMesa.elementosGenerales,
                            error = editedMesa.error ?: "",
                            precioTotal = editedMesa.precioTotal
                        )
                        onSave(mesaActualizada)
                    },
                contentAlignment = Alignment.Center
            ) {
                SpanText(
                    text = "Guardar",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .color(Colors.White)
                )
            }
        }
    }
}

// Función auxiliar para formatear precios
fun formatPrice(price: Double): String {
    return try {
        val formatter = js("new Intl.NumberFormat('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 })")
        formatter.format(price)
    } catch (e: Exception) {
        e.printStackTrace().toString()
    }
}