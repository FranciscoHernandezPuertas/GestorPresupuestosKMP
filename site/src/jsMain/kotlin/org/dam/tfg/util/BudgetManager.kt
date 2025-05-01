package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.table.Cubeta
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
}