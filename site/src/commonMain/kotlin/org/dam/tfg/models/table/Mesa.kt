package org.dam.tfg.models.table
import kotlinx.serialization.Serializable

@Serializable
expect class Mesa {
    val id: String
    val tipo: String
    val tramos: List<Tramo>
    val cubetas: List<Cubeta>
    val modulos: List<ModuloSeleccionado>
    val elementosGenerales: Map<String, Int>
    val precioTotal: Double
    var error: String

    fun isValid(): Boolean
    fun calcularSuperficieTotal(): Double
    fun toMap(): Map<String, Any>

    companion object
}