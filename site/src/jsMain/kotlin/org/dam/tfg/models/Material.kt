package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual class Material(
    @SerialName("_id")
    actual val id: String = "",
    actual val name: String,
    actual val price: Double,
) {
    actual companion object {
        actual fun fromMap(map: Map<String, Any>): Material {
            return Material(
                id = map["_id"] as String,
                name = map["nombre"] as String,
                price = (map["precio"] as Number).toDouble()
            )
        }
    }

    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to name,
            "precio" to price
        )
    }
}