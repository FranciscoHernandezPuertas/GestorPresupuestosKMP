package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual class Formula(
    @SerialName("_id")
    actual val id: String = "",
    actual val name: String,
    actual val formula: String,
    actual val formulaEncrypted: Boolean = true,
    actual val aplicaA: String,
    actual val variables: Map<String, String>
) {
    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to name,
            "formula" to formula,
            "formulaEncrypted" to formulaEncrypted,
            "aplicaA" to aplicaA,
            "variables" to variables
        )
    }
}