package org.dam.tfg.models.table

import kotlinx.serialization.Serializable
import org.dam.tfg.models.ItemWithLimits

@Serializable
data class Modulo(
    val nombre: String,
    val largo: Double,
    val fondo: Double,
    val alto: Double,
    val cantidad: Int,
    val limite: ItemWithLimits,
    val precio: Double = 0.0,
)

@Serializable
data class ElementoSeleccionado(
    val nombre: String,
    val cantidad: Int,
    val precio: Double = 0.0,
    val limite: ItemWithLimits
)