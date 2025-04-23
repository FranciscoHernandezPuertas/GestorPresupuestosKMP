package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
actual class Material(
    @SerialName("_id")
    actual val id: String = ObjectId().toHexString(),
    actual val name: String,
    actual val price: Double
) {
    actual companion object {
        actual fun fromMap(map: Map<String, Any>): Material {
            return Material(
                id = map["_id"] as? String ?: ObjectId().toHexString(),
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