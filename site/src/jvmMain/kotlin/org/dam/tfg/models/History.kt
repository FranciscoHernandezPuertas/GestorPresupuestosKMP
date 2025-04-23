package org.dam.tfg.models

import kotlinx.serialization.Serializable

@Serializable
actual class History(
    actual val id: String,
    actual val userId: String,
    actual val action: String,
    actual val timestamp: String,
    actual val details: String
) {
    actual fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "action" to action,
            "timestamp" to timestamp,
            "details" to details
        )
    }

    actual fun isValid(): Boolean {
        return id.isNotEmpty() && userId.isNotEmpty() && action.isNotEmpty() && timestamp.isNotEmpty()
    }
}