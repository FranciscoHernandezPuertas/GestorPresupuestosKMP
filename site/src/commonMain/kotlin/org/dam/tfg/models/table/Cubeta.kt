package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
data class Cubeta(
    val tipo: String = "Cubeta",
    val numero: Int = 0,
    val largo: Double = 0.0,
    val fondo: Double = 0.0,
    val alto: Double? = null,
    val precio: Double = 0.0,
    var error: String = "",
    val maxQuantity: Int? = null,
    val minQuantity: Int = 0,
) {
    fun calcularPrecio(): Double {
        // Lógica pendiente
        return 0.0
    }

    fun isValid(): Boolean {
        if (largo <= 0) {
            error = "El largo debe ser mayor a 0"
            return false
        }
        if (fondo <= 0) {
            error = "El ancho debe ser mayor a 0"
            return false
        }
        error = ""
        return true
    }
}