package org.dam.tfg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.Clock

@Serializable
actual class Formula(
    @SerialName("_id")
    actual val id: String = ObjectId().toHexString(),
    actual val name: String,
    actual val formula: String,
    actual val formulaEncrypted: Boolean = true,
    actual val variables: Map<String, String>
) {
    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "_id" to id,
            "nombre" to name,
            "formula" to formula,
            "formulaEncrypted" to formulaEncrypted,
            "variables" to variables
        )
    }
}