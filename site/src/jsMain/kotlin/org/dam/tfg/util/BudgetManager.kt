package org.dam.tfg.util

import kotlinx.browser.localStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ModuloSeleccionado
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.repositories.BudgetRepository
import org.dam.tfg.repositories.BudgetRepositoryJs
import org.w3c.dom.get
import org.w3c.dom.set

object BudgetManager {
    private val repository: BudgetRepository = BudgetRepositoryJs()

    // Funciones para gestionar la mesa y sus tramos
    fun saveMesaData(
        tipoMesa: String,
        tramos: List<Tramo>,
        extras: List<String> = emptyList(),
        precioTotal: Double = 0.0
    ) {
        repository.setMesaTipo(tipoMesa)
        repository.setMesaTramos(tramos)
        repository.setMesaExtras(extras)
        repository.setMesaPrecioTotal(precioTotal)
    }

    fun getMesaTipo(): String = repository.getMesaTipo()
    fun getMesaTramos(): List<Tramo> = repository.getMesaTramos()
    fun getMesaExtras(): List<String> = repository.getMesaExtras()
    fun getMesaPrecioTotal(): Double = repository.getMesaPrecioTotal()

    // Funciones para gestionar las cantidades de elementos
    fun saveElementosCantidades(cantidades: Map<String, Int>) {
        localStorage["elementos_cantidades"] = Json.encodeToString(cantidades)
    }

    fun getElementosCantidades(): Map<String, Int> {
        val cantidadesJson = localStorage["elementos_cantidades"]
        return if (!cantidadesJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(cantidadesJson)
            } catch (e: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    // Cálculo del área total de la mesa
    fun calcularAreaTotalMesa(): Double {
        val tramos = getMesaTramos()
        return tramos.sumOf { it.largo * it.ancho }
    }

    fun agregarCubeta(
        nombre: String,
        largo: Double,
        ancho: Double,
        alto: Double = 0.0,
        cantidad: Int = 1
    ): Boolean {
        val cubetasActuales = getCubetas()

        val nuevasCubetas = cubetasActuales.toMutableList()
        nuevasCubetas.add(Cubeta(nombre, cantidad, largo, ancho, alto, maxQuantity = cantidad))
        saveCubetas(nuevasCubetas)
        return true
    }

    fun getCubetas(): List<Cubeta> {
        val cubetasJson = localStorage["cubetas_data"] ?: return emptyList()
        return try {
            Json.decodeFromString(cubetasJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCubetas(cubetas: List<Cubeta>) {
        localStorage["cubetas_data"] = Json.encodeToString(cubetas)
    }

    // Extras/elementos generales
    fun agregarElementoGeneral(nombre: String, cantidad: Int = 1) {
        val elementosActuales = getElementosGenerales().toMutableMap()
        elementosActuales[nombre] = cantidad
        saveElementosGenerales(elementosActuales)
    }

    fun getElementosGenerales(): Map<String, Int> {
        val elementosJson = localStorage["elementos_generales"] ?: return emptyMap()
        return try {
            Json.decodeFromString(elementosJson)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveElementosGenerales(elementos: Map<String, Int>) {
        localStorage["elementos_generales"] = Json.encodeToString(elementos)
    }

    fun saveModulos(modulos: List<ModuloSeleccionado>) {
        try {
            val json = Json.encodeToString(modulos)
            localStorage["modulos_data"] = json
            console.log("Módulos guardados correctamente: $json")
        } catch (e: Exception) {
            console.error("Error al guardar módulos: ${e.message}")
        }
    }

    fun getModulos(): List<ModuloSeleccionado> {
        val modulosJson = localStorage["modulos_data"] ?: return emptyList()
        return try {
            val resultado = Json.decodeFromString<List<ModuloSeleccionado>>(modulosJson)
            console.log("Módulos recuperados correctamente: ${resultado.size}")
            resultado
        } catch (e: Exception) {
            console.error("Error al recuperar módulos: ${e.message}")
            emptyList()
        }
    }

    // Resetear todos los datos
    fun resetBudgetData() {
        localStorage.removeItem("mesa_tipo")
        localStorage.removeItem("mesa_tramos")
        localStorage.removeItem("mesa_material")
        localStorage.removeItem("mesa_extras")
        localStorage.removeItem("mesa_precio_total")
        localStorage.removeItem("cubetas_data")
        localStorage.removeItem("elementos_generales")
        localStorage.removeItem("elementos_cantidades")
    }

}
