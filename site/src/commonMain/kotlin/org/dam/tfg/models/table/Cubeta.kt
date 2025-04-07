package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
data class Cubeta(
    override val tipo: String = "Cubeta",
    override val numero: Int = 0,
    override val largo: Double = 0.0,
    override val ancho: Double = 0.0,
    override val alto: Double? = null,
    override val precio: Double = 0.0,
    override var error: String = "",
    val maxQuantity: Int?,
    val minQuantity: Int = 0,
) : Extra() {
    override fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    override fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (ancho <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}