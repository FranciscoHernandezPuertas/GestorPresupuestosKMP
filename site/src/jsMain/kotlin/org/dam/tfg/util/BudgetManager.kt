package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ElementoSeleccionado
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
        // Obtener directamente de los datos almacenados
        val elementosData = getElementosData()
        return elementosData.keys.toList()
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
}
