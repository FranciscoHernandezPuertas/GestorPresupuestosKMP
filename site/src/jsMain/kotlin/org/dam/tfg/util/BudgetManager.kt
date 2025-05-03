package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.Formula
import org.dam.tfg.models.Material
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.models.table.TipoTramo
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.repositories.BudgetRepository
import org.dam.tfg.repositories.BudgetRepositoryJs
import org.w3c.dom.get
import org.w3c.dom.set

object BudgetManager {
    private val repository: BudgetRepository = BudgetRepositoryJs()

    // Gestión de elementos generales seleccionados
    private val elementosSeleccionados = mutableMapOf<String, MutableMap<String, Int>>()

    // Caché de precios de materiales
    private var materialesCache: Map<String, Double> = emptyMap()

    // FUNCIONES DE MESA GENERAL
    fun setMesaTipo(tipo: String) {
        repository.setMesaTipo(tipo)
    }

    fun setMesaTramos(tramos: List<Tramo>) {
        repository.setMesaTramos(tramos)
    }

    fun setMesaError(error: String) {
        repository.setMesaError(error)
    }
    fun getMesaError(): String = repository.getMesaError()


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


    // FUNCIONES DE ELEMENTO GENERAL
    fun addElemento(nombre: String, cantidad: Int, precio: Int) {
        elementosSeleccionados[nombre] = mutableMapOf(
            "cantidad" to cantidad,
            "precio" to precio
        )
        saveElementos()
    }

    fun updateElementoCantidad(nombre: String, cantidad: Int) {
        elementosSeleccionados[nombre]?.let {
            it["cantidad"] = cantidad
            saveElementos()
        }
    }

    fun getElementoCantidad(nombre: String): Int {
        return elementosSeleccionados[nombre]?.get("cantidad") ?: 0
    }

    fun getElementosNombres(): List<String> {
        return elementosSeleccionados.keys.toList()
    }

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


    private fun saveElementos() {
        localStorage["elementos_seleccionados"] = Json.encodeToString(elementosSeleccionados)
    }

    fun loadElementos() {
        localStorage["elementos_seleccionados"]?.let {
            try {
                val loaded: Map<String, MutableMap<String, Int>> = Json.decodeFromString(it)
                elementosSeleccionados.clear()
                elementosSeleccionados.putAll(loaded)
            } catch (e: Exception) {
                console.error("Error loading elementos: ${e.message}")
            }
        }
    }

    // FUNCIONES DE CUBETAS
    fun saveCubetas(cubetas: List<Cubeta>) {
        repository.saveCubetas(cubetas)
    }

    fun getCubetas(): List<Cubeta> = repository.getCubetas()

    // FUNCIONES DE MÓDULOS
    fun saveModulos(modulos: List<Modulo>) {
        repository.saveModulos(modulos)
    }

    fun getModulos(): List<Modulo> = repository.getModulos()

    // FUNCIONES DE CÁLCULO
    fun evaluarFormula(formula: String, variables: Map<String, Double>): Double {
        try {
            // Ordenar variables por longitud de nombre (de mayor a menor)
            // para evitar reemplazos parciales
            val sortedVars = variables.keys.sortedByDescending { it.length }

            var expresion = formula.trim()
            console.log("Fórmula original: $formula")

            // Reemplazar cada variable con su valor
            for (variable in sortedVars) {
                val value = variables[variable]
                if (value != null) {
                    // Asegurar que reemplazamos la variable completa, no parte de otra
                    expresion = expresion.replace(variable, value.toString())
                }
            }

            console.log("Expresión final: $expresion")
            return js("eval(expresion)") as Double
        } catch (e: Exception) {
            console.error("Error al evaluar fórmula: ${e.message}")
            return 0.0
        }
    }

    // Funciones auxiliares para calcular áreas y volúmenes
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
            is Cubeta -> elemento.largo * elemento.fondo * (elemento.alto ?: 0.0)
            is Modulo -> elemento.largo * elemento.fondo * elemento.alto
            else -> 0.0
        }
    }

    // Función principal para calcular el presupuesto total
    suspend fun calcularPresupuesto(formulas: Map<String, Formula>): Pair<Double, Map<String, Double>> {
        loadElementos()
        val tramos = getMesaTramos()
        val cubetas = getCubetas()
        val modulos = getModulos()
        val elementos = getElementosNombres().map { nombre ->
            ElementoSeleccionado(
                nombre = nombre,
                cantidad = getElementoCantidad(nombre),
                precio = getElementoCantidad(nombre) * 10.0,  // Precio base provisional
                limite = org.dam.tfg.models.ItemWithLimits(name = nombre)
            )
        }

        // Cargar los materiales (precios) desde el servidor
        val materiales = getAllMaterials()

        // Preparar variables comunes para todas las fórmulas
        val variables = mutableMapOf<String, Double>()

        // Añadir área y volumen de todos los elementos
        variables["areaTotal"] = tramos.sumOf { calcularAreaElemento(it) }
        variables["volumenTotal"] = (cubetas.sumOf { calcularVolumenElemento(it) } +
                modulos.sumOf { calcularVolumenElemento(it) })

        // Añadir cantidad total de elementos
        variables["cantidadTramos"] = tramos.size.toDouble()
        variables["cantidadCubetas"] = cubetas.size.toDouble()
        variables["cantidadModulos"] = modulos.size.toDouble()

        // Cargar todas las variables de las fórmulas
        formulas.values.forEach { formula ->
            formula.variables.forEach { (key, value) ->
                // Solo añadir si no es material (se añade después con precios reales)
                if (!key.startsWith("material")) {
                    variables[key] = value.toDoubleOrNull() ?: 0.0
                }
            }
        }

        // Añadir todos los materiales como variables
        materiales.forEach { (nombre, precio) ->
            variables[nombre] = precio
        }

        // Calcular precios por categoría
        val desglose = mutableMapOf<String, Double>()
        var precioTotal = 0.0

        // Calcular precios para los tramos
        tramos.forEachIndexed { index, tramo ->
            val formulaTramo = formulas["Tramos"]

            if (formulaTramo != null) {
                console.log("Tramo ${index}: largo=${tramo.largo}, ancho=${tramo.ancho}")
                console.log("Área calculada: ${calcularAreaElemento(tramo)}")

                val varsTramo = variables.toMutableMap().apply {
                    // Variables específicas del tramo
                    put("largo", tramo.largo)
                    put("ancho", tramo.ancho)
                    put("areaTramo", tramo.largo * tramo.ancho)
                    put("superficie", tramo.largo * tramo.ancho) // Alias de área para compatibilidad
                    put("superficieTramo", tramo.largo * tramo.ancho) // Alias de área para compatibilidad

                    // Variable de tipo (mural o central)
                    when (tramo.tipo) {
                        TipoTramo.MURAL -> put("tipoTramo", variables["tipoTramoMural"] ?: 30.0)
                        TipoTramo.CENTRAL -> put("tipoTramo", variables["tipoTramoCentral"] ?: 40.0)
                    }
                }

                // Calcular precio con la fórmula
                val precioTramo = evaluarFormula(formulaTramo.formula, varsTramo)
                desglose["tramo_$index"] = precioTramo
                precioTotal += precioTramo
            } else {
                // Si no hay fórmula, usar precio base
                desglose["tramo_$index"] = tramo.precio
                precioTotal += tramo.precio
            }
        }

        // Calcular precios para las cubetas
        cubetas.forEachIndexed { index, cubeta ->
            val formulaCubeta = formulas["Cubetas"]

            if (formulaCubeta != null) {
                val varsCubeta = variables.toMutableMap().apply {
                    // Variables específicas de la cubeta
                    put("largo", cubeta.largo)
                    put("fondo", cubeta.fondo)
                    put("alto", cubeta.alto ?: 0.0)
                    put("areaCubeta", cubeta.largo * cubeta.fondo)
                    put("volumenCubeta", cubeta.largo * cubeta.fondo * (cubeta.alto ?: 0.0))
                    put("tipoCubeta", variables["tipoCubeta_${cubeta.tipo}"] ?: 0.0)
                }

                // Calcular precio con la fórmula
                val precioCubeta = evaluarFormula(formulaCubeta.formula, varsCubeta)
                desglose["cubeta_$index"] = precioCubeta
                precioTotal += precioCubeta
            } else {
                // Si no hay fórmula, usar precio base
                desglose["cubeta_$index"] = cubeta.precio
                precioTotal += cubeta.precio
            }
        }

        // Para los módulos
        modulos.forEachIndexed { index, modulo ->
            val formulaModulo = formulas["Modulos"]

            if (formulaModulo != null) {
                val varsModulo = variables.toMutableMap().apply {
                    // Normalizar el nombre para buscar la variable correspondiente
                    val nombreNormalizado = modulo.nombre.replace(" ", "")

                    // Buscar el valor directamente en las variables de la fórmula
                    var valorTipo = 0.0
                    formulaModulo.variables.forEach { (key, value) ->
                        if (key.equals("tipo$nombreNormalizado", ignoreCase = true)) {
                            valorTipo = value.toDoubleOrNull() ?: 0.0
                            return@forEach
                        }
                    }

                    console.log("Módulo: ${modulo.nombre}, variable buscada: tipo$nombreNormalizado, valor encontrado: $valorTipo")
                    put("tipoModulo", valorTipo)

                    // Variables específicas del módulo
                    put("largo", modulo.largo)
                    put("ancho", modulo.fondo)
                    put("alto", modulo.alto)
                    put("cantidad", modulo.cantidad.toDouble())
                    put("volumen", modulo.largo * modulo.fondo * modulo.alto)
                }

                // Calcular precio con la fórmula
                val precioModulo = evaluarFormula(formulaModulo.formula, varsModulo) * modulo.cantidad
                desglose["modulo_$index"] = precioModulo
                precioTotal += precioModulo
                console.log("Módulo $index (${modulo.nombre}): Precio calculado = $precioModulo")
            }
        }

        // Calcular precios para elementos generales
        // Calcular precios para elementos generales
        elementos.forEach { elemento ->
            val formulaElementos = formulas["Elementos"]

            if (formulaElementos != null) {
                // Construir el nombre de la variable según el formato "tipo" + nombre del elemento sin espacios
                val nombreVariable = "tipo" + elemento.nombre.replace(" ", "")

                console.log("Elemento: ${elemento.nombre}, variable buscada: $nombreVariable")

                // Verificar si existe la variable en la fórmula
                val valorTipo = formulaElementos.variables[nombreVariable]?.toDoubleOrNull() ?: 0.0

                // Añadir esta variable específica al mapa de variables para la evaluación
                val elementoVariables = variables.toMutableMap()
                elementoVariables["tipoElemento"] = valorTipo

                // Evaluar la fórmula con las variables del elemento
                val precioElemento = evaluarFormula(formulaElementos.formula, elementoVariables) * elemento.cantidad

                console.log("Elemento ${elemento.nombre} (${elemento.cantidad}): Precio calculado = $precioElemento")

                // Añadir al desglose y al precio total
                desglose["elemento_${elemento.nombre}"] = precioElemento
                precioTotal += precioElemento
            } else {
                // Si no hay fórmula, usar el precio predeterminado
                desglose["elemento_${elemento.nombre}"] = elemento.precio * elemento.cantidad
                precioTotal += elemento.precio * elemento.cantidad
            }
        }





// Asegurarnos de que el precio total incluye todos los componentes
        console.log("Precio total calculado: $precioTotal (Desglose: ${desglose.values.sum()})")
        saveMesaPrecioTotal(precioTotal)
        return Pair(precioTotal, desglose)
    }

    // Función para obtener materiales (necesario para los precios)
    private suspend fun getAllMaterials(): Map<String, Double> {
        try {
            // Usar la caché si ya tenemos los materiales
            if (materialesCache.isNotEmpty()) {
                return materialesCache
            }

            // Si no hay en caché, cargar desde el servidor
            val materials = org.dam.tfg.util.getAllMaterials()

            // Transformar nombres de materiales para usar en fórmulas (lowercase y sin espacios)
            val materialesMap = materials.associate {
                it.name.lowercase().replace(" ", "_") to it.price
            }

            // Guardar en caché para futuras llamadas
            materialesCache = materialesMap

            return materialesMap
        } catch (e: Exception) {
            console.error("Error al obtener materiales: ${e.message}")
            return emptyMap()
        }
    }

    // Precarga los materiales para tenerlos disponibles cuando se necesiten
    fun precargarMateriales() {
        GlobalScope.launch {
            try {
                val materials = getAllMaterials()
                console.log("Materiales precargados: ${materials.keys.joinToString()}")
            } catch (e: Exception) {
                console.error("Error al precargar materiales: ${e.message}")
            }
        }
    }
}