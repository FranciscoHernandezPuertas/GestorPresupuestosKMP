package org.dam.tfg.models.table
import kotlinx.serialization.Serializable

@Serializable
expect class Mesa {
    val id: String
    val tipo: String
    val tramos: List<Tramo>
    val elementosGenerales: List<ElementoSeleccionado>
    val cubetas: List<Cubeta>
    val modulos: List<Modulo>
    val precioTotal: Double
    val fechaCreacion: String?
    var error: String

    fun isValid(): Boolean
    fun calcularSuperficieTotal(): Double
    fun toMap(): Map<String, Any>
}