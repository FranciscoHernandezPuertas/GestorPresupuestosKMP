package org.dam.tfg.pages.admin

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.BorderCollapse
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.core.Page
import org.dam.tfg.components.AdminPageLayout
import org.dam.tfg.util.isAdminCheck
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.jetbrains.compose.web.css.px
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.TextOverflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.Theme
import org.dam.tfg.models.User
import org.dam.tfg.styles.RadioButtonStyle
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.addFormula
import org.dam.tfg.util.addMaterial
import org.dam.tfg.util.addUser
import org.dam.tfg.util.deleteFormula
import org.dam.tfg.util.deleteMaterial
import org.dam.tfg.util.deleteUser
import org.dam.tfg.util.getAllFormulas
import org.dam.tfg.util.getAllMaterials
import org.dam.tfg.util.getAllUsers
import org.dam.tfg.util.getFormulaById
import org.dam.tfg.util.updateFormula
import org.dam.tfg.util.updateMaterial
import org.dam.tfg.util.updateUser
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.get

@Page
@Composable
fun AdminEditPage() {
    isAdminCheck {
        AdminEditScreenContent()
    }
}

@Composable
fun AdminEditScreenContent() {
    val breakpoint = rememberBreakpoint()
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
                    text = "Panel de Administración",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(24.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb)
                        .margin(bottom = 20.px)
                        .textAlign(TextAlign.Center)
                )

                // Contenedor principal centrado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .backgroundColor(Colors.White)
                        .borderRadius(8.px)
                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                        .boxShadow(offsetX = 0.px, offsetY = 2.px, blurRadius = 8.px, color = Colors.LightGray)
                        .padding(20.px)
                ) {
                    AdminCrudTabs()
                }
            }
        }
    }
}

@Composable
fun AdminCrudTabs() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Materiales", "Fórmulas", "Usuarios")
    val breakpoint = rememberBreakpoint()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tabs headers - Mejorado con estilo similar al resto de la aplicación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .margin(bottom = 16.px)
                .borderRadius(8.px)
                .overflow(Overflow.Hidden)
                .border(1.px, LineStyle.Solid, Theme.Primary.rgb),
            horizontalArrangement = Arrangement.Center
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .padding(12.px)
                        .backgroundColor(if (selectedTab == index) Theme.Primary.rgb else Colors.White)
                        .cursor(Cursor.Pointer)
                        .onClick { selectedTab = index }
                        .fillMaxWidth((100f / tabs.size).percent),
                    contentAlignment = Alignment.Center
                ) {
                    SpanText(
                        text = title,
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY)
                            .fontSize(16.px)
                            .fontWeight(FontWeight.Medium)
                            .color(if (selectedTab == index) Colors.White else Theme.Primary.rgb)
                            .textAlign(TextAlign.Center)
                    )
                }
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> MaterialesTab()
            1 -> FormulasTab()
            2 -> UsuariosTab()
        }
    }
}

@Composable
fun MaterialesTab() {
    var materiales by remember { mutableStateOf<List<Material>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var editingMaterial by remember { mutableStateOf<Material?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<Material?>(null) }

    var nombreInput by remember { mutableStateOf("") }
    var precioInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val breakpoint = rememberBreakpoint()

    LaunchedEffect(Unit) {
        try {
            materiales = getAllMaterials()
            loading = false
        } catch (e: Exception) {
            error = "Error al cargar materiales: ${e.message ?: "Desconocido"}"
            loading = false
        }
    }

    // Reset form cuando cambia el modo de edición
    LaunchedEffect(isAdding, editingMaterial) {
        if (isAdding) {
            nombreInput = ""
            precioInput = ""
        } else if (editingMaterial != null) {
            editingMaterial?.let { material ->
                nombreInput = material.name
                precioInput = material.price.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.px)
    ) {
        // Error message
        if (error.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.px)
                    .backgroundColor(Colors.LightPink)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .borderRadius(4.px)
                    .margin(bottom = 16.px)
            ) {
                SpanText(error, modifier = Modifier.padding(8.px).color(Colors.Red))
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            materiales = getAllMaterials()
                            loading = false
                            error = ""
                        } catch (e: Exception) {
                            error = "Error al cargar materiales: ${e.message ?: "Desconocido"}"
                        }
                    }
                },
                modifier = Modifier
                    .margin(bottom = 16.px)
                    .borderRadius(4.px)
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
            ) {
                SpanText("Reintentar")
            }
        }

        // Add button
        if (!isAdding && editingMaterial == null) {
            Button(
                onClick = { isAdding = true },
                modifier = Modifier
                    .margin(bottom = 16.px)
                    .borderRadius(4.px)
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
            ) {
                SpanText("Añadir Material")
            }
        }

        // Form
        if (isAdding || editingMaterial != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                    .borderRadius(8.px)
                    .margin(bottom = 16.px)
                    .keyboardActions(
                        onEnter = {
                            scope.launch {
                                try {
                                    val precio = precioInput.toDoubleOrNull() ?: 0.0

                                    if (nombreInput.isBlank()) {
                                        error = "El nombre no puede estar vacío"
                                        return@launch
                                    }

                                    val material = if (editingMaterial != null) {
                                        Material(
                                            id = editingMaterial!!.id,
                                            name = nombreInput,
                                            price = precio
                                        )
                                    } else {
                                        Material(
                                            id = "",  // Se generará en el servidor
                                            name = nombreInput,
                                            price = precio
                                        )
                                    }

                                    if (editingMaterial != null) {
                                        updateMaterial(material)
                                    } else {
                                        addMaterial(material)
                                    }
                                    materiales = getAllMaterials()
                                    isAdding = false
                                    editingMaterial = null
                                    error = ""
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                }
                            }
                        },
                        onEscape = {
                            isAdding = false
                            editingMaterial = null
                        }
                    )
            ) {
                SpanText(
                    text = if (isAdding) "Añadir Material" else "Editar Material",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(18.px)
                        .fontWeight(FontWeight.Medium)
                        .margin(bottom = 16.px)
                        .color(Theme.Secondary.rgb)
                )

                TextInput(
                    text = nombreInput,
                    onTextChange = { nombreInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 16.px)
                        .height(40.px)
                        .padding(leftRight = 10.px),
                    placeholder = "Nombre del material"
                )

                TextInput(
                    text = precioInput,
                    onTextChange = { precioInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 16.px)
                        .height(40.px)
                        .padding(leftRight = 10.px),
                    placeholder = "Precio (€/mm)",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            isAdding = false
                            editingMaterial = null
                        },
                        modifier = Modifier
                            .margin(right = 8.px)
                            .borderRadius(4.px)
                            .backgroundColor(Colors.Red)
                            .color(Colors.White)
                    ) {
                        SpanText("Cancelar")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val precio = precioInput.toDoubleOrNull() ?: 0.0

                                    if (nombreInput.isBlank()) {
                                        error = "El nombre no puede estar vacío"
                                        return@launch
                                    }

                                    val material = if (editingMaterial != null) {
                                        Material(
                                            id = editingMaterial!!.id,
                                            name = nombreInput,
                                            price = precio
                                        )
                                    } else {
                                        Material(
                                            id = "",  // Se generará en el servidor
                                            name = nombreInput,
                                            price = precio
                                        )
                                    }

                                    if (editingMaterial != null) {
                                        updateMaterial(material)
                                    } else {
                                        addMaterial(material)
                                    }
                                    materiales = getAllMaterials()
                                    isAdding = false
                                    editingMaterial = null
                                    error = ""
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                }
                            }
                        },
                        modifier = Modifier
                            .borderRadius(4.px)
                            .backgroundColor(Colors.Green)
                            .color(Colors.White)
                    ) {
                        SpanText("Guardar")
                    }
                }
            }
        }

        // Delete confirmation
        if (showConfirmDelete && materialToDelete != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .borderRadius(8.px)
                    .backgroundColor(Colors.LightPink)
                    .margin(bottom = 16.px)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.px)) {
                    SpanText(
                        "¿Está seguro de eliminar el material '${materialToDelete?.name}'?",
                        modifier = Modifier.margin(bottom = 16.px)
                    )

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                showConfirmDelete = false
                                materialToDelete = null
                            },
                            modifier = Modifier
                                .margin(right = 8.px)
                                .borderRadius(4.px)
                                .backgroundColor(Theme.Secondary.rgb)
                                .color(Colors.White)
                        ) {
                            SpanText("Cancelar")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        materialToDelete?.id?.let { id ->
                                            deleteMaterial(id)
                                            materiales = getAllMaterials()
                                        }
                                        showConfirmDelete = false
                                        materialToDelete = null
                                        error = ""
                                    } catch (e: Exception) {
                                        error = "Error al eliminar: ${e.message ?: "Desconocido"}"
                                        showConfirmDelete = false
                                        materialToDelete = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .borderRadius(4.px)
                                .backgroundColor(Colors.Red)
                                .color(Colors.White)
                        ) {
                            SpanText("Eliminar")
                        }
                    }
                }
            }
        }

        // Loading or table
        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.px), contentAlignment = Alignment.Center) {
                SpanText("Cargando...")
            }
        } else if (materiales.isEmpty() && !isAdding) {
            Box(modifier = Modifier.fillMaxWidth().height(100.px), contentAlignment = Alignment.Center) {
                SpanText("No hay materiales disponibles")
            }
        } else {
            if (breakpoint < Breakpoint.MD) {
                // Vista móvil: tarjetas en lugar de tabla
                materiales.forEach { material ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .margin(bottom = 16.px)
                            .padding(16.px)
                            .border(1.px, LineStyle.Solid, Colors.LightGray)
                            .borderRadius(8.px)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SpanText(
                                "Nombre: ${material.name}",
                                modifier = Modifier
                                    .fontWeight(FontWeight.Bold)
                                    .fontSize(16.px)
                                    .margin(bottom = 8.px)
                            )
                            SpanText(
                                "Precio: ${material.price}€",
                                modifier = Modifier.margin(bottom = 8.px)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().margin(top = 8.px),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { editingMaterial = material },
                                    modifier = Modifier
                                        .margin(right = 8.px)
                                        .borderRadius(4.px)
                                        .backgroundColor(Theme.Secondary.rgb)
                                        .color(Colors.White)
                                ) {
                                    SpanText("Editar")
                                }

                                Button(
                                    onClick = {
                                        materialToDelete = material
                                        showConfirmDelete = true
                                    },
                                    modifier = Modifier
                                        .borderRadius(4.px)
                                        .backgroundColor(Colors.Red)
                                        .color(Colors.White)
                                ) {
                                    SpanText("Eliminar")
                                }
                            }
                        }
                    }
                }
            } else {
                // Vista desktop: tabla
                Table(
                    attrs = Modifier
                        .fillMaxWidth()
                        .borderCollapse(BorderCollapse.Collapse)
                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                        .borderRadius(8.px)
                        .toAttrs()
                ) {
                    Thead {
                        Tr {
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) {
                                Text("Nombre")
                            }
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) {
                                Text("Precio (€/m)")
                            }
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Center)
                                    .width(200.px)
                                    .toAttrs()
                            ) {
                                Text("Acciones")
                            }
                        }
                    }
                    Tbody {
                        materiales.forEach { material ->
                            Tr(
                                attrs = Modifier
                                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                                    .toAttrs()
                            ) {
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .toAttrs()
                                ) {
                                    Text(material.name)
                                }
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .toAttrs()
                                ) {
                                    Text(material.price.toString())
                                }
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .textAlign(TextAlign.Center)
                                        .toAttrs()
                                ) {
                                    Button(
                                        onClick = { editingMaterial = material },
                                        modifier = Modifier
                                            .margin(right = 8.px)
                                            .borderRadius(4.px)
                                            .backgroundColor(Theme.Secondary.rgb)
                                            .color(Colors.White)
                                    ) {
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = {
                                            materialToDelete = material
                                            showConfirmDelete = true
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
}
@Composable
fun FormulasTab() {
    var formulas by remember { mutableStateOf<List<Formula>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var editingFormula by remember { mutableStateOf<Formula?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var formulaToDelete by remember { mutableStateOf<Formula?>(null) }

    var nombreInput by remember { mutableStateOf("") }
    var descripcionInput by remember { mutableStateOf("") }
    var formulaInput by remember { mutableStateOf("") }
    var aplicaAInput by remember { mutableStateOf("") }
    var variablesInput by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var nuevaVariableNombre by remember { mutableStateOf("") }
    var nuevaVariableDescripcion by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val breakpoint = rememberBreakpoint()
    val userType = remember { localStorage["userType"] ?: "user" }

    LaunchedEffect(Unit) {
        try {
            formulas = getAllFormulas(userType)
            loading = false
        } catch (e: Exception) {
            error = "Error al cargar fórmulas: ${e.message ?: "Desconocido"}"
            loading = false
        }
    }

    // Reset form cuando cambia el modo de edición
    LaunchedEffect(key1 = editingFormula?.id) {  // Cambia la dependencia a solo el ID
        try {
            if (editingFormula != null) {
                val formulaDetail = getFormulaById(editingFormula!!.id, userType)
                formulaDetail?.let {
                    nombreInput = it.name
                    formulaInput = it.formula
                    aplicaAInput = it.aplicaA
                    variablesInput = it.variables.entries.map { entry ->
                        entry.key to entry.value
                    }
                }
            }
        } catch (e: Exception) {
            error = e.message ?: "Error desconocido"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.px)
    ) {
        // Mensaje de error
        if (error.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.px)
                    .backgroundColor(Colors.LightPink)
                    .borderRadius(4.px)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .margin(bottom = 16.px)
            ) {
                SpanText(error, modifier = Modifier.padding(8.px).color(Colors.Red))
            }

            Button(
                onClick = {
                    error = ""
                    loading = true
                    scope.launch {
                        try {
                            formulas = getAllFormulas(userType)
                            loading = false
                        } catch (e: Exception) {
                            error = "Error al cargar fórmulas: ${e.message ?: "Desconocido"}"
                            loading = false
                        }
                    }
                },
                modifier = Modifier.margin(bottom = 16.px).borderRadius(4.px).backgroundColor(Theme.Primary.rgb).color(Colors.White)
            ) {
                SpanText("Reintentar")
            }
        }

        // Botón añadir
        if (!isAdding && editingFormula == null) {
            Button(
                onClick = { isAdding = true },
                modifier = Modifier.margin(bottom = 16.px).borderRadius(4.px).backgroundColor(Theme.Primary.rgb).color(Colors.White)
            ) {
                SpanText("Añadir Fórmula")
            }
        }

        // Formulario
        if (isAdding || editingFormula != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                    .borderRadius(8.px)
                    .margin(bottom = 16.px)
                    .keyboardActions(
                        onEnter = {
                            // Primero validamos los campos
                            if (nombreInput.isBlank() || formulaInput.isBlank() || aplicaAInput.isBlank()) {
                                error = "Todos los campos son obligatorios"
                                return@keyboardActions
                            }

                            val formula = Formula(
                                id = editingFormula?.id ?: "",  // Asegura que el ID se conserva
                                name = nombreInput.trim(),
                                formula = formulaInput.trim(),
                                formulaEncrypted = false,  // Indica al servidor que debe encriptar
                                aplicaA = aplicaAInput.trim(),
                                variables = variablesInput.associate { it.first to it.second }
                            )

                            scope.launch {
                                try {
                                    // Mostrar los datos para depuración
                                    console.log("Enviando fórmula:", JSON.stringify(formula))

                                    if (editingFormula != null) {
                                        val success = updateFormula(formula)
                                        if (success) {
                                            // Actualiza la lista solo si fue exitoso
                                            formulas = getAllFormulas(userType)
                                            editingFormula = null
                                        } else {
                                            error = "No se pudo actualizar la fórmula"
                                        }
                                    } else {
                                        val newFormula = addFormula(formula)
                                        if (newFormula != null) {
                                            formulas = getAllFormulas(userType)
                                            isAdding = false
                                        } else {
                                            error = "No se pudo añadir la fórmula"
                                        }
                                    }
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                    console.error("Error al guardar:", e)
                                }
                            }
                        },
                        onEscape = {
                            if (isAdding) {
                                isAdding = false
                            } else {
                                editingFormula = null
                            }
                        }
                    )
            ) {
                SpanText(
                    modifier = Modifier
                        .fontSize(18.px)
                        .fontWeight(FontWeight.Bold)
                        .fontFamily(FONT_FAMILY)
                        .margin(bottom = 16.px),
                    text = if (isAdding) "Añadir Fórmula" else "Editar Fórmula"
                )

                TextInput(
                    text = nombreInput,
                    onTextChange = { nombreInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 8.px),
                    placeholder = "Nombre"
                )

                TextInput(
                    text = descripcionInput,
                    onTextChange = { descripcionInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 8.px),
                    placeholder = "Descripción"
                )

                TextInput(
                    text = formulaInput,
                    onTextChange = { formulaInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 8.px),
                    placeholder = "Fórmula"
                )

                TextInput(
                    text = aplicaAInput,
                    onTextChange = { aplicaAInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 16.px),
                    placeholder = "Aplica a"
                )

                // Variables existentes
                if (variablesInput.isNotEmpty()) {
                    SpanText(
                        text = "Variables:",
                        modifier = Modifier
                            .margin(bottom = 8.px)
                            .fontWeight(FontWeight.Bold)
                    )

                    variablesInput.forEachIndexed { index, (nombre, descripcion) ->
                        Row(
                            modifier = Modifier.margin(bottom = 8.px).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpanText(
                                text = "$nombre: $descripcion",
                                modifier = Modifier.fillMaxWidth(80.percent)
                            )

                            Button(
                                onClick = {
                                    variablesInput = variablesInput.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier
                                    .margin(left = 8.px)
                                    .borderRadius(4.px)
                                    .backgroundColor(Colors.Red)
                                    .color(Colors.White)
                            ) {
                                SpanText("Eliminar")
                            }
                        }
                    }
                }

                // Añadir nueva variable
                SpanText(
                    text = "Añadir Variable",
                    modifier = Modifier
                        .margin(top = 16.px, bottom = 8.px)
                        .fontWeight(FontWeight.Medium)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().margin(bottom = 8.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextInput(
                        text = nuevaVariableNombre,
                        onTextChange = { nuevaVariableNombre = it },
                        modifier = Modifier.margin(right = 8.px).width(40.percent),
                        placeholder = "Nombre"
                    )

                    TextInput(
                        text = nuevaVariableDescripcion,
                        onTextChange = { nuevaVariableDescripcion = it },
                        modifier = Modifier.margin(right = 8.px).width(40.percent),
                        placeholder = "Descripción"
                    )

                    Button(
                        modifier = Modifier.borderRadius(4.px)
                            .backgroundColor(Theme.Primary.rgb).color(Colors.White),
                        onClick = {
                            if (nuevaVariableNombre.isNotEmpty()) {
                                variablesInput = variablesInput + (nuevaVariableNombre to nuevaVariableDescripcion)
                                nuevaVariableNombre = ""
                                nuevaVariableDescripcion = ""
                            }
                        }
                    ) {
                        SpanText("Añadir")
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth().margin(top = 16.px),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (isAdding) {
                                isAdding = false
                            } else {
                                editingFormula = null
                            }
                        },
                        modifier = Modifier.margin(right = 8.px).borderRadius(4.px).backgroundColor(Colors.Red).color(Colors.White)
                    ) {
                        SpanText("Cancelar")
                    }

                    Button(
                        modifier = Modifier.borderRadius(4.px).backgroundColor(Colors.Green).color(Colors.White),
                        onClick = onClick@{
                            // Primero validamos los campos
                            if (nombreInput.isBlank() || formulaInput.isBlank() || aplicaAInput.isBlank()) {
                                error = "Todos los campos son obligatorios"
                                return@onClick
                            }

                            val formula = Formula(
                                id = editingFormula?.id ?: "",  // Asegura que el ID se conserva
                                name = nombreInput.trim(),
                                formula = formulaInput.trim(),
                                formulaEncrypted = false,  // Indica al servidor que debe encriptar
                                aplicaA = aplicaAInput.trim(),
                                variables = variablesInput.associate { it.first to it.second }
                            )

                            scope.launch {
                                try {
                                    // Mostrar los datos para depuración
                                    console.log("Enviando fórmula:", JSON.stringify(formula))

                                    if (editingFormula != null) {
                                        val success = updateFormula(formula)
                                        if (success) {
                                            // Actualiza la lista solo si fue exitoso
                                            formulas = getAllFormulas(userType)
                                            editingFormula = null
                                        } else {
                                            error = "No se pudo actualizar la fórmula"
                                        }
                                    } else {
                                        val newFormula = addFormula(formula)
                                        if (newFormula != null) {
                                            formulas = getAllFormulas(userType)
                                            isAdding = false
                                        } else {
                                            error = "No se pudo añadir la fórmula"
                                        }
                                    }
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                    console.error("Error al guardar:", e)
                                }
                            }
                        }
                    ) {
                        SpanText("Guardar")
                    }
                }
            }
        }

        // Confirmación de eliminación
        if (showConfirmDelete && formulaToDelete != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .borderRadius(8.px)
                    .backgroundColor(Colors.LightPink)
                    .margin(bottom = 16.px)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.px)
                ) {
                    SpanText(
                        "¿Está seguro de eliminar la fórmula '${formulaToDelete?.name}'?",
                        modifier = Modifier.margin(bottom = 16.px)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                showConfirmDelete = false
                                formulaToDelete = null
                            },
                            modifier = Modifier
                                .margin(right = 8.px)
                                .borderRadius(4.px)
                                .backgroundColor(Theme.Secondary.rgb)
                                .color(Colors.White)
                        ) {
                            SpanText("Cancelar")
                        }

                        Button(
                            modifier = Modifier
                                .margin(right = 8.px)
                                .borderRadius(4.px)
                                .backgroundColor(Colors.Red)
                                .color(Colors.White),
                            onClick = {
                                formulaToDelete?.let { formula ->
                                    scope.launch {
                                        try {
                                            deleteFormula(formula.id)
                                            formulas = formulas.filter { it.id != formula.id }
                                            showConfirmDelete = false
                                            formulaToDelete = null
                                        } catch (e: Exception) {
                                            error = "Error al eliminar: ${e.message ?: "Desconocido"}"
                                            showConfirmDelete = false
                                            formulaToDelete = null
                                        }
                                    }
                                }
                            }
                        ) {
                            SpanText("Eliminar")
                        }
                    }
                }
            }
        }

        // Cargando o mostrar fórmulas
        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.px), contentAlignment = Alignment.Center) {
                SpanText("Cargando fórmulas...")
            }
        } else if (formulas.isEmpty() && !isAdding && error.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.px), contentAlignment = Alignment.Center) {
                SpanText("No hay fórmulas disponibles")
            }
        } else {
            if (breakpoint < Breakpoint.MD) {
                // Vista móvil: tarjetas
                Column(modifier = Modifier.fillMaxWidth()) {
                    formulas.forEach { formula ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .margin(bottom = 16.px)
                                .padding(16.px)
                                .backgroundColor(Colors.White)
                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                .borderRadius(4.px)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SpanText(
                                    formula.name,
                                    modifier = Modifier
                                        .fontWeight(FontWeight.Bold)
                                        .fontSize(16.px)
                                        .margin(bottom = 8.px)
                                )


                                SpanText(
                                    "Fórmula: ${formula.formula}",
                                    modifier = Modifier.margin(bottom = 4.px)
                                )

                                SpanText(
                                    "Aplica a: ${formula.aplicaA}",
                                    modifier = Modifier.margin(bottom = 8.px)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { editingFormula = formula },
                                        modifier = Modifier
                                            .margin(right = 8.px)
                                            .borderRadius(4.px)
                                            .backgroundColor(Theme.Secondary.rgb)
                                            .color(Colors.White)
                                    )
                                    {
                                        SpanText("Editar")
                                    }

                                    Button(
                                        onClick = {
                                            formulaToDelete = formula
                                            showConfirmDelete = true
                                        },
                                        modifier = Modifier
                                            .borderRadius(4.px)
                                            .backgroundColor(Colors.Red)
                                            .color(Colors.White)
                                    ) {
                                        SpanText("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Vista desktop: tabla
                Table(
                    attrs = Modifier
                        .fillMaxWidth()
                        .borderCollapse(BorderCollapse.Collapse)
                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                        .borderRadius(8.px)
                        .toAttrs()
                ) {
                    Thead {
                        Tr {
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) { Text("Nombre") }
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) { Text("Fórmula") }
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) { Text("Aplica a") }
                            Th(
                                attrs = Modifier
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .padding(12.px)
                                    .textAlign(TextAlign.Center)
                                    .width(150.px)
                                    .toAttrs()
                            ) { Text("Acciones") }
                        }
                    }
                    Tbody {
                        formulas.forEach { formula ->
                            Tr(
                                attrs = Modifier
                                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                                    .toAttrs()
                            ) {
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .maxWidth(150.px)
                                        .overflow(Overflow.Hidden)
                                        .textOverflow(TextOverflow.Ellipsis)
                                        .whiteSpace(WhiteSpace.NoWrap)
                                        .toAttrs()
                                ) { Text(formula.name) }

                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .maxWidth(200.px)
                                        .overflow(Overflow.Hidden)
                                        .textOverflow(TextOverflow.Ellipsis)
                                        .whiteSpace(WhiteSpace.NoWrap)
                                        .toAttrs()
                                ) { Text(formula.formula) }

                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .maxWidth(150.px)
                                        .overflow(Overflow.Hidden)
                                        .textOverflow(TextOverflow.Ellipsis)
                                        .whiteSpace(WhiteSpace.NoWrap)
                                        .toAttrs()
                                ) { Text(formula.aplicaA) }

                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .textAlign(TextAlign.Center)
                                        .toAttrs()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { editingFormula = formula },
                                            modifier = Modifier
                                                .borderRadius(4.px)
                                                .backgroundColor(Theme.Secondary.rgb)
                                                .color(Colors.White)
                                                .margin(right = 8.px)
                                        ) {
                                            Text("Editar")
                                        }

                                        Button(
                                            onClick = {
                                                formulaToDelete = formula
                                                showConfirmDelete = true
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
    }
}

@Composable
fun UsuariosTab() {
    var usuarios by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var isAdding by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("user") }

    val scope = rememberCoroutineScope()
    val breakpoint = rememberBreakpoint()

    LaunchedEffect(Unit) {
        try {
            usuarios = getAllUsers()
            loading = false
        } catch (e: Exception) {
            error = "Error al cargar usuarios: ${e.message ?: "Desconocido"}"
            loading = false
        }
    }

    // Reset form cuando cambia el modo de edición
    LaunchedEffect(isAdding, editingUser) {
        if (isAdding) {
            usernameInput = ""
            passwordInput = ""
            typeInput = "user"
        } else if (editingUser != null) {
            editingUser?.let { user ->
                usernameInput = user.username
                passwordInput = ""
                typeInput = user.type
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.px)
    ) {
        // Mensaje de error
        if (error.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.px)
                    .backgroundColor(Colors.LightPink)
                    .borderRadius(4.px)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .margin(bottom = 16.px)
            ) {
                SpanText(
                    text = error,
                    modifier = Modifier.padding(8.px).color(Colors.Red)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            usuarios = getAllUsers()
                            loading = false
                            error = ""
                        } catch (e: Exception) {
                            error = "Error al cargar usuarios: ${e.message ?: "Desconocido"}"
                        }
                    }
                },
                modifier = Modifier
                    .margin(bottom = 16.px)
                    .borderRadius(4.px)
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
            ) {
                SpanText("Reintentar")
            }
        }

        // Botón añadir
        if (!isAdding && editingUser == null) {
            Button(
                onClick = {
                    isAdding = true
                },
                modifier = Modifier
                    .margin(bottom = 16.px)
                    .borderRadius(4.px)
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
            ) {
                SpanText("Añadir Usuario")
            }
        }

        // Formulario
        if (isAdding || editingUser != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                    .borderRadius(8.px)
                    .margin(bottom = 16.px)
                    .keyboardActions(
                        onEnter = {
                            scope.launch {
                                try {
                                    // Validaciones
                                    if (usernameInput.isBlank()) {
                                        error = "El nombre de usuario no puede estar vacío"
                                        return@launch
                                    }

                                    if (isAdding && passwordInput.isBlank()) {
                                        error = "La contraseña no puede estar vacía"
                                        return@launch
                                    }

                                    val user = if (editingUser != null) {
                                        // Si estamos editando, solo actualizamos la contraseña si se ha proporcionado una
                                        if (passwordInput.isBlank()) {
                                            editingUser!!.copy(
                                                username = usernameInput,
                                                type = typeInput
                                            )
                                        } else {
                                            editingUser!!.copy(
                                                username = usernameInput,
                                                password = passwordInput,
                                                type = typeInput
                                            )
                                        }
                                    } else {
                                        User(
                                            id = "",  // Se generará en el servidor
                                            username = usernameInput,
                                            password = passwordInput,
                                            type = typeInput
                                        )
                                    }

                                    if (editingUser != null) {
                                        updateUser(user)
                                    } else {
                                        addUser(user)
                                    }
                                    usuarios = getAllUsers()
                                    isAdding = false
                                    editingUser = null
                                    error = ""
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                }
                            }
                        },
                        onEscape = {
                            isAdding = false
                            editingUser = null
                        }
                    )
            ) {
                SpanText(
                    if (isAdding) "Añadir Usuario" else "Editar Usuario",
                    modifier = Modifier
                        .fontSize(18.px)
                        .fontWeight(FontWeight.Bold)
                        .fontFamily(FONT_FAMILY)
                        .margin(bottom = 16.px)
                        .color(Theme.Secondary.rgb)
                )

                TextInput(
                    text = usernameInput,
                    onTextChange = { usernameInput = it },
                    placeholder = "Nombre de usuario",
                    modifier = Modifier
                        .margin(bottom = 8.px)
                        .fillMaxWidth()
                        .height(40.px)
                        .padding(leftRight = 10.px)
                )

                TextInput(
                    text = passwordInput,
                    onTextChange = { passwordInput = it },
                    placeholder = if (editingUser != null) "Nueva contraseña (dejar en blanco para no cambiar)" else "Contraseña",
                    modifier = Modifier
                        .margin(bottom = 8.px)
                        .fillMaxWidth()
                        .height(40.px)
                        .padding(leftRight = 10.px)
                        .attr("type", "password")
                )

                // Selector de tipo
                // Reemplaza la sección de radio buttons en UsuariosTab() por este código:
                SpanText(
                    "Tipo:",
                    modifier = Modifier
                        .margin(bottom = 4.px)
                        .fontFamily(FONT_FAMILY)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 16.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radio button para usuario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.margin(right = 20.px)
                    ) {
                        Box(
                            modifier = Modifier.width(20.px).height(20.px)
                        ) {
                            // Input radio real pero escondido
                            Input(
                                type = InputType.Radio,
                                attrs = Modifier
                                    .id("radio-user")
                                    .toAttrs {
                                        name("userType")
                                        checked(typeInput == "user")
                                        onChange { typeInput = "user" }
                                        style {
                                            property("opacity", "0")
                                            property("position", "absolute")
                                        }
                                    }
                            )

                            // Círculo personalizado visible
                            Box(
                                modifier = RadioButtonStyle.toModifier()
                                    .width(20.px)
                                    .height(20.px)
                                    .border(
                                        width = 2.px,
                                        style = LineStyle.Solid,
                                        color = if (typeInput == "user") Theme.Primary.rgb else Theme.HalfBlack.rgb
                                    )
                                    .borderRadius(50.percent)
                                    .backgroundColor(Colors.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (typeInput == "user") {
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
                                .cursor(Cursor.Pointer)
                                .toAttrs {
                                    attr("for", "radio-user")
                                }
                        ) {
                            Text("Usuario")
                        }
                    }

                    // Radio button para administrador
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(20.px).height(20.px)
                        ) {
                            // Input radio real pero escondido
                            Input(
                                type = InputType.Radio,
                                attrs = Modifier
                                    .id("radio-admin")
                                    .toAttrs {
                                        name("userType")
                                        checked(typeInput == "admin")
                                        onChange { typeInput = "admin" }
                                        style {
                                            property("opacity", "0")
                                            property("position", "absolute")
                                        }
                                    }
                            )

                            // Círculo personalizado visible
                            Box(
                                modifier = RadioButtonStyle.toModifier()
                                    .width(20.px)
                                    .height(20.px)
                                    .border(
                                        width = 2.px,
                                        style = LineStyle.Solid,
                                        color = if (typeInput == "admin") Theme.Primary.rgb else Theme.HalfBlack.rgb
                                    )
                                    .borderRadius(50.percent)
                                    .backgroundColor(Colors.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (typeInput == "admin") {
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
                                .cursor(Cursor.Pointer)
                                .toAttrs {
                                    attr("for", "radio-admin")
                                }
                        ) {
                            Text("Administrador")
                        }
                    }
                }

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            isAdding = false
                            editingUser = null
                        },
                        modifier = Modifier
                            .margin(right = 8.px)
                            .borderRadius(4.px)
                            .backgroundColor(Colors.Red)
                            .color(Colors.White)
                    ) {
                        SpanText("Cancelar")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // Validaciones
                                    if (usernameInput.isBlank()) {
                                        error = "El nombre de usuario no puede estar vacío"
                                        return@launch
                                    }

                                    if (isAdding && passwordInput.isBlank()) {
                                        error = "La contraseña no puede estar vacía"
                                        return@launch
                                    }

                                    val user = if (editingUser != null) {
                                        // Si estamos editando, solo actualizamos la contraseña si se ha proporcionado una
                                        if (passwordInput.isBlank()) {
                                            editingUser!!.copy(
                                                username = usernameInput,
                                                type = typeInput
                                            )
                                        } else {
                                            editingUser!!.copy(
                                                username = usernameInput,
                                                password = passwordInput,
                                                type = typeInput
                                            )
                                        }
                                    } else {
                                        User(
                                            id = "",  // Se generará en el servidor
                                            username = usernameInput,
                                            password = passwordInput,
                                            type = typeInput
                                        )
                                    }

                                    if (editingUser != null) {
                                        updateUser(user)
                                    } else {
                                        addUser(user)
                                    }
                                    usuarios = getAllUsers()
                                    isAdding = false
                                    editingUser = null
                                    error = ""
                                } catch (e: Exception) {
                                    error = "Error al guardar: ${e.message ?: "Desconocido"}"
                                }
                            }
                        },
                        modifier = Modifier
                            .borderRadius(4.px)
                            .backgroundColor(Colors.Green)
                            .color(Colors.White)
                    ) {
                        SpanText("Guardar")
                    }
                }
            }
        }

        // Confirmación de eliminación
        if (showConfirmDelete && userToDelete != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.px)
                    .border(1.px, LineStyle.Solid, Colors.Red)
                    .borderRadius(8.px)
                    .backgroundColor(Colors.LightPink)
                    .margin(bottom = 16.px)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.px)
                ) {
                    SpanText(
                        "¿Está seguro de eliminar al usuario '${userToDelete?.username}'?",
                        modifier = Modifier.margin(bottom = 16.px)
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                showConfirmDelete = false
                                userToDelete = null
                            },
                            modifier = Modifier
                                .margin(right = 8.px)
                                .borderRadius(4.px)
                                .backgroundColor(Theme.Secondary.rgb)
                                .color(Colors.White)
                        ) {
                            SpanText("Cancelar")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        userToDelete?.id?.let { id ->
                                            deleteUser(id)
                                            usuarios = getAllUsers()
                                            showConfirmDelete = false
                                            userToDelete = null
                                            error = ""
                                        }
                                    } catch (e: Exception) {
                                        error = "Error al eliminar: ${e.message ?: "Desconocido"}"
                                        showConfirmDelete = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .borderRadius(4.px)
                                .backgroundColor(Colors.Red)
                                .color(Colors.White)
                        ) {
                            SpanText("Eliminar")
                        }
                    }
                }
            }
        }

        // Cargando o mostrar usuarios
        if (loading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(100.px)
            ) {
                SpanText("Cargando usuarios...")
            }
        } else if (usuarios.isEmpty() && !isAdding && error.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(100.px)
            ) {
                SpanText("No hay usuarios disponibles")
            }
        } else {
            if (breakpoint < Breakpoint.MD) {
                // Vista móvil: tarjetas
                Column(modifier = Modifier.fillMaxWidth()) {
                    usuarios.forEach { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .margin(bottom = 16.px)
                                .padding(16.px)
                                .backgroundColor(Colors.White)
                                .border(1.px, LineStyle.Solid, Colors.LightGray)
                                .borderRadius(8.px)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SpanText(
                                    user.username,
                                    modifier = Modifier
                                        .fontWeight(FontWeight.Bold)
                                        .fontSize(16.px)
                                        .margin(bottom = 8.px)
                                )

                                SpanText(
                                    "Tipo: ${if (user.type == "admin") "Administrador" else "Usuario"}",
                                    modifier = Modifier.margin(bottom = 8.px)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { editingUser = user },
                                        modifier = Modifier
                                            .margin(right = 8.px)
                                            .borderRadius(4.px)
                                            .backgroundColor(Theme.Secondary.rgb)
                                            .color(Colors.White)
                                    ) {
                                        SpanText("Editar")
                                    }

                                    Button(
                                        onClick = {
                                            userToDelete = user
                                            showConfirmDelete = true
                                        },
                                        modifier = Modifier
                                            .borderRadius(4.px)
                                            .backgroundColor(Colors.Red)
                                            .color(Colors.White)
                                    ) {
                                        SpanText("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tabla de usuarios
                Table(
                    attrs = Modifier
                        .fillMaxWidth()
                        .borderCollapse(BorderCollapse.Collapse)
                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                        .borderRadius(8.px)
                        .toAttrs()
                ) {
                    Thead {
                        Tr {
                            Th(
                                attrs = Modifier
                                    .padding(12.px)
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .textAlign(TextAlign.Left)
                                    .toAttrs()
                            ) {
                                Text("Usuario")
                            }
                            Th(
                                attrs = Modifier
                                    .padding(12.px)
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .textAlign(TextAlign.Center)
                                    .toAttrs()
                            ) {
                                Text("Tipo")
                            }
                            Th(
                                attrs = Modifier
                                    .padding(12.px)
                                    .backgroundColor(Theme.Primary.rgb)
                                    .color(Colors.White)
                                    .textAlign(TextAlign.Center)
                                    .width(200.px)
                                    .toAttrs()
                            ) {
                                Text("Acciones")
                            }
                        }
                    }
                    Tbody {
                        usuarios.forEach { user ->
                            Tr(
                                attrs = Modifier
                                    .border(1.px, LineStyle.Solid, Colors.LightGray)
                                    .toAttrs()
                            ) {
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .toAttrs()
                                ) {
                                    Text(user.username)
                                }
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .textAlign(TextAlign.Center)
                                        .toAttrs()
                                ) {
                                    Text(if (user.type == "admin") "Administrador" else "Usuario")
                                }
                                Td(
                                    attrs = Modifier
                                        .padding(12.px)
                                        .border(1.px, LineStyle.Solid, Colors.LightGray)
                                        .textAlign(TextAlign.Center)
                                        .toAttrs()
                                ) {
                                    Button(
                                        onClick = { editingUser = user },
                                        modifier = Modifier
                                            .margin(right = 8.px)
                                            .borderRadius(4.px)
                                            .backgroundColor(Theme.Secondary.rgb)
                                            .color(Colors.White)
                                    ) {
                                        Text("Editar")
                                    }
                                    Button(
                                        onClick = {
                                            userToDelete = user
                                            showConfirmDelete = true
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
}

// Función auxiliar para manejar eventos de teclado
private fun Modifier.keyboardActions(onEnter: () -> Unit, onEscape: () -> Unit): Modifier {
    return this.then(
        Modifier.onKeyDown { event ->
            when (event.key) {
                "Enter" -> {
                    onEnter()
                    true
                }
                "Escape" -> {
                    onEscape()
                    true
                }
                else -> false
            }
        }
    )
}