// Archivo: site/src/jsMain/kotlin/org/dam/tfg/budget/BudgetCalculatorJs.kt
package org.dam.tfg.budget

import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ModuloSeleccionado
import org.dam.tfg.models.table.Tramo
import kotlinx.browser.window

class BudgetCalculatorJs : BudgetCalculator {
    override fun calcularPrecioMesa(tramos: List<Tramo>, material: String): Double {
        // En JS, hacemos peticiones API al servidor
        return tramos.sumOf { it.superficie() * 0.01 } // Valor temporal
    }

    override fun calcularPrecioCubetas(cubetas: List<Cubeta>): Double {
        return cubetas.size * 150.0 // Valor temporal
    }

    override fun calcularPrecioModulos(modulos: List<ModuloSeleccionado>): Double {
        return modulos.sumOf { it.cantidad * 200.0 } // Valor temporal
    }

    override fun calcularPrecioElementos(elementos: Map<String, Int>): Double {
        return elementos.values.sum() * 50.0 // Valor temporal
    }

    override fun calcularPrecioTotal(
        tramos: List<Tramo>,
        cubetas: List<Cubeta>,
        modulos: List<ModuloSeleccionado>,
        elementos: Map<String, Int>,
        material: String
    ): Double {
        // Aquí se haría una llamada API al servidor para calcular el total
        return calcularPrecioMesa(tramos, material) +
                calcularPrecioCubetas(cubetas) +
                calcularPrecioModulos(modulos) +
                calcularPrecioElementos(elementos)
    }

    // Función para hacer la petición API real
    suspend fun solicitarPresupuestoFinal(): Double {
        // Implementación de llamada API
        return 0.0
    }
}