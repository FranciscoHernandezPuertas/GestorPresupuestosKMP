package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual class Formula(
    @SerialName("_id")
    actual val id: String = "",
    actual val nombre: String,
    actual val formula: String,
    actual val aplicaA: String,
    actual val variables: Map<String, String>
) {

    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to nombre,
            "formula" to formula,
            "aplicaA" to aplicaA,
            "variables" to variables
        )
    }
}