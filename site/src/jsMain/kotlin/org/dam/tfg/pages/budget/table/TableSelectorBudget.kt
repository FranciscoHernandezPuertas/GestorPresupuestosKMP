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
import kotlinx.serialization.Serializable
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
import kotlinx.serialization.decodeFromString

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

    // Estados para el presupuesto
    var precioTotal by remember { mutableStateOf(0.0) }
    var presupuestoCalculado by remember { mutableStateOf(false) }
    var presupuestoDesglosado by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    LaunchedEffect(Unit) {
        try {
            console.log("Iniciando cálculo de presupuesto")

            // Cargar materiales desde el servidor (solo para UI)
            materials = getAllMaterials()

            // Crear objeto Mesa para enviar al servidor
            val mesaData = Mesa(
                tipo = BudgetManager.getMesaTipo(),
                tramos = tramos,
                elementosGenerales = elementosGenerales,
                cubetas = cubetas,
                modulos = modulos,
                precioTotal = 0.0, // El precio lo calculará el servidor
                fechaCreacion = null,
                error = "",
                username = localStorage.getItem("username") ?: ""
            )

            console.log("Enviando datos al servidor: ${Json.encodeToString(Mesa.serializer(), mesaData)}")

            // Llamar al servidor para calcular el presupuesto
            val result = window.api.tryPost(
                apiPath = "budget/calculate",
                body = Json.encodeToString(Mesa.serializer(), mesaData).encodeToByteArray()
            )

            if (result != null) {
                val responseStr = result.decodeToString()
                console.log("Respuesta recibida con tipo: ${responseStr::class.simpleName}")
                console.log("Respuesta completa: $responseStr")

                // Intentar decodificar con registro de errores detallado
                try {
                    val response = Json.decodeFromString<BudgetResponse>(responseStr)
                    console.log("Precio total calculado: ${response.precioTotal}")
                    console.log("Desglose: ${response.desglose}")
                    console.log("Tipo de objeto deserializado: ${response::class.simpleName}")

                    // Actualizar estados
                    precioTotal = response.precioTotal
                    presupuestoDesglosado = response.desglose
                    presupuestoCalculado = true
                } catch (e: Exception) {
                    // Si falla, intentamos leer el objeto como es (para análisis)
                    console.error("Error al deserializar: ${e.message}")
                    try {
                        // Intenta parsear manualmente para ver qué estructura tiene realmente
                        val jsonObject = JSON.parse<dynamic>(responseStr)
                        console.log("Estructura real del objeto:", jsonObject)

                        if (js("typeof jsonObject.precioTotal") != "undefined") {
                            precioTotal = jsonObject.precioTotal as Double
                            presupuestoDesglosado = (jsonObject.desglose as? Map<String, Double>) ?: emptyMap()
                            presupuestoCalculado = true
                        }
                    } catch (e2: Exception) {
                        console.error("También falló el análisis manual: ${e2.message}")
                        error = "Error al procesar la respuesta: ${e.message}"
                    }
                }
            } else {
                error = "No se pudo conectar con el servidor para calcular el presupuesto"
                console.error("Error: No se pudo conectar con el servidor")
            }

            isLoading = false
        } catch (e: Exception) {
            error = "Error al cargar datos: ${e.message}"
            console.error("Error en cálculo: ${e.message}")
            console.error(e.stackTraceToString())
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
            // Actualizar objetos con sus precios calculados
            val tramosConPrecio = tramos.mapIndexed { index, tramo ->
                tramo.copy(precio = presupuestoDesglosado["tramo_$index"] ?: tramo.precio)
            }

            val cubetasConPrecio = cubetas.mapIndexed { index, cubeta ->
                cubeta.copy(precio = presupuestoDesglosado["cubeta_$index"] ?: cubeta.precio)
            }

            val modulosConPrecio = modulos.mapIndexed { index, modulo ->
                modulo.copy(precio = presupuestoDesglosado["modulo_$index"] ?: modulo.precio)
            }

            // Guardar elementos con precios
            val elementosConPrecio = elementosGenerales.associate { elemento ->
                elemento.nombre to mapOf(
                    "cantidad" to elemento.cantidad,
                    "precio" to (presupuestoDesglosado["elemento_${elemento.nombre}"] ?: elemento.precio).toInt()
                )
            }

            // Guardar en localStorage
            BudgetManager.saveMesaTramos(tramosConPrecio)
            BudgetManager.saveCubetas(cubetasConPrecio)
            BudgetManager.saveModulos(modulosConPrecio)
            BudgetManager.saveElementosData(elementosConPrecio)

            // Guardar precio total
            BudgetManager.saveMesaPrecioTotal(precioTotal)

            // Crear el objeto Mesa para el PDF con los valores actualizados
            val mesa = Mesa(
                tipo = BudgetManager.getMesaTipo(),
                tramos = tramosConPrecio,
                elementosGenerales = elementosGenerales,
                cubetas = cubetasConPrecio,
                modulos = modulosConPrecio,
                precioTotal = precioTotal,
                fechaCreacion = null,
                error = "",
                username = localStorage.getItem("username") ?: ""
            )

            // Validar antes de finalizar (opcional) - ahora envuelto en una corrutina
            coroutineScope.launch {
                try {
                    window.api.tryPost(
                        apiPath = "budget/validate",
                        body = Json.encodeToString(Mesa.serializer(), mesa).encodeToByteArray()
                    )
                } catch (e: Exception) {
                    console.error("Error al validar presupuesto: ${e.message}")
                }
            }
        },
        continueButtonText = { "Aceptar y generar PDF" }
    )
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
    Column(modifier = Modifier.fillMaxWidth().padding(16.px)) {
        // Sección de tramos
        Section("Tramos") {
            tramos.forEachIndexed { index, tramo ->
                val precioTramo = presupuestoDesglosado["tramo_$index"] ?: tramo.precio
                DesgloseLine(
                    descripcion = "Tramo ${index + 1} (${tramo.largo}x${tramo.ancho}) - Tipo: ${tramo.tipo.displayName}",
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
                        descripcion = "${cubeta.tipo} (${cubeta.numero})",
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
        
        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.px)) {
            P(
                attrs = {
                    style {
                        property("font-style", "italic")
                        property("font-size", "14px")
                        property("color", "#555555")
                    }
                }
            ) { 
                Text("* Los precios indicados no incluyen IVA ni otros impuestos aplicables. Pueden existir costes adicionales no contemplados en este presupuesto.")
            }
        }
        
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
@Serializable
data class BudgetResponse(
    val precioTotal: Double,
    val desglose: Map<String, Double>
)

// Extensión para formatear números con dos decimales
private fun Double.toStringWithTwoDecimals(): String {
    return ((this * 100).toInt() / 100.0).toString().replace('.', ',')
}
