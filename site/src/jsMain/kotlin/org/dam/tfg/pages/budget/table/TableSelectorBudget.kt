package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.components.LoadingIndicator
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.*
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Page
@Composable
fun TableSelectorBudgetPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            TableSelectorBudget()
        }
    }
}

@Composable
fun TableSelectorBudget() {
    val coroutineScope = rememberCoroutineScope()

    var formulas by remember { mutableStateOf<List<Formula>>(emptyList()) }
    var materials by remember { mutableStateOf<List<Material>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Datos del presupuesto
    val tramos = remember { BudgetManager.getMesaTramos() }
    val cubetas = remember { BudgetManager.getCubetas() }
    val modulos = remember { BudgetManager.getModulos() }
    val elementosGenerales = remember {
        BudgetManager.getElementosData().map { (nombre, datos) ->
            ElementoSeleccionado(
                nombre = nombre,
                cantidad = datos["cantidad"] ?: 0,
                precio = (datos["precio"] ?: 0).toDouble(),
                limite = org.dam.tfg.models.ItemWithLimits(name = nombre)
            )
        }
    }

    // Calcular precio final
    var precioTotal by remember { mutableStateOf(0.0) }
    var presupuestoCalculado by remember { mutableStateOf(false) }
    var presupuestoDesglosado by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    LaunchedEffect(Unit) {
        try {
            // Cargar fórmulas y materiales desde el servidor
            val userType = localStorage.getItem("userType") ?: "user"
            formulas = getAllFormulas(userType)
            materials = getAllMaterials()

            // Calcular el presupuesto en el backend para mayor seguridad
            calcularPresupuesto(
                tramos = tramos,
                cubetas = cubetas,
                modulos = modulos,
                elementosGenerales = elementosGenerales,
                onResult = { precio, desglose ->
                    precioTotal = precio
                    presupuestoDesglosado = desglose
                    presupuestoCalculado = true
                },
                onError = { errorMsg ->
                    error = errorMsg
                }
            )

            isLoading = false
        } catch (e: Exception) {
            error = "Error al cargar datos: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.px),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        H2 { Text("Presupuesto Final") }

        if (isLoading) {
            LoadingIndicator()
        } else if (error != null) {
            P { Text("Error: $error") }
        } else if (presupuestoCalculado) {
            // Mostrar desglose del presupuesto
            PresupuestoDesglosado(
                tramos = tramos,
                cubetas = cubetas,
                modulos = modulos,
                elementosGenerales = elementosGenerales,
                presupuestoDesglosado = presupuestoDesglosado,
                precioTotal = precioTotal
            )
        }
    }

    BudgetFooter(
        previousScreen = Screen.TableSelectorResume,
        nextScreen = Screen.PdfGenerator,
        validateData = {
            presupuestoCalculado && error == null
        },
        saveData = {
            // Guardar datos del presupuesto final
            BudgetManager.saveMesaPrecioTotal(precioTotal)

            // Crear el objeto Mesa completo para guardar
            val mesa = Mesa(
                tipo = BudgetManager.getMesaTipo(),
                tramos = tramos,
                elementosGenerales = elementosGenerales,
                cubetas = cubetas,
                modulos = modulos,
                precioTotal = precioTotal,
                error = ""
            )

            // Guardar para usar en la generación del PDF
            coroutineScope.launch {
                // No guardamos la mesa en localStorage, para mantener la seguridad
                // Solo guardamos los elementos necesarios para la visualización

                // Esta parte se realizaría en la página de generación del PDF
                // addMesa(mesa)
            }
        },
        continueButtonText = { "Aceptar y generar PDF" }
    )
}

// Función para calcular el presupuesto de forma segura
private suspend fun calcularPresupuesto(
    tramos: List<Tramo>,
    cubetas: List<Cubeta>,
    modulos: List<Modulo>,
    elementosGenerales: List<ElementoSeleccionado>,
    onResult: (Double, Map<String, Double>) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Cargar fórmulas necesarias
        val userType = localStorage.getItem("userType") ?: "user"
        val formulas = getAllFormulas(userType)
            .associateBy { it.name }

        // Realizar cálculos localmente para optimizar
        val (precioTotal, desglose) = BudgetManager.calcularPresupuesto(formulas)

        // Validar resultado en el backend (opcional)
        val mesaData = Mesa(
            tipo = BudgetManager.getMesaTipo(),
            tramos = tramos,
            elementosGenerales = elementosGenerales,
            cubetas = cubetas,
            modulos = modulos,
            precioTotal = precioTotal,
            error = ""
        )

        // Llamada al backend para validar los cálculos
        val result = window.api.tryPost(
            apiPath = "budget/validate",
            body = Json.encodeToString(Mesa.serializer(), mesaData).encodeToByteArray()
        )

        // Procesar respuesta o usar cálculo local si no hay respuesta
        if (result != null) {
            val responseStr = result.decodeToString()
            val response = Json.decodeFromString(BudgetResponse.serializer(), responseStr)
            onResult(response.precioTotal, response.desglose)
        } else {
            onResult(precioTotal, desglose)
        }

    } catch (e: Exception) {
        onError("Error al calcular presupuesto: ${e.message}")
    }
}

@Composable
private fun PresupuestoDesglosado(
    tramos: List<Tramo>,
    cubetas: List<Cubeta>,
    modulos: List<Modulo>,
    elementosGenerales: List<ElementoSeleccionado>,
    presupuestoDesglosado: Map<String, Double>,
    precioTotal: Double
) {
    // Implementar la visualización del presupuesto desglosado
    // Solo mostrar resultados finales, no fórmulas ni cálculos
    Column(modifier = Modifier.fillMaxWidth().padding(16.px)) {
        // Sección de tramos
        Section("Tramos") {
            tramos.forEachIndexed { index, tramo ->
                val precioTramo = presupuestoDesglosado["tramo_$index"] ?: tramo.precio
                DesgloseLine(
                    descripcion = "Tramo ${index + 1} (${tramo.largo}x${tramo.ancho})",
                    precio = precioTramo
                )
            }
        }

        // Sección de cubetas
        if (cubetas.isNotEmpty()) {
            Section("Cubetas") {
                cubetas.forEachIndexed { index, cubeta ->
                    val precioCubeta = presupuestoDesglosado["cubeta_$index"] ?: cubeta.precio
                    DesgloseLine(
                        descripcion = "Cubeta ${index + 1} (${cubeta.largo}x${cubeta.fondo}x${cubeta.alto ?: 0})",
                        precio = precioCubeta
                    )
                }
            }
        }

        // Sección de módulos
        if (modulos.isNotEmpty()) {
            Section("Módulos") {
                modulos.forEachIndexed { index, modulo ->
                    val precioModulo = presupuestoDesglosado["modulo_$index"] ?: modulo.precio
                    DesgloseLine(
                        descripcion = "${modulo.nombre} (${modulo.cantidad})",
                        precio = precioModulo
                    )
                }
            }
        }

        // Elementos generales
        if (elementosGenerales.isNotEmpty()) {
            Section("Elementos Generales") {
                elementosGenerales.forEach { elemento ->
                    val precioElemento = presupuestoDesglosado["elemento_${elemento.nombre}"] ?: elemento.precio
                    DesgloseLine(
                        descripcion = "${elemento.nombre} (${elemento.cantidad})",
                        precio = precioElemento
                    )
                }
            }
        }

        // Precio total
        Spacer()
        DesgloseLine(
            descripcion = "PRECIO TOTAL",
            precio = precioTotal,
            isTotal = true
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.px)) {
        H2 { Text(title) }
        content()
    }
}

@Composable
private fun DesgloseLine(
    descripcion: String,
    precio: Double,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(topBottom = 4.px),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        P(
            attrs = if (isTotal) {
                { style { (FontWeight.Bold) } }
            } else {
                {}
            }
        ) { Text(descripcion) }

        P(
            attrs = if (isTotal) {
                { style { (FontWeight.Bold) } }
            } else {
                {}
            }
        ) { Text(precio.toStringWithTwoDecimals() + " €") }
    }
}

// Clase para la respuesta de cálculo de presupuesto
@kotlinx.serialization.Serializable
private data class BudgetResponse(
    val precioTotal: Double,
    val desglose: Map<String, Double>
)

// Extensión para formatear números con dos decimales
private fun Double.toStringWithTwoDecimals(): String {
    return ((this * 100).toInt() / 100.0).toString().replace('.', ',')
}