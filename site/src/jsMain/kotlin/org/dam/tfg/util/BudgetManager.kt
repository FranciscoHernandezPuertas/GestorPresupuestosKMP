package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.Formula
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.repositories.BudgetRepository
import org.dam.tfg.repositories.BudgetRepositoryJs
import org.w3c.dom.get
import org.w3c.dom.set

object BudgetManager {

    private val repository: BudgetRepository = BudgetRepositoryJs()

    // Mesa tipo (1 tramo, 2 tramos, etc.)
    fun saveMesaTipo(tipo: String) {
        repository.setMesaTipo(tipo)
    }

    fun getMesaTipo(): String {
        return repository.getMesaTipo()
    }

    // Tramos
    fun saveMesaTramos(tramos: List<Tramo>) {
        repository.setMesaTramos(tramos)
    }

    fun getMesaTramos(): List<Tramo> {
        return repository.getMesaTramos()
    }

    // Precio total
    fun saveMesaPrecioTotal(precioTotal: Double) {
        repository.setMesaPrecioTotal(precioTotal)
    }

    fun getMesaPrecioTotal(): Double {
        return repository.getMesaPrecioTotal()
    }

    // Métodos para guardar todos los datos de la mesa
    fun saveMesaData(
        tipoMesa: String,
        tramos: List<Tramo>,
        extras: List<String>,
        precioTotal: Double
    ) {
        saveMesaTipo(tipoMesa)
        saveMesaTramos(tramos)
        saveMesaPrecioTotal(precioTotal)
    }

    // Elementos
    // Función modificada para guardar elementos con cantidad y precio
    fun saveElementosData(elementos: Map<String, Map<String, Int>>) {
        localStorage["elementos_data"] = Json.encodeToString(elementos)
    }

    // Función modificada para obtener elementos con cantidad y precio
    fun getElementosData(): Map<String, Map<String, Int>> {
        val elementosJson = localStorage["elementos_data"]
        return if (!elementosJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(elementosJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    // Obtener solo los nombres de los elementos
    fun getElementosNombres(): List<String> {
        return getElementosData().keys.toList()
    }


    // Métodos adicionales para errores o validaciones
    fun setCubetaError(error: String) {
        repository.setCubetaError(error)
    }

    fun getCubetaError(): String {
        return repository.getCubetaError()
    }

    fun setMesaError(error: String) {
        repository.setMesaError(error)
    }

    fun getMesaError(): String {
        return repository.getMesaError()
    }

    fun saveCubetas(cubetas: List<Cubeta>) {
        repository.saveCubetas(cubetas)
    }

    fun getCubetas(): List<Cubeta> {
        return repository.getCubetas()
    }

    fun saveModulos(modulos: List<Modulo>) {
        repository.saveModulos(modulos)
    }

    fun getModulos(): List<Modulo> {
        return repository.getModulos()
    }

    // Función para limpiar todos los datos
    fun clearAllData() {
        localStorage.removeItem("mesa_tipo")
        localStorage.removeItem("mesa_tramos")
        localStorage.removeItem("elementos_data")
        localStorage.removeItem("cubetas_data")
        localStorage.removeItem("modulos_data")
        localStorage.removeItem("mesa_precio_total")
        localStorage.removeItem("form_state")
        localStorage.removeItem("mesa_error")
        localStorage.removeItem("cubeta_error")
    }

    fun calcularAreaElemento(elemento: Any): Double {
        return when (elemento) {
            is Tramo -> elemento.largo * elemento.ancho
            is Cubeta -> elemento.largo * elemento.fondo
            is Modulo -> elemento.largo * elemento.fondo
            else -> 0.0
        }
    }

    fun calcularVolumenElemento(elemento: Any): Double {
        return when (elemento) {
            is Tramo -> 0.0  // Los tramos no tienen volumen relevante
            is Cubeta -> elemento.largo * elemento.fondo * (elemento.alto ?: 0.0)
            is Modulo -> elemento.largo * elemento.fondo * elemento.alto
            else -> 0.0
        }
    }

    // Evaluar fórmulas usando JS
    fun evaluarFormula(formula: String, variables: Map<String, Double>): Double {
        try {
            // Reemplazar las variables en la fórmula
            var expresion = formula
            variables.forEach { (variable, valor) ->
                expresion = expresion.replace(variable, valor.toString())
            }

            // Evaluar la expresión
            return js("eval(expresion)") as Double
        } catch (e: Exception) {
            console.error("Error al evaluar fórmula: ${e.message}")
            return 0.0
        }
    }

    // Función principal para calcular el presupuesto total
    suspend fun calcularPresupuesto(formulas: Map<String, Formula>): Pair<Double, Map<String, Double>> {
        val tramos = getMesaTramos()
        val cubetas = getCubetas()
        val modulos = getModulos()
        val elementos = getElementosNombres().map { nombre ->
            val datos = getElementosData()[nombre] ?: mapOf()
            ElementoSeleccionado(
                nombre = nombre,
                cantidad = datos["cantidad"] ?: 0,
                precio = (datos["precio"] ?: 0).toDouble(),
                limite = ItemWithLimits(name = nombre)
            )
        }

        // Preparar variables comunes
        val variables = mutableMapOf<String, Double>()

        // Añadir área y volumen de todos los elementos
        variables["areaTotal"] = tramos.sumOf { calcularAreaElemento(it) }
        variables["volumenTotal"] = cubetas.sumOf { calcularVolumenElemento(it) } +
                modulos.sumOf { calcularVolumenElemento(it) }

        // Precios de materiales (deberían venir de la base de datos)
        variables["material"] = 10.0 // Ejemplo

        // Calcular precios por categoría
        val desglose = mutableMapOf<String, Double>()
        var precioTotal = 0.0

        // Calcular precios para los tramos
        tramos.forEachIndexed { index, tramo ->
            val formulaTramo = formulas["Tramos"]
            val materials = getAllMaterials()
            if (formulaTramo != null) {
                val varsTramo = variables.toMutableMap().apply {
                    put("areaTramo", calcularAreaElemento(tramo))
                    put("largoTramo", tramo.largo)
                    put("anchoTramo", tramo.ancho)

                    materials.forEach { material ->
                        put(material.name.lowercase().replace(" ", "_"), material.price)
                    }

                    // Añadir variables específicas de la fórmula
                    formulaTramo.variables.forEach { (key, value) ->
                        put(key, value.toDoubleOrNull() ?: 0.0)
                    }
                }

                val precio = evaluarFormula(formulaTramo.formula, varsTramo)
                desglose["tramo_$index"] = precio
                precioTotal += precio
            }
        }

        // Calcular precios para cubetas
        // Modificar la función para obtener materiales antes de evaluar
        cubetas.forEachIndexed { index, cubeta ->
            val formulaCubeta = formulas["Cubetas"]
            if (formulaCubeta != null) {
                // Obtener materiales de la base de datos (carga previa o cargar aquí)
                val materials = getAllMaterials() // Esto debe ejecutarse en un contexto de corrutina

                val varsCubeta = variables.toMutableMap().apply {
                    put("areaCubeta", calcularAreaElemento(cubeta))
                    put("volumenCubeta", calcularVolumenElemento(cubeta))
                    put("cubetas", 1.0) // Esta cubeta específica
                    put("largoCubeta", cubeta.largo)
                    put("fondoCubeta", cubeta.fondo)
                    put("altoCubeta", cubeta.alto ?: 0.0)

                    // Añadir los materiales como variables
                    materials.forEach { material ->
                        put(material.name.lowercase().replace(" ", "_"), material.price)
                    }

                    formulaCubeta.variables.forEach { (key, value) ->
                        put(key, value.toDoubleOrNull() ?: 0.0)
                    }
                }

                val precio = evaluarFormula(formulaCubeta.formula, varsCubeta)
                desglose["cubeta_$index"] = precio
                precioTotal += precio
            }
        }

        // Calcular precios para módulos
        modulos.forEachIndexed { index, modulo ->
            val formulaModulo = formulas["Modulos"]
            val materials = getAllMaterials()
            if (formulaModulo != null) {
                val varsModulo = variables.toMutableMap().apply {
                    put("areaModulo", calcularAreaElemento(modulo))
                    put("volumenModulo", calcularVolumenElemento(modulo))
                    put("cantidad", modulo.cantidad.toDouble())

                    materials.forEach { material ->
                        put(material.name.lowercase().replace(" ", "_"), material.price)
                    }

                    formulaModulo.variables.forEach { (key, value) ->
                        put(key, value.toDoubleOrNull() ?: 0.0)
                    }
                }

                val precioUnitario = evaluarFormula(formulaModulo.formula, varsModulo)
                var precioTotal = precioUnitario * modulo.cantidad
                desglose["modulo_$index"] = precioTotal
                precioTotal += precioTotal
            }
        }

        // Calcular precios para elementos generales
        elementos.forEach { elemento ->
            val formulaElemento = formulas["Elementos"]
            val materials = getAllMaterials()
            if (formulaElemento != null) {
                val varsElemento = variables.toMutableMap().apply {
                    put("cantidad", elemento.cantidad.toDouble())

                    materials.forEach { material ->
                        put(material.name.lowercase().replace(" ", "_"), material.price)
                    }

                    formulaElemento.variables.forEach { (key, value) ->
                        put(key, value.toDoubleOrNull() ?: 0.0)
                    }
                }

                val precioUnitario = evaluarFormula(formulaElemento.formula, varsElemento)
                var precioTotal = precioUnitario * elemento.cantidad
                desglose["elemento_${elemento.nombre}"] = precioTotal
                precioTotal += precioTotal
            }
        }

        return Pair(precioTotal, desglose)
    }
}