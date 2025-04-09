package org.dam.tfg.services

import org.dam.tfg.models.ItemWithLimits
import org.dam.tfg.models.table.Extra
import org.dam.tfg.models.table.Mesa
/*
class DimensionService {
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
            println("Advertencia: La mesa no tiene tramos definidos")
            return Pair(0.0, 0.0)
        }

        // Usar dimensiones máximas en lugar de mínimas
        var maxLargo = 0.0
        var maxAncho = 0.0

        mesa.tramos.forEach { tramo ->
            if (tramo.largo > maxLargo) maxLargo = tramo.largo
            if (tramo.ancho > maxAncho) maxAncho = tramo.ancho
        }

        println("Dimensiones disponibles calculadas: $maxLargo x $maxAncho")
        return Pair(maxLargo, maxAncho)
    }

    fun <T : Extra> filtrarOpcionesPorDimensiones(
        opciones: List<ItemWithLimits>,
        itemsActuales: List<T>,
        dimensionesDisponibles: Pair<Double, Double>,
        extractorDimensiones: (String) -> Pair<Double, Double>
    ): List<ItemWithLimits> {
        val areaTotal = dimensionesDisponibles.first * dimensionesDisponibles.second
        val areaOcupada = calcularAreaOcupada(itemsActuales)
        val areaDisponible = (areaTotal * 0.90) - areaOcupada  // Límite 90% en lugar de 80%

        println("Área total: $areaTotal, Área ocupada: $areaOcupada, Área disponible: $areaDisponible")

        val tiposYaAñadidos = itemsActuales.map { it.tipo }

        return opciones.mapNotNull { item ->
            if (tiposYaAñadidos.contains(item.name)) return@mapNotNull null

            val dimensiones = extractorDimensiones(item.name)
            val largo = dimensiones.first
            val ancho = dimensiones.second

            println("Evaluando ${item.name}: $largo x $ancho")

            // Comprobar si cabe en la orientación normal o rotada
            val encaja = (largo <= dimensionesDisponibles.first && ancho <= dimensionesDisponibles.second) ||
                    (ancho <= dimensionesDisponibles.first && largo <= dimensionesDisponibles.second)

            if (!encaja) {
                println("${item.name} no cabe dimensionalmente")
                return@mapNotNull null
            }

            val area = largo * ancho
            if (area <= 0) {
                println("${item.name} tiene área inválida: $area")
                return@mapNotNull null
            }

            val maximoPorArea = if (area > 0) (areaDisponible / area).toInt() else 0
            val nuevoMaximo = minOf(maximoPorArea, item.maxQuantity).coerceAtLeast(1) // Al menos 1

            println("${item.name} - máximo posible: $nuevoMaximo")
            item.copy(maxQuantity = nuevoMaximo)
        }
    }

    // Extensión para manejar nullable Double
    private fun Double?.orZero() = this ?: 0.0
} */