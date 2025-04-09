package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
data class Tramo(
    val numero: Int = 0,
    val largo: Double = 0.0,
    val ancho: Double = 0.0,
    val tipo: TipoTramo = TipoTramo.CENTRAL,
    var error: String = ""
) {
    fun isValid(): Boolean {
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

    fun superficie(): Double = largo * ancho
}