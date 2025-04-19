package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
actual class Material(
    @SerialName("_id")
    actual val id: String,
    actual val nombre: String,
    actual val precio: Double
) {
    actual companion object {
        actual fun fromMap(map: Map<String, Any>): Material {
            return Material(
                id = map["_id"] as? String ?: ObjectId().toHexString(),
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