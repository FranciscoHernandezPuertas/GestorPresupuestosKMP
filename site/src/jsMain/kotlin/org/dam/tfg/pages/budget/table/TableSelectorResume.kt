package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.components.*
import org.dam.tfg.models.Theme
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.navigation.Screen
import org.dam.tfg.resources.WebResourceProvider
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba


@Page
@Composable
fun TableSelectorResumePage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            TableSelectorResumeContent()
        }
    }
}

@Composable
fun TableSelectorResumeContent() {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val resourceProvider = remember { WebResourceProvider() }

    // Recuperar datos del localStorage
    val mesaTipo = BudgetManager.getMesaTipo()
    val mesaTramos = BudgetManager.getMesaTramos()
    val elementos = BudgetManager.getElementosData()
    val cubetas = BudgetManager.getCubetas()
    val modulos = BudgetManager.getModulos()

    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 100.px),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(80.percent)
                .padding(top = 20.px),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la página
            SpanText(
                "Resumen del Presupuesto",
                modifier = Modifier
                    .margin(bottom = 20.px)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(24.px)
                    .fontWeight(FontWeight.Bold)
                    .color(Theme.Primary.rgb)
            )

            // 1. Sección de Mesa
            SectionTitle("Información de la Mesa")
            MesaResumen(mesaTipo, mesaTramos, resourceProvider)

            // 2. Sección de Elementos Adicionales
            if (elementos.isNotEmpty()) {
                SectionTitle("Elementos Adicionales")
                ElementosResumen(elementos, resourceProvider)
            }

            // 3. Sección de Cubetas
            if (cubetas.isNotEmpty()) {
                SectionTitle("Cubetas Seleccionadas")
                CubetasResumen(cubetas, resourceProvider)
            }

            // 4. Sección de Módulos
            if (modulos.isNotEmpty()) {
                SectionTitle("Módulos Seleccionados")
                ModulosResumen(modulos, resourceProvider)
            }

            // Información adicional antes de continuar
            InfoBox()
        }
    }

    // Footer con botones de navegación
    BudgetFooter(
        previousScreen = Screen.TableSelectorModules,
        nextScreen = Screen.TableSelectorBudget,
        validateData = {
            // Mostrar confirmación antes de continuar
            showConfirmDialog = true
            false // No continuar automáticamente
        },
        saveData = { /* No es necesario guardar datos aquí */ }
    )

    // Diálogo de confirmación personalizado
    if (showConfirmDialog) {
        FinalConfirmDialog(
            onConfirm = {
                showConfirmDialog = false
                // Aquí se llamaría al cálculo final del presupuesto
                // BudgetCalculator.calcularPresupuestoFinal()
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    SpanText(
        text = title,
        modifier = Modifier
            .margin(topBottom = 15.px)
            .fontFamily(FONT_FAMILY)
            .fontSize(20.px)
            .fontWeight(FontWeight.Medium)
            .color(Theme.Secondary.rgb)
    )

    Box(
        modifier = Modifier
            .width(100.px)
            .height(3.px)
            .margin(bottom = 15.px)
            .backgroundColor(Theme.Primary.rgb)
    )
}

@Composable
private fun MesaResumen(tipoMesa: String, tramos: List<Tramo>, resourceProvider: WebResourceProvider) {
    val mesaImageKey = getMesaImageKey(tipoMesa, tramos)

    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la mesa
            Box(
                modifier = Modifier
                    .width(80.px)
                    .height(80.px)
                    .margin(right = 15.px),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    src = resourceProvider.getImagePath(mesaImageKey),
                    alt = tipoMesa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .objectFit(ObjectFit.Contain)
                )
            }

            // Información de la mesa
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.px)
            ) {
                // Tipo de mesa
                ResumeItem("Tipo de Mesa", "$tipoMesa tramos")

                // Tramos
                SpanText(
                    text = "Tramos:",
                    modifier = Modifier
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Medium)
                )

                tramos.forEach { tramo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .margin(left = 15.px)
                    ) {
                        SpanText(
                            text = "Tramo ${tramo.numero}: Largo: ${tramo.largo} mm, Ancho: ${tramo.ancho} mm, Tipo: ${tramo.tipo}",
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
private fun ElementosResumen(elementos: Map<String, Int>, resourceProvider: WebResourceProvider) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.px)
        ) {
            elementos.entries.forEach { (elemento, cantidad) ->
                if (cantidad > 0) {
                    val imageKey = getElementoImageKey(elemento)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .margin(bottom = 10.px),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen del elemento
                        Box(
                            modifier = Modifier
                                .width(50.px)
                                .height(50.px)
                                .margin(right = 15.px),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                src = resourceProvider.getImagePath(imageKey),
                                alt = elemento,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .objectFit(ObjectFit.Contain)
                            )
                        }

                        // Nombre y cantidad
                        ResumeItem(elemento, "$cantidad unidades")
                    }
                }
            }
        }
    }
}

@Composable
private fun CubetasResumen(cubetas: List<Cubeta>, resourceProvider: WebResourceProvider) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.px)
        ) {
            cubetas.forEach { cubeta ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 10.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de la cubeta
                    Box(
                        modifier = Modifier
                            .width(50.px)
                            .height(50.px)
                            .margin(right = 15.px),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            src = resourceProvider.getImagePath("CUBETA"),
                            alt = "Cubeta",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .objectFit(ObjectFit.Contain)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        SpanText(
                            text = "${cubeta.numero}x Cubeta ",
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .fontWeight(FontWeight.Medium)
                                .margin(bottom = 4.px)
                        )

                        SpanText(
                            text = "${cubeta.tipo} - Largo: ${cubeta.largo} mm, Fondo: ${cubeta.fondo} mm, Alto: ${cubeta.alto} mm",
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
private fun ModulosResumen(modulos: List<Modulo>, resourceProvider: WebResourceProvider) {
    Card {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.px)
        ) {
            modulos.forEach { modulo ->
                val imageKey = getModuloImageKey(modulo.nombre)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .margin(bottom = 10.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del módulo
                    Box(
                        modifier = Modifier
                            .width(60.px)
                            .height(60.px)
                            .margin(right = 15.px),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            src = resourceProvider.getImagePath(imageKey),
                            alt = modulo.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .objectFit(ObjectFit.Contain)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .margin(bottom = 4.px),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpanText(
                                text = "${modulo.cantidad}x ${modulo.nombre}",
                                modifier = Modifier
                                    .fontFamily(FONT_FAMILY)
                                    .fontSize(16.px)
                                    .fontWeight(FontWeight.Medium)
                            )
                        }

                        SpanText(
                            text = "Dimensiones: ${modulo.largo}x${modulo.fondo}x${modulo.alto} mm",
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
private fun ResumeItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 8.px),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpanText(
            text = "$label:",
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .fontWeight(FontWeight.Medium)
                .margin(right = 5.px)
        )
        SpanText(
            text = value,
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
        )
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(bottom = 20.px)
            .margin(bottom = 20.px)
            .border(2.px, LineStyle.Solid, Theme.LightGray.rgb)
            .borderRadius(8.px)
            .backgroundColor(Theme.White.rgb)
            .boxShadow(
                offsetX = 0.px,
                offsetY = 3.px,
                blurRadius = 8.px,
                spreadRadius = 1.px,
                color = rgba(0, 0, 0, 0.1)
            )
            // Efecto de brillo interior sutil
            .boxShadow(
                offsetX = 0.px,
                offsetY = 4.px,
                blurRadius = 8.px,
                spreadRadius = 0.px,
                color = Theme.HalfBlack.rgb
            )
    ) {
        content()
    }
}

@Composable
private fun InfoBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .margin(top = 20.px, bottom = 20.px)
            .padding(15.px)
            .backgroundColor(rgba(255, 243, 205, 1))
            .borderRadius(8.px)
            .border(2.px, LineStyle.Solid, rgba(255, 193, 7, 0.5))
            .borderRadius(8.px),
        contentAlignment = Alignment.Center
    ) {
        SpanText(
            text = "Al continuar se realizará el cálculo final del presupuesto. " +
                    "Una vez calculado, no se podrán modificar las opciones seleccionadas.",
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .textAlign(TextAlign.Center)
        )
    }
}

@Composable
fun FinalConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .position(org.jetbrains.compose.web.css.Position.Fixed)
            .top(0.px)
            .left(0.px)
            .zIndex(999)
            .backgroundColor(Theme.HalfBlack.rgb)
            .onClick { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(400.px)
                .padding(20.px)
                .backgroundColor(Theme.White.rgb)
                .borderRadius(8.px)
                .boxShadow(offsetX = 0.px, offsetY = 4.px, blurRadius = 8.px, color = Theme.Black.rgb)
                .onClick { it.stopPropagation() }
        ) {
            Column(
                modifier = Modifier.padding(20.px).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpanText(
                    modifier = Modifier
                        .margin(bottom = 20.px)
                        .fontFamily(FONT_FAMILY)
                        .fontSize(18.px)
                        .fontWeight(FontWeight.Bold)
                        .color(Theme.Secondary.rgb),
                    text = "Confirmación"
                )

                SpanText(
                    modifier = Modifier
                        .margin(bottom = 30.px)
                        .fontFamily(FONT_FAMILY)
                        .fontSize(16.px)
                        .color(Theme.Secondary.rgb)
                        .textAlign(TextAlign.Center),
                    text = "¿Está seguro que desea continuar al cálculo final? " +
                            "Una vez calculado, no podrá volver atrás para modificar las opciones seleccionadas."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Theme.Primary.rgb)
                            .borderRadius(6.px)
                            .padding(topBottom = 10.px, leftRight = 20.px)
                            .cursor(com.varabyte.kobweb.compose.css.Cursor.Pointer)
                            .onClick { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .color(Theme.White.rgb),
                            text = "Continuar"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(10.px)
                            .backgroundColor(Theme.Secondary.rgb)
                            .borderRadius(6.px)
                            .padding(topBottom = 10.px, leftRight = 20.px)
                            .cursor(com.varabyte.kobweb.compose.css.Cursor.Pointer)
                            .onClick { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            modifier = Modifier
                                .fontFamily(FONT_FAMILY)
                                .fontSize(16.px)
                                .color(Theme.White.rgb),
                            text = "Cancelar"
                        )
                    }
                }
            }
        }
    }
}

// Funciones helper para obtener las claves de las imágenes
private fun getMesaImageKey(tipoMesa: String, tramos: List<Tramo>): String {
    // Crear clave para imagen de mesa según número de tramos y tipos
    if (tramos.isEmpty()) return ""

    val key = when (tramos.size) {
        1 -> "MESA_1TRAMO_" + if (tramos[0].tipo.equals("Central")) "CENTRAL" else "MURAL"
        2 -> "MESA_2TRAMOS_" + tramos.joinToString("") { if (it.tipo.equals("Central")) "C" else "M" }
        3 -> "MESA_3TRAMOS_" + tramos.joinToString("") { if (it.tipo.equals("Central")) "C" else "M" }
        4 -> "MESA_4TRAMOS_" + tramos.joinToString("") { if (it.tipo.equals("Central")) "C" else "M" }
        else -> ""
    }

    return key
}

private fun getElementoImageKey(nombre: String): String {
    return when (nombre) {
        "Cubeta" -> "CUBETA"
        "Peto lateral" -> "PETO_LATERAL"
        "Esquina en chaflán" -> "ESQUINA_EN_CHAFLAN"
        "Esquina redondeada" -> "ESQUINA_REDONDEADA"
        "Cajeado columna" -> "CAJEADO_COLUMNA"
        "Aro de desbarace" -> "ARO_DE_DESBARACE"
        "Kit lavamanos pulsador" -> "KIT_LAVAMANOS_PULSADOR"
        "Kit lavam. pedal simple" -> "KIT_LAVAMANOS_PEDAL_SIMPLE"
        "Kit lavam. pedal doble" -> "KIT_LAVAMANOS_PEDAL_DOBLE"
        "Baquetón en seno" -> "BAQUETON_EN_SENO"
        "Baqueton perimetrico" -> "BAQUETON_PERIMETRICO"
        else -> ""
    }
}

private fun getModuloImageKey(nombre: String): String {
    return when (nombre) {
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