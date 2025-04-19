package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
expect class Material {
    val id: String
    val nombre: String
    val precio: Double

    companion object {
        fun fromMap(map: Map<String, Any>): Material
    }

    fun toMap(): Map<String, Any>
}