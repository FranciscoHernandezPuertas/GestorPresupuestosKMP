package org.dam.tfg.pages.budget

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.LoadingIndicator
import org.dam.tfg.models.History
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.navigation.Screen
import org.dam.tfg.util.*
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLScriptElement
import kotlin.js.json

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
    var pdfGenerating by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mesa by remember { mutableStateOf<Mesa?>(null) }

    // Cargar jsPDF al inicio del componente
    LaunchedEffect(Unit) {
        loadJsPDF()
    }

    LaunchedEffect(Unit) {
        try {
            // Obtener la fecha actual
            val fechaActual = getCurrentDateTime()

            // Crear objeto Mesa con todos los datos guardados y sus precios correctos
            val mesaData = Mesa(
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
                fechaCreacion = fechaActual, // Añadir la fecha actual
                error = ""
            )

            mesa = mesaData

            // Guardar la mesa en la base de datos
            val mesaGuardada = addMesa(mesaData)

            if (mesaGuardada) {
                // Registrar en el historial
                val userId = window.localStorage.getItem("username") ?: ""
                val historyEntry = History(
                    id = generateUUID(),
                    userId = userId,
                    action = "Generación de presupuesto",
                    timestamp = fechaActual, // Usar la misma fecha para consistencia
                    details = "Mesa tipo: ${mesaData.tipo}, Precio: ${mesaData.precioTotal}€, Fecha: $fechaActual"
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
            LoadingIndicator()
        } else if (success) {
            H2 { Text("¡Presupuesto generado con éxito!") }
            P { Text("El presupuesto ha sido guardado correctamente.") }

            // Botón para descargar PDF (implementación real)
            Button(
                attrs = {
                    onClick {
                        coroutineScope.launch {
                            pdfGenerating = true
                            try {
                                val username = window.localStorage.getItem("username") ?: "Cliente"
                                mesa?.let { generateAndDownloadPdf(it, username) }
                            } finally {
                                pdfGenerating = false
                            }
                        }
                    }
                    style {
                        property("padding", "10px")
                        property("margin", "20px")
                        property("background-color", "green")
                        property("color", "white")
                        property("border-radius", "5px")
                        if (pdfGenerating) {
                            property("opacity", "0.7")
                            property("cursor", "wait")
                        }
                    }
                    if (pdfGenerating) {
                        attr("disabled", "true")
                    }
                }
            ) {
                Text(if (pdfGenerating) "Generando PDF..." else "Descargar PDF")
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

// Cargar jsPDF dinámicamente
private fun loadJsPDF() {
    val scriptId = "jspdf-script"
    if (document.getElementById(scriptId) == null) {
        val script = (document.createElement("script") as HTMLScriptElement).apply {
            id = scriptId
            src = "https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"
        }
        document.head?.appendChild(script)
    }
}

// Función para generar y descargar el PDF
private fun generateAndDownloadPdf(mesa: Mesa, username: String) {
    // Preparar datos necesarios
    val logoPath = Res.Image.logo
    val mesaTipo = mesa.tipo
    val mesaPrecioTotal = mesa.precioTotal.toString()
    val fechaCreacion = mesa.fechaCreacion ?: getCurrentDateTime() // Usar la fecha guardada o generar una nueva

    // Serializar colecciones a JSON
    val tramosJson = Json.encodeToString(mesa.tramos)
    val cubetasJson = Json.encodeToString(mesa.cubetas)
    val modulosJson = Json.encodeToString(mesa.modulos)
    val elementosJson = Json.encodeToString(mesa.elementosGenerales)

    // Exponer funciones necesarias para que JS pueda acceder a los datos
    // Usando una aproximación diferente con un script dinámico
    val scriptId = "pdf-data-script"
    val existingScript = document.getElementById(scriptId)
    if (existingScript != null) {
        document.head?.removeChild(existingScript)
    }

    val dataScript = (document.createElement("script") as HTMLScriptElement).apply {
        id = scriptId
        text = """
            window._pdfData = {
                logoPath: "${logoPath}",
                username: "${username}",
                mesaTipo: "${mesaTipo}",
                mesaPrecioTotal: ${mesaPrecioTotal},
                fechaCreacion: "${fechaCreacion}",
                tramos: ${tramosJson},
                cubetas: ${cubetasJson},
                modulos: ${modulosJson},
                elementos: ${elementosJson}
            };
            
            function generatePdf() {
                // Asegurarse de que jsPDF está cargado
                if (typeof window.jspdf === 'undefined') {
                    alert('Error: jsPDF no está cargado. Por favor, inténtelo de nuevo.');
                    return;
                }
                
                // Crear una instancia de jsPDF
                const { jsPDF } = window.jspdf;
                const doc = new jsPDF('p', 'mm', 'a4');
                
                // Añadir logo
                try {
                    doc.addImage(window._pdfData.logoPath, 'SVG', 15, 10, 50, 25);
                } catch(e) {
                    console.error('Error al cargar el logo:', e);
                }
                
                // Configurar la página
                doc.setFontSize(20);
                doc.setTextColor(0, 78, 152);
                doc.text('PRESUPUESTO', 105, 20, null, null, 'center');
                
                // Información del cliente y fecha
                const fechaFormateada = new Date(window._pdfData.fechaCreacion).toLocaleDateString('es-ES');
                doc.setFontSize(10);
                doc.setTextColor(0, 0, 0);
                doc.text('Cliente: ' + window._pdfData.username, 140, 35);
                doc.text('Fecha: ' + fechaFormateada, 140, 40);
                doc.text('Ref: PR-' + Math.floor(Math.random()*10000), 140, 45);
                
                // Línea separadora
                doc.setDrawColor(0, 78, 152);
                doc.setLineWidth(0.5);
                doc.line(15, 50, 195, 50);
                
                // Título de la mesa
                doc.setFontSize(14);
                doc.setTextColor(0, 78, 152);
                doc.text('Mesa tipo: ' + window._pdfData.mesaTipo, 15, 60);
                
                // Detalles del presupuesto
                let y = 70;
                
                // Función para añadir secciones
                function addSection(title, items, getDescription, getPrice) {
                    if (items.length === 0) return y;
                    
                    // Verificar si necesitamos una nueva página
                    if (y > 250) {
                        doc.addPage();
                        y = 20;
                    }
                    
                    doc.setFontSize(12);
                    doc.setTextColor(0, 78, 152);
                    doc.text(title, 15, y);
                    y += 7;
                    
                    // Cabecera de tabla
                    doc.setFillColor(240, 240, 240);
                    doc.rect(15, y-5, 180, 7, 'F');
                    doc.setFontSize(9);
                    doc.setTextColor(0, 0, 0);
                    doc.text('Descripción', 18, y);
                    doc.text('Precio', 170, y);
                    y += 5;
                    
                    // Elementos
                    doc.setFontSize(10);
                    items.forEach((item, index) => {
                        // Verificar si necesitamos una nueva página
                        if (y > 270) {
                            doc.addPage();
                            y = 20;
                            
                            // Repetir cabecera
                            doc.setFillColor(240, 240, 240);
                            doc.rect(15, y-5, 180, 7, 'F');
                            doc.setFontSize(9);
                            doc.text('Descripción', 18, y);
                            doc.text('Precio', 170, y);
                            y += 5;
                            doc.setFontSize(10);
                        }
                        
                        const description = getDescription(item, index);
                        const price = getPrice(item);
                        const formattedPrice = price.toLocaleString('es-ES', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2
                        }) + ' €';
                        
                        doc.text(description, 18, y);
                        doc.text(formattedPrice, 190, y, null, null, 'right');
                        y += 7;
                    });
                    
                    y += 5;
                    return y;
                }
                
                // Tramos
                y = addSection('Tramos', window._pdfData.tramos, 
                    function(tramo, i) { return 'Tramo ' + (i+1) + ' (' + tramo.largo + 'x' + tramo.ancho + ')' },
                    function(tramo) { return tramo.precio }
                );
                
                // Cubetas
                y = addSection('Cubetas', window._pdfData.cubetas, 
                    function(cubeta, i) { return 'Cubeta ' + (i+1) + ' - ' + cubeta.tipo + ' (' + cubeta.largo + 'x' + cubeta.fondo + 'x' + (cubeta.alto || 0) + ')' },
                    function(cubeta) { return cubeta.precio }
                );
                
                // Módulos
                y = addSection('Módulos', window._pdfData.modulos, 
                    function(modulo) { return modulo.nombre + ' (' + modulo.cantidad + ' ud)' },
                    function(modulo) { return modulo.precio }
                );
                
                // Elementos generales
                y = addSection('Elementos adicionales', window._pdfData.elementos,
                    function(elemento) { return elemento.nombre + ' (' + elemento.cantidad + ' ud)' },
                    function(elemento) { return elemento.precio * elemento.cantidad }
                );
                
                // Verificar si necesitamos una nueva página para el total
                if (y > 260) {
                    doc.addPage();
                    y = 20;
                }
                
                // Precio total
                doc.setLineWidth(0.5);
                doc.line(15, y, 195, y);
                y += 10;
                
                doc.setFontSize(14);
                doc.setFont(undefined, 'bold');
                doc.text('PRECIO TOTAL:', 15, y);
                const precioTotalFormateado = Number(window._pdfData.mesaPrecioTotal).toLocaleString('es-ES', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                }) + ' €';
                doc.text(precioTotalFormateado, 190, y, null, null, 'right');
                
                // Pie de página
                doc.setFont(undefined, 'normal');
                doc.setFontSize(8);
                doc.setTextColor(128, 128, 128);
                
                const pageCount = doc.internal.getNumberOfPages();
                for(let i = 1; i <= pageCount; i++) {
                    doc.setPage(i);
                    doc.text('Página ' + i + ' de ' + pageCount, 195, 287, null, null, 'right');
                    doc.text('© 2025 GeneradorPresupuestosKMP', 105, 287, null, null, 'center');
                }
                
                // Guardar PDF
                doc.save('Presupuesto_' + window._pdfData.mesaTipo + '.pdf');
                
                // Limpiar los datos después de usarlos
                delete window._pdfData;
            }
        """
    }
    document.head?.appendChild(dataScript)

    // Ejecutar la función que acaba de ser definida
    js("generatePdf()")

    // Limpiar después de ejecutar
    document.head?.removeChild(dataScript)
}