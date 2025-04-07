package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: String = "",
    val nombre: String = "",
    val precioPorMilimetroCuadrado: Double = 0.0
)