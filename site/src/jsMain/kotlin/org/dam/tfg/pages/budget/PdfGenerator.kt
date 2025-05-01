package org.dam.tfg.pages.budget

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.dam.tfg.components.AppHeader
import org.dam.tfg.models.History
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.*
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun PdfGeneratorPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()
            PdfGenerator()
        }
    }
}

@Composable
fun PdfGenerator() {
    val context = rememberPageContext()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Crear objeto Mesa con todos los datos guardados
            val mesa = Mesa(
                tipo = BudgetManager.getMesaTipo(),
                tramos = BudgetManager.getMesaTramos(),
                elementosGenerales = BudgetManager.getElementosData().map { (nombre, datos) ->
                    org.dam.tfg.models.table.ElementoSeleccionado(
                        nombre = nombre,
                        cantidad = datos["cantidad"] ?: 0,
                        precio = (datos["precio"] ?: 0).toDouble(),
                        limite = org.dam.tfg.models.ItemWithLimits(name = nombre)
                    )
                },
                cubetas = BudgetManager.getCubetas(),
                modulos = BudgetManager.getModulos(),
                precioTotal = BudgetManager.getMesaPrecioTotal(),
                error = ""
            )

            // Guardar la mesa en la base de datos
            val mesaGuardada = addMesa(mesa)

            if (mesaGuardada) {
                // Registrar en el historial
                val userId = window.localStorage.getItem("username") ?: ""
                val historyEntry = History(
                    id = generateUUID(),
                    userId = userId,
                    action = "Generación de presupuesto",
                    timestamp = getCurrentDateTime(),
                    details = "Mesa tipo: ${mesa.tipo}, Precio: ${mesa.precioTotal}€"
                )
                addHistory(historyEntry)

                success = true

                // Limpiar datos después de guardar con éxito
                BudgetManager.clearAllData()
            } else {
                error = "No se pudo guardar el presupuesto"
            }

            isLoading = false
        } catch (e: Exception) {
            error = "Error al generar presupuesto: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.px),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            P { Text("Generando presupuesto...") }
            // Mostrar indicador de carga
        } else if (success) {
            H2 { Text("¡Presupuesto generado con éxito!") }
            P { Text("El presupuesto ha sido guardado correctamente.") }

            // Botón para descargar PDF (simulado)
            Button(
                attrs = {
                    onClick {
                        // Implementación de descarga de PDF
                        window.alert("PDF descargado correctamente.")
                    }
                    style {
                        property("padding", "10px")
                        property("margin", "20px")
                        property("background-color", "green")
                        property("color", "white")
                        property("border-radius", "5px")
                    }
                }
            ) {
                Text("Descargar PDF")
            }

            // Botón para volver al inicio
            Button(
                attrs = {
                    onClick { context.router.navigateTo(Screen.Home.route) }
                    style {
                        property("padding", "10px")
                        property("background-color", "#0078d4")
                        property("color", "white")
                        property("border-radius", "5px")
                    }
                }
            ) {
                Text("Volver al inicio")
            }
        } else {
            H2 { Text("Error") }
            P { Text(error ?: "Ha ocurrido un error desconocido.") }

            Button(
                attrs = {
                    onClick { context.router.navigateTo(Screen.TableSelectorBudget.route) }
                    style {
                        property("padding", "10px")
                        property("background-color", "#0078d4")
                        property("color", "white")
                        property("border-radius", "5px")
                    }
                }
            ) {
                Text("Volver a intentar")
            }
        }
    }
}

private fun getCurrentDateTime(): String {
    return js("new Date().toISOString()") as String
}

private fun generateUUID(): String {
    return js("crypto.randomUUID ? crypto.randomUUID() : 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) { var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8); return v.toString(16); })") as String
}