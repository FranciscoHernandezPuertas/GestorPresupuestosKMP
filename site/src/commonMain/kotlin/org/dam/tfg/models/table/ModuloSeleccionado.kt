package org.dam.tfg.models.table

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.dam.tfg.models.ItemWithLimits

@Serializable
data class ModuloSeleccionado(
    val nombre: String,
    val largo: Double,
    val fondo: Double,
    val alto: Double,
    val cantidad: Int,
    @Contextual val limite: ItemWithLimits
)