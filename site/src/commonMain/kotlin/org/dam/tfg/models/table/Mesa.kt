package org.dam.tfg.models.table

import kotlinx.serialization.Serializable

@Serializable
data class Mesa(
    val id: String = "",
    val tipo: String = "",
    val tramos: List<Tramo> = listOf(),
    val extras: List<Extra> = listOf(),
    var precioTotal: Double = 0.0,
    var error: String = ""
) {

}