package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
actual class Material(
    actual val id: String = "",
    actual val nombre: String,
    actual val precio: Double,
) {
    actual companion object {
        actual fun fromMap(map: Map<String, Any>): Material {
            return Material(
                id = map["_id"] as String,
                nombre = map["nombre"] as String,
                precio = (map["precio"] as Number).toDouble()
            )
        }
    }

    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to nombre,
            "precio" to precio
        )
    }
}