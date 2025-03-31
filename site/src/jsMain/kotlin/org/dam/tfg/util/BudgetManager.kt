package org.dam.tfg.util

import kotlinx.browser.localStorage
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.Extra
import org.dam.tfg.models.table.Material
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Tramo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set

object BudgetManager {
    // Datos de la mesa
    private var tipoMesa: String = ""
    private var tramos: MutableList<Tramo> = mutableListOf()
    private var material: Material? = null

    // Datos de extras
    private var cubetas: MutableList<Cubeta> = mutableListOf()
    private var otrosExtras: MutableList<Extra> = mutableListOf()

    // Gestión de tipo de mesa y tramos
    fun setTipoMesa(tipo: String) {
        tipoMesa = tipo
        saveBudgetData()
    }

    fun getTipoMesa(): String = tipoMesa

    fun setTramos(nuevosTramos: List<Tramo>) {
        tramos.clear()
        tramos.addAll(nuevosTramos)
        saveBudgetData()
    }

    fun getTramos(): List<Tramo> = tramos.toList()

    fun setMaterial(nuevoMaterial: Material) {
        material = nuevoMaterial
        saveBudgetData()
    }

    fun getMaterial(): Material? = material

    // Gestión de cubetas
    fun addCubeta(cubeta: Cubeta) {
        val existingIndex = cubetas.indexOfFirst { it.tipo == cubeta.tipo }
        if (existingIndex >= 0) {
            // Actualizar cantidad si ya existe
            val existing = cubetas[existingIndex]
            cubetas[existingIndex] = existing.copy(numero = existing.numero + 1)
        } else {
            // Agregar nueva cubeta
            cubetas.add(cubeta)
        }
        saveBudgetData()
    }

    fun updateCubetaCantidad(index: Int, cantidad: Int) {
        if (index >= 0 && index < cubetas.size) {
            cubetas[index] = cubetas[index].copy(numero = cantidad)
            saveBudgetData()
        }
    }

    fun removeCubeta(index: Int) {
        if (index >= 0 && index < cubetas.size) {
            cubetas.removeAt(index)
            saveBudgetData()
        }
    }

    fun getCubetas(): List<Cubeta> = cubetas.toList()

    // Gestión de otros extras
    fun addExtra(extra: Extra) {
        otrosExtras.add(extra)
        saveBudgetData()
    }

    fun updateExtraCantidad(index: Int, cantidad: Int) {
        if (index >= 0 && index < otrosExtras.size) {
            // Actualizar según el tipo de extra
            val extra = otrosExtras[index]
            when (extra) {
                is Cubeta -> otrosExtras[index] = extra.copy(numero = cantidad)
                // Añade más casos según los tipos de extras que tengas
                else -> {} // Fallback
            }
            saveBudgetData()
        }
    }

    fun removeExtra(index: Int) {
        if (index >= 0 && index < otrosExtras.size) {
            otrosExtras.removeAt(index)
            saveBudgetData()
        }
    }

    fun getExtras(): List<Extra> = otrosExtras.toList()

    // Crear objeto Mesa completo para el presupuesto final
    fun buildMesa(): Mesa {
        return Mesa(
            tipo = tipoMesa,
            tramos = tramos,
            extras = cubetas + otrosExtras,
            material = material
        )
    }

    // Persistencia de datos
    private fun saveBudgetData() {
        try {
            localStorage["tipoMesa"] = tipoMesa
            localStorage["tramos"] = Json.encodeToString(tramos)
            localStorage["cubetas"] = Json.encodeToString(cubetas)
            localStorage["extras"] = Json.encodeToString(otrosExtras)
            material?.let {
                localStorage["material"] = Json.encodeToString(it)
            }
        } catch (e: Exception) {
            console.error("Error guardando datos: ${e.message}")
        }
    }

    fun loadBudgetData() {
        try {
            tipoMesa = localStorage["tipoMesa"] ?: ""

            localStorage["tramos"]?.let {
                tramos = Json.decodeFromString(it)
            }

            localStorage["cubetas"]?.let {
                cubetas = Json.decodeFromString(it)
            }

            localStorage["extras"]?.let {
                otrosExtras = Json.decodeFromString(it)
            }

            localStorage["material"]?.let {
                material = Json.decodeFromString(it)
            }
        } catch (e: Exception) {
            console.error("Error cargando datos: ${e.message}")
        }
    }

    fun resetBudgetData() {
        tipoMesa = ""
        tramos.clear()
        cubetas.clear()
        otrosExtras.clear()
        material = null

        // Limpiar localStorage
        localStorage.removeItem("tipoMesa")
        localStorage.removeItem("tramos")
        localStorage.removeItem("cubetas")
        localStorage.removeItem("extras")
        localStorage.removeItem("material")
        localStorage.removeItem("table_elements")
    }
    fun loadMesa(): Mesa {
        loadBudgetData() // Asegurarnos de cargar datos del localStorage
        return buildMesa()
    }

    fun updateMesa(mesa: Mesa) {
        tipoMesa = mesa.tipo

        // Actualizar tramos
        tramos.clear()
        tramos.addAll(mesa.tramos)

        // Actualizar material si existe
        material = mesa.material

        // Guardar cambios
        saveBudgetData()
    }
}