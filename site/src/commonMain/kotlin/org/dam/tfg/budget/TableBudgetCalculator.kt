package org.dam.tfg.budget
import org.dam.tfg.models.Formula
import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ElementoSeleccionado
import org.dam.tfg.models.table.Mesa
import org.dam.tfg.models.table.Modulo
import org.dam.tfg.models.table.Tramo

class TableBudgetCalculator(private val formulas: List<Formula>) {

    fun calcularPresupuesto(mesa: Mesa): Double {
        return calcularPrecioTotal(
            mesa.tramos,
            mesa.cubetas,
            mesa.modulos,
            mesa.elementosGenerales,
            mesa.tipo
        )
    }

    fun calcularPrecioTotal(
        tramos: List<Tramo>,
        cubetas: List<Cubeta>,
        modulos: List<Modulo>,
        elementos: List<ElementoSeleccionado>,
        material: String
    ): Double {
        return calcularPrecioMesa(tramos, material) +
                calcularPrecioCubetas(cubetas) +
                calcularPrecioModulos(modulos) +
                calcularPrecioElementos(elementos)
    }

    // Métodos auxiliares para calcular cada parte del presupuesto
    // usando las fórmulas de la base de datos

    private fun calcularPrecioMesa(tramos: List<Tramo>, material: String): Double {
        val formulasMesa = formulas.filter { it.aplicaA == "MESA" }
        // Implementación del cálculo
        return 0.0
    }

    private fun calcularPrecioCubetas(cubetas: List<Cubeta>): Double {
        val formulasCubeta = formulas.filter { it.aplicaA == "CUBETA" }
        // Implementación del cálculo
        return 0.0
    }

    private fun calcularPrecioModulos(modulos: List<Modulo>): Double {
        val formulasModulo = formulas.filter { it.aplicaA == "MODULO" }
        // Implementación del cálculo
        return 0.0
    }

    private fun calcularPrecioElementos(elementos: List<ElementoSeleccionado>): Double {
        val formulasElemento = formulas.filter { it.aplicaA == "ELEMENTO" }
        // Implementación del cálculo
        return 0.0
    }
}