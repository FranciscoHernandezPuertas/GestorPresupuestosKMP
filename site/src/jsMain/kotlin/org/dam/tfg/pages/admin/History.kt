package org.dam.tfg.pages.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.bottom
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
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.icons.fa.FaMagnifyingGlass
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.models.History
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.dam.tfg.util.deleteHistory
import org.dam.tfg.util.getAllHistory
import org.dam.tfg.util.isAdminCheck
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.SearchInput
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr
import kotlin.js.Date


@Page
@Composable
fun AdminHistoryPage() {
    isAdminCheck {
        AdminHistoryContent()
    }
}

@Composable
fun AdminHistoryContent() {
    val breakpoint = rememberBreakpoint()
    val scope = rememberCoroutineScope()
    val isMobile = breakpoint < Breakpoint.MD

    // Estados para gestionar los datos y la UI
    var historyEntries by remember { mutableStateOf<List<History>>(emptyList()) }
    var filteredEntries by remember { mutableStateOf<List<History>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    // Estado para el diálogo de confirmación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<History?>(null) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        try {
            historyEntries = getAllHistory().sortedByDescending { it.timestamp }
            filteredEntries = historyEntries
            isLoading = false
        } catch (e: Exception) {
            error = "Error al cargar el historial: ${e.message}"
            isLoading = false
        }
    }

    // Filtrar entradas cuando cambia el texto de búsqueda
    LaunchedEffect(searchText, historyEntries) {
        if (searchText.isBlank()) {
            filteredEntries = historyEntries
        } else {
            val query = searchText.lowercase()
            filteredEntries = historyEntries.filter { entry ->
                entry.userId.lowercase().contains(query) ||
                        entry.action.lowercase().contains(query) ||
                        entry.timestamp.lowercase().contains(query) ||
                        entry.details.lowercase().contains(query)
            }
        }
    }

    // Función para formatear la fecha a una forma más legible
    fun formatDate(isoDate: String): String {
        try {
            val date = Date(isoDate)
            // Formatear la fecha usando funciones nativas de JS
            return "${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}"
        } catch (e: Exception) {
            return isoDate // Si hay error, devolver la fecha original
        }
    }

    AdminPageLayout {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(left = if (breakpoint > Breakpoint.MD) SIDE_PANEL_WIDTH.px else 0.px)
                .padding(top = 20.px),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (breakpoint >= Breakpoint.MD) 80.percent else 95.percent)
                    .maxWidth(1200.px),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                SpanText(
                    text = "Historial del Sistema",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(24.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb)
                        .margin(bottom = 20.px)
                        .textAlign(TextAlign.Center)
                )

                // Barra de búsqueda
                Row(
                    modifier = Modifier.fillMaxWidth().margin(bottom = if (isMobile) 12.px else 16.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaMagnifyingGlass(
                        modifier = Modifier
                            .color(Theme.Secondary.rgb)
                            .margin(right = 8.px)
                    )

                    TextInput(
                        text = searchText,
                        onTextChange = { searchText = it },
                        placeholder = if (isMobile) "Buscar..." else "Buscar por usuario, acción, fecha o detalles",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isMobile) 36.px else 40.px)
                            .padding(right = 8.px)
                            .fontFamily(FONT_FAMILY)
                            .fontSize(if (isMobile) 14.px else 16.px)
                    )
                }

                // Mostrar error si existe
                if (error.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isMobile) 8.px else 16.px)
                            .backgroundColor(Colors.LightPink)
                            .borderRadius(4.px),
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            error,
                            modifier = Modifier
                                .color(Colors.Red)
                                .fontSize(if (isMobile) 14.px else 16.px)
                        )
                    }
                }

                // Estado de carga
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.px),
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            "Cargando historial...",
                            modifier = Modifier.fontSize(if (isMobile) 14.px else 16.px)
                        )
                    }
                } else if (filteredEntries.isEmpty()) {
                    // Mostrar mensaje si no hay resultados
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.px),
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            "No se encontraron registros de historial",
                            modifier = Modifier.fontSize(if (isMobile) 14.px else 16.px)
                        )
                    }
                } else {
                    if (isMobile) {
                        // Vista de tarjetas para móvil
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.px)
                        ) {
                            filteredEntries.forEach { entry ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .margin(bottom = 8.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .borderRadius(4.px)
                                        .padding(8.px)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Fila de usuario
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .margin(bottom = 4.px)
                                        ) {
                                            SpanText(
                                                "Usuario: ",
                                                modifier = Modifier
                                                    .fontWeight(FontWeight.Bold)
                                                    .fontSize(14.px)
                                            )
                                            SpanText(
                                                entry.userId,
                                                modifier = Modifier.fontSize(14.px)
                                            )
                                        }

                                        // Fila de acción
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .margin(bottom = 4.px)
                                        ) {
                                            SpanText(
                                                "Acción: ",
                                                modifier = Modifier
                                                    .fontWeight(FontWeight.Bold)
                                                    .fontSize(14.px)
                                            )
                                            SpanText(
                                                entry.action,
                                                modifier = Modifier.fontSize(14.px)
                                            )
                                        }

                                        // Fila de fecha
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .margin(bottom = 4.px)
                                        ) {
                                            SpanText(
                                                "Fecha: ",
                                                modifier = Modifier
                                                    .fontWeight(FontWeight.Bold)
                                                    .fontSize(14.px)
                                            )
                                            SpanText(
                                                formatDate(entry.timestamp),
                                                modifier = Modifier.fontSize(14.px)
                                            )
                                        }

                                        // Fila de detalles
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .margin(bottom = 8.px)
                                        ) {
                                            SpanText(
                                                "Detalles: ",
                                                modifier = Modifier
                                                    .fontWeight(FontWeight.Bold)
                                                    .fontSize(14.px)
                                            )
                                            SpanText(
                                                entry.details,
                                                modifier = Modifier.fontSize(14.px)
                                            )
                                        }

                                        // Botón de eliminar
                                        Button(
                                            onClick = {
                                                entryToDelete = entry
                                                showDeleteConfirmation = true
                                            },
                                            modifier = Modifier
                                                .borderRadius(4.px)
                                                .backgroundColor(Colors.Red)
                                                .color(Colors.White)
                                                .fontSize(14.px)
                                                .padding(leftRight = 12.px, topBottom = 6.px)
                                        ) {
                                            Text("Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Vista de tabla para escritorio
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .overflow(com.varabyte.kobweb.compose.css.Overflow.Auto)
                        ) {
                            Table(
                                attrs = Modifier
                                    .fillMaxWidth()
                                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                                    .borderRadius(4.px)
                                    .toAttrs()
                            ) {
                                Thead {
                                    Tr {
                                        Th(attrs = Modifier
                                            .backgroundColor(Theme.Primary.rgb)
                                            .color(Colors.White)
                                            .padding(12.px)
                                            .textAlign(TextAlign.Left)
                                            .toAttrs()
                                        ) { Text("Usuario") }

                                        Th(attrs = Modifier
                                            .backgroundColor(Theme.Primary.rgb)
                                            .color(Colors.White)
                                            .padding(12.px)
                                            .textAlign(TextAlign.Left)
                                            .toAttrs()
                                        ) { Text("Acción") }

                                        Th(attrs = Modifier
                                            .backgroundColor(Theme.Primary.rgb)
                                            .color(Colors.White)
                                            .padding(12.px)
                                            .textAlign(TextAlign.Left)
                                            .toAttrs()
                                        ) { Text("Fecha") }

                                        Th(attrs = Modifier
                                            .backgroundColor(Theme.Primary.rgb)
                                            .color(Colors.White)
                                            .padding(12.px)
                                            .textAlign(TextAlign.Left)
                                            .toAttrs()
                                        ) { Text("Detalles") }

                                        Th(attrs = Modifier
                                            .backgroundColor(Theme.Primary.rgb)
                                            .color(Colors.White)
                                            .padding(12.px)
                                            .width(100.px)
                                            .textAlign(TextAlign.Center)
                                            .toAttrs()
                                        ) { Text("Acciones") }
                                    }
                                }

                                Tbody {
                                    filteredEntries.forEach { entry ->
                                        Tr(attrs = Modifier
                                            .border(1.px, LineStyle.Solid, Colors.LightGray)
                                            .toAttrs()
                                        ) {
                                            Td(attrs = Modifier
                                                .padding(12.px)
                                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                                .toAttrs()
                                            ) { Text(entry.userId) }

                                            Td(attrs = Modifier
                                                .padding(12.px)
                                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                                .toAttrs()
                                            ) { Text(entry.action) }

                                            Td(attrs = Modifier
                                                .padding(12.px)
                                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                                .toAttrs()
                                            ) { Text(formatDate(entry.timestamp)) }

                                            Td(attrs = Modifier
                                                .padding(12.px)
                                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                                .toAttrs()
                                            ) { Text(entry.details) }

                                            Td(attrs = Modifier
                                                .padding(12.px)
                                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                                .textAlign(TextAlign.Center)
                                                .toAttrs()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        entryToDelete = entry
                                                        showDeleteConfirmation = true
                                                    },
                                                    modifier = Modifier
                                                        .borderRadius(4.px)
                                                        .backgroundColor(Colors.Red)
                                                        .color(Colors.White)
                                                ) {
                                                    Text("Eliminar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Diálogo de confirmación para eliminar
                if (showDeleteConfirmation && entryToDelete != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .backgroundColor(Colors.Black.copy(alpha = 1))
                            .position(Position.Fixed)
                            .top(0.px)
                            .left(0.px)
                            .right(0.px)
                            .bottom(0.px)
                            .zIndex(1000),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.px)
                                .backgroundColor(Colors.White)
                                .borderRadius(8.px)
                                .maxWidth(if (isMobile) 300.px else 400.px)
                                .then(if (isMobile) Modifier.width(90.percent) else Modifier.width(auto)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            SpanText(
                                "Confirmar eliminación",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(if (isMobile) 18.px else 20.px)
                                    .fontWeight(FontWeight.Bold)
                                    .padding(bottom = 8.px)
                                    .textAlign(TextAlign.Center)
                            )

                            SpanText(
                                "¿Estás seguro de que deseas eliminar este registro de historial?",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(if (isMobile) 14.px else 16.px)
                                    .padding(bottom = 16.px)
                                    .textAlign(TextAlign.Center)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        showDeleteConfirmation = false
                                        entryToDelete = null
                                    },
                                    modifier = Modifier
                                        .margin(8.px)
                                        .borderRadius(4.px)
                                        .fontSize(if (isMobile) 14.px else 16.px)
                                ) {
                                    SpanText("Cancelar")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                entryToDelete?.id?.let { id ->
                                                    val result = deleteHistory(id)
                                                    if (result) {
                                                        // Actualizar la lista
                                                        historyEntries = historyEntries.filter { it.id != id }
                                                        showDeleteConfirmation = false
                                                        entryToDelete = null
                                                        error = ""
                                                    } else {
                                                        error = "Error al eliminar el registro"
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                error = "Error: ${e.message}"
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .margin(8.px)
                                        .backgroundColor(Colors.Red)
                                        .color(Colors.White)
                                        .borderRadius(4.px)
                                        .fontSize(if (isMobile) 14.px else 16.px)
                                ) {
                                    SpanText("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}