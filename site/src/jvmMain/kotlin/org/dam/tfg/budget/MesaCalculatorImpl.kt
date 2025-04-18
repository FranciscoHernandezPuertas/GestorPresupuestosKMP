package org.dam.tfg.budget

import org.dam.tfg.models.table.Cubeta
import org.dam.tfg.models.table.ModuloSeleccionado
import org.dam.tfg.models.table.Tramo
import org.dam.tfg.data.MongoDB

class BudgetCalculatorImpl(private val mongoDB: MongoDB) : BudgetCalculator {
    override fun calcularPrecioMesa(tramos: List<Tramo>, material: String): Double {
        // Implementación real: consulta a base de datos para precios de materiales
        // y cálculos según dimensiones
        return tramos.sumOf { it.superficie() * getPrecioMaterial(material) }
    }

    override fun calcularPrecioCubetas(cubetas: List<Cubeta>): Double {
        // Implementación real: consulta a base de datos para precios de cubetas
        return cubetas.sumOf { getPrecioCubeta(it.tipo) }
    }

    override fun calcularPrecioModulos(modulos: List<ModuloSeleccionado>): Double {
        // Implementación real: consulta a base de datos para precios de módulos
        return modulos.sumOf { modulo ->
            getPrecioModulo(modulo.nombre) * modulo.cantidad
        }
    }

    override fun calcularPrecioElementos(elementos: Map<String, Int>): Double {
        // Implementación real: consulta a base de datos para precios de elementos
        return elementos.entries.sumOf { (nombre, cantidad) ->
            getPrecioElemento(nombre) * cantidad
        }
    }

    override fun calcularPrecioTotal(
        tramos: List<Tramo>,
        cubetas: List<Cubeta>,
        modulos: List<ModuloSeleccionado>,
        elementos: Map<String, Int>,
        material: String
    ): Double {
        val precioMesa = calcularPrecioMesa(tramos, material)
        val precioCubetas = calcularPrecioCubetas(cubetas)
        val precioModulos = calcularPrecioModulos(modulos)
        val precioElementos = calcularPrecioElementos(elementos)

        return precioMesa + precioCubetas + precioModulos + precioElementos
    }

    private fun getPrecioMaterial(material: String): Double {
        // Consulta a MongoDB para obtener precio del material
        return 0.01 // Valor de ejemplo
    }

    private fun getPrecioCubeta(tipoCubeta: String): Double {
        // Consulta a MongoDB para obtener precio de la cubeta
        return 150.0 // Valor de ejemplo
    }

    private fun getPrecioModulo(tipoModulo: String): Double {
        // Consulta a MongoDB para obtener precio del módulo
        return 200.0 // Valor de ejemplo
    }

    private fun getPrecioElemento(tipoElemento: String): Double {
        // Consulta a MongoDB para obtener precio del elemento
        return 50.0 // Valor de ejemplo
    }
}