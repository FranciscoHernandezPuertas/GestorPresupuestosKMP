package org.dam.tfg.budget

import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ModuloSeleccionado
import org.dam.tfg.models.table.Tramo

interface BudgetCalculator {
    fun calcularPrecioMesa(tramos: List<Tramo>, material: String): Double
    fun calcularPrecioCubetas(cubetas: List<Cubeta>): Double
    fun calcularPrecioModulos(modulos: List<ModuloSeleccionado>): Double
    fun calcularPrecioElementos(elementos: Map<String, Int>): Double
    fun calcularPrecioTotal(
        tramos: List<Tramo>,
        cubetas: List<Cubeta>,
        modulos: List<ModuloSeleccionado>,
        elementos: Map<String, Int>,
        material: String
    ): Double
}