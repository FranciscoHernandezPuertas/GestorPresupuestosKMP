package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ModuloSeleccionado
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
    fun saveElementosData(elementos: Map<String, Int>) {
        localStorage["elementos_data"] = Json.encodeToString(elementos)
    }

    fun getElementosData(): Map<String, Int> {
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

    fun getElementosNombres(): List<String> {
        return getElementosData().keys.toList()
    }

    // Métodos para cubetas
    fun saveCubetasData(cubetas: List<Cubeta>) {
        localStorage["cubetas_data"] = Json.encodeToString(cubetas)
    }

    fun getCubetasData(): List<Cubeta> {
        val cubetasJson = localStorage["cubetas_data"]
        return if (!cubetasJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(cubetasJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Métodos para módulos
    fun saveModulosData(modulos: List<ModuloSeleccionado>) {
        localStorage["modulos_data"] = Json.encodeToString(modulos)
    }

    fun getModulosData(): List<ModuloSeleccionado> {
        val modulosJson = localStorage["modulos_data"]
        return if (!modulosJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(modulosJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Estado del formulario
    fun saveFormState(state: Map<String, Boolean>) {
        localStorage["form_state"] = Json.encodeToString(state)
    }

    fun getFormState(): Map<String, Boolean> {
        val stateJson = localStorage["form_state"]
        return if (!stateJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(stateJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
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

    fun saveModulos(modulos: List<ModuloSeleccionado>) {
        repository.saveModulos(modulos)
    }

    fun getModulos(): List<ModuloSeleccionado> {
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