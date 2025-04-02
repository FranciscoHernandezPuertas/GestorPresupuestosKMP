package org.dam.tfg.util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.browser.localStorage
import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.Extra
import org.dam.tfg.models.table.Mesa

object DimensionManager {
    // Cargar mesa desde el almacenamiento local
    fun loadMesa(): Mesa {
        val mesaJson = localStorage.getItem("mesa_data")
        return if (!mesaJson.isNullOrBlank()) {
            try {
                Json.decodeFromString(mesaJson)
            } catch (e: Exception) {
                console.log("Error al cargar la mesa: ${e.message}")
                Mesa()
            }
        } else {
            Mesa()
        }
    }

    // Calcular el área total de la mesa
    fun calcularAreaTotal(mesa: Mesa): Double {
        return mesa.tramos.sumOf { it.largo * it.ancho }
    }

    // Calcular el área ocupada por elementos
    fun <T : Extra> calcularAreaOcupada(items: List<T>): Double {
        return items.sumOf { it.largo.orZero() * it.ancho.orZero() * it.numero }
    }

    // Comprobar si un elemento cabe en las dimensiones de la mesa
    fun elementoCabeDimensionalmente(
        largoElemento: Double,
        anchoElemento: Double,
        dimensionesDisponibles: Pair<Double, Double>
    ): Boolean {
        return largoElemento <= dimensionesDisponibles.first &&
                anchoElemento <= dimensionesDisponibles.second
    }
    // Calcular dimensiones mínimas disponibles
    fun calcularDimensionesMinimasDisponibles(mesa: Mesa): Pair<Double, Double> {
        if (mesa.tramos.isEmpty()) {
            console.log("Advertencia: La mesa no tiene tramos definidos")
            return Pair(0.0, 0.0)
        }

        // Usar dimensiones máximas en lugar de mínimas
        var maxLargo = 0.0
        var maxAncho = 0.0

        mesa.tramos.forEach { tramo ->
            if (tramo.largo > maxLargo) maxLargo = tramo.largo
            if (tramo.ancho > maxAncho) maxAncho = tramo.ancho
        }

        console.log("Dimensiones disponibles calculadas: $maxLargo x $maxAncho")
        return Pair(maxLargo, maxAncho)
    }

    fun <T : Extra> filtrarOpcionesPorDimensiones(
        opciones: List<ItemWithLimits>,
        itemsActuales: List<T>,
        dimensionesDisponibles: Pair<Double, Double>,
        extractorDimensiones: (String) -> Pair<Double, Double>
    ): List<ItemWithLimits> {
        // ELIMINAR esta línea para evitar sobreescribir la mesa ya cargada
        // val mesa = loadMesa()

        // En su lugar, calcula el área total basado en las dimensiones disponibles
        val areaTotal = dimensionesDisponibles.first * dimensionesDisponibles.second
        val areaOcupada = calcularAreaOcupada(itemsActuales)
        val areaDisponible = areaTotal - areaOcupada

        console.log("Área total: $areaTotal, Área ocupada: $areaOcupada, Área disponible: $areaDisponible")

        // Resto del código igual...
        val tiposYaAñadidos = itemsActuales.map { it.tipo }

        return opciones.mapNotNull { item ->
            if (tiposYaAñadidos.contains(item.name)) return@mapNotNull null

            val dimensiones = extractorDimensiones(item.name)
            val largo = dimensiones.first
            val ancho = dimensiones.second

            console.log("Evaluando ${item.name}: $largo x $ancho")

            if (!elementoCabeDimensionalmente(largo, ancho, dimensionesDisponibles)) {
                console.log("${item.name} no cabe dimensionalmente")
                return@mapNotNull null
            }

            val area = largo * ancho
            val maximoPorArea = if (area > 0) (areaDisponible / area).toInt() else 0
            val nuevoMaximo = minOf(maximoPorArea, item.maxQuantity)

            console.log("${item.name} - máximo posible: $nuevoMaximo")

            if (nuevoMaximo > 0) item.copy(maxQuantity = nuevoMaximo) else null
        }
    }

    // Extensión para manejar nullable Double
    private fun Double?.orZero() = this ?: 0.0
}