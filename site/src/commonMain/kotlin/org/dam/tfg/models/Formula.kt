package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
expect class Formula {
    val id: String
    val nombre: String
    val formula: String
    val aplicaA: String // "MESA", "CUBETA", "MODULO", etc.
    val variables: Map<String, String> // Nombre de variable -> descripción

    fun toMap(): Map<String, Any>

}