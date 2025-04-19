package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
actual class Formula(
    actual val id: String = "",
    actual val nombre: String,
    actual val descripcion: String,
    actual val formula: String,
    actual val aplicaA: String,
    actual val variables: Map<String, String>
) {
    actual companion object {}

    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to nombre,
            "descripcion" to descripcion,
            "formula" to formula,
            "aplicaA" to aplicaA,
            "variables" to variables
        )
    }
}